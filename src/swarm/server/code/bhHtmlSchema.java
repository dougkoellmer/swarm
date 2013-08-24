package swarm.server.code;


import java.util.Set;
import java.util.logging.Logger;

import com.google.caja.config.WhiteList;
import com.google.caja.lang.html.HTML;
import com.google.caja.lang.html.HTML.Attribute.Type;
import com.google.caja.lang.html.HtmlSchema;
import com.google.caja.parser.html.AttribKey;
import com.google.caja.parser.html.ElKey;

public class bhHtmlSchema extends HtmlSchema
{
	private static final Logger s_logger = Logger.getLogger(bhHtmlSchema.class.getName());
	
	private static final WhiteList EMPTY_WHITELIST = WhiteList.Factory.empty();
	
	private final HtmlSchema m_inner;
	private boolean m_foundJavaScript = false;
	private boolean m_noScriptMode = false;
	
	public bhHtmlSchema(HtmlSchema inner)
	{
		super(EMPTY_WHITELIST, EMPTY_WHITELIST);
		
		m_inner = inner;
	}
	
	public void setToNoScriptMode()
	{
		m_noScriptMode = true;
	}
	
	public Set<AttribKey> getAttributeNames()
	{
		return m_inner.getAttributeNames();
	}

	public Set<ElKey> getElementNames() {
		return m_inner.getElementNames();
	}

	public boolean isElementAllowed(ElKey elementName)
	{
		if( elementName.localName.equalsIgnoreCase("script") )
		{
			m_foundJavaScript = true;
			
			return false;
		}
		else if( elementName.localName.equalsIgnoreCase("caja-v-splash") )
		{
			//NOTE: For some reason, plain old "splash" tag doesn't make it in here.
			return m_noScriptMode;
		}
		
		boolean allowed = m_inner.isElementAllowed(elementName);
		
		return allowed;
	}

	public HTML.Element lookupElement(ElKey elementName) {
		return m_inner.lookupElement(elementName);
	}

	public boolean isElementVirtualized(ElKey el)
	{
		/*if( el.localName.equalsIgnoreCase("noscript") )
		{
			return true;
		}*/
		
		boolean isVirtualized = m_inner.isElementVirtualized(el);
		
		return isVirtualized;
	}

	public ElKey virtualToRealElementName(ElKey virtual)
	{
		return m_inner.virtualToRealElementName(virtual);
	}
	
	public boolean foundJavaScript()
	{
		return m_foundJavaScript;
	}

	public boolean isAttributeAllowed(AttribKey k)
	{
		//--- DRK > This check is here because if you e.g. have a DIV with onclick defined,
		//---		allowing the onclick attribute for some reason rewrites the DIVs id from what
		//---		a user potentially specified until the cajoled javascript comes in and sets it correctly.
		//---		But since we're not using cajoled JS, we have to disallow all script attributes so user-specified
		//---		IDs (perhaps among other things) appear correctly in the static html.
		HTML.Attribute attr = lookupAttribute(k);
		if( attr != null )
		{
			if( attr.getType() == Type.SCRIPT )
			{
				m_foundJavaScript = true;
				
				return false;
			}
		}
		
		return m_inner.isAttributeAllowed(k);
	}

	public HTML.Attribute lookupAttribute(AttribKey k) {
		return m_inner.lookupAttribute(k);
	}
}
