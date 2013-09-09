package swarm.server.code;

import java.util.logging.Logger;

import org.apache.xerces.dom.ElementNSImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import swarm.shared.code.smU_Code;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smE_CellAddressParseError;
import com.google.caja.parser.html.AttribKey;
import com.google.caja.parser.html.Dom;
import com.google.caja.parser.html.ElKey;
import com.google.caja.parser.html.Nodes;

public class smHtmlPreProcessor
{
	private static final Logger s_logger = Logger.getLogger(smHtmlPreProcessor.class.getName());
	
	private boolean m_foundJs = false;
	private final smHtmlSchema m_schema;
	private Node m_bodyNode = null;
	private Node m_splashNode = null;
	
	private int m_splashNodeDepth = 0;
	private final Dom m_dom;
	
	public smHtmlPreProcessor(Dom htmlDom, smHtmlSchema schema)
	{
		m_dom = htmlDom;

		m_schema = schema;
		this.visit(m_dom.getValue(), null);
	}
	
	public String renderHtml()
	{
		return Nodes.render(m_dom.getValue());
	}
	
	public boolean hasSplashTag()
	{
		return m_splashNode != null && m_bodyNode != null;
	}
	
	public boolean injectSplashTag()
	{
		if( m_splashNode != null )
		{
			if( m_bodyNode != null )
			{
				while(m_bodyNode.getFirstChild() != null )
				{
					Node firstChild = m_bodyNode.getFirstChild();
					m_bodyNode.removeChild(firstChild);
				}
				m_bodyNode.appendChild(m_splashNode);
				
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
		if( m_splashNodeDepth == 0 )
		{
			m_foundJs = true;
		}
	}
	
	public boolean foundJavaScript()
	{
		return m_foundJs;
	}
	
	private boolean visit(Node node, Node parentNode)
	{		
		ElKey elKey = null;
		boolean isSplashNode = false;
		
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
			if( m_splashNode == null )
			{
				m_splashNode = node;
			}

			isSplashNode = true;
			m_splashNodeDepth++;
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
			if( !visit(child, node) )
			{
				node.removeChild(child);
			}
			
			child = next;
		}
		
		if( isSplashNode )
		{
			m_splashNodeDepth--;
		}
		
		return isSplashNode ? false : true;
	}
}
