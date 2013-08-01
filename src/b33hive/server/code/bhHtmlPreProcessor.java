package com.b33hive.server.code;

import java.util.logging.Logger;

import org.apache.xerces.dom.ElementNSImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.b33hive.shared.code.bhU_Code;
import com.b33hive.shared.structs.bhCellAddress;
import com.b33hive.shared.structs.bhE_CellAddressParseError;
import com.google.caja.parser.html.AttribKey;
import com.google.caja.parser.html.Dom;
import com.google.caja.parser.html.ElKey;

public class bhHtmlPreProcessor
{
	private static final Logger s_logger = Logger.getLogger(bhHtmlPreProcessor.class.getName());
	
	private boolean m_foundJs = false;
	private final bhHtmlSchema m_schema;
	private Node m_bodyNode = null;
	private Node m_noscriptNode = null;
	private int m_noScriptDepth = 0;
	
	public bhHtmlPreProcessor(Dom htmlDom, bhHtmlSchema schema)
	{
		ElementNSImpl node;

		m_schema = schema;
		this.visit(htmlDom.getValue());
	}
	
	public boolean hasNoscriptContent()
	{
		return m_noscriptNode != null && m_bodyNode != null;
	}
	
	public boolean injectNoscriptTag()
	{
		if( m_noscriptNode != null )
		{
			if( m_bodyNode != null )
			{
				while(m_bodyNode.getFirstChild() != null )
				{
					m_bodyNode.removeChild(m_bodyNode.getFirstChild());
				}
				m_bodyNode.appendChild(m_noscriptNode);
				
				return true;
			}
			else
			{
				s_logger.severe("Expected body node to be set for noscript transformation.");
			}
		}
		
		return false;
	}
	
	private void onFoundJs()
	{
		if( m_noScriptDepth == 0 )
		{
			m_foundJs = true;
		}
	}
	
	public boolean foundJavaScript()
	{
		return m_foundJs;
	}
	
	private boolean visit(Node node)
	{
		ElKey elKey = null;
		boolean isNoScriptNode = false;
		
		if( node.getNodeName().equalsIgnoreCase("script") )
		{
			onFoundJs();
			
			return false;
		}
		else if( node.getNodeName().equalsIgnoreCase("style") )
		{
			//--- DRK > For some reason style::type shows up as javascript-related, and we don't want that
			//---		kind of potentially false-positive for m_foundJs, so we skip the attribute scanning below
			//---		and early out here.
			//---		If it turns out that something with the "style" tag in this DOM *is* script-related,
			//---		then Caja should pick up on it and we will have a non-empty JS module to let us
			//---		know that JS was found.
			return true;
		}
		else if( node.getNodeName().equalsIgnoreCase("splash") )
		{
			if( m_noscriptNode == null )
			{
				//--- DRK > Can't change node name, so wrap it and override instead.
				m_noscriptNode = node;
			}

			isNoScriptNode = true;
			m_noScriptDepth++;
		}
		else if( node.getNodeName().equalsIgnoreCase("body") )
		{
			if( m_bodyNode == null )
			{
				m_bodyNode = node;
			}
		}
		
		NamedNodeMap attributes = node.getAttributes();
		if( attributes != null )
		{
			int length = attributes.getLength();
			for( int i = 0; i < length; i++ )
			{
				Node attrNode = attributes.item(i);
				
				if( attrNode != null && (attrNode instanceof Attr) )
				{
					Attr attribute = (Attr) attrNode;
					elKey = elKey != null ? elKey : ElKey.forHtmlElement(node.getNodeName());
					AttribKey attrKey = AttribKey.forAttribute(elKey, attribute);
					
					if( !m_schema.isAttributeAllowed(attrKey) )
					{
						onFoundJs();
						
						attributes.removeNamedItem(attribute.getName());
					}
				}
			}
		}
		
		Node child = node.getFirstChild();
		while( child != null )
		{
			Node next = child.getNextSibling();
			if( !visit(child) )
			{
				node.removeChild(child);
			}
			
			child = next;
		}
		
		if( isNoScriptNode )
		{
			m_noScriptDepth--;
		}
		
		return isNoScriptNode ? false : true;
	}
}
