package b33hive.client.ui.tabs.code;

import java.util.logging.Logger;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

public class bhStaticCajaContainer
{
	private static final Logger s_logger = Logger.getLogger(bhStaticCajaContainer.class.getName());
	
	private static final String OUTER_CONTAINER_STYLE = "position: relative; overflow: hidden; display: block; margin: 0px; padding: 0px;";
	private static final String OUTER_CONTAINER_CLASS = "caja-vdoc-outer caja-vdoc-wrapper";
	private static final String INNER_CONTAINER_STYLE = "display: block; position: relative;";
	private static final String INNER_CONTAINER_CLASS = "static-caja-vdoc caja-vdoc-inner caja-vdoc-wrapper vdoc-container___";


	private final Element m_host;
	private final Element m_outerCaja;
	private final Element m_innerCaja;
	private String m_idClass = null;
	
	bhStaticCajaContainer(Element host)
	{
		m_outerCaja = DOM.createElement("div");
		m_outerCaja.setAttribute("style", OUTER_CONTAINER_STYLE);
		m_outerCaja.setAttribute("class", OUTER_CONTAINER_CLASS);
		m_innerCaja = DOM.createElement("div");
		m_innerCaja.setAttribute("style", INNER_CONTAINER_STYLE);
		m_innerCaja.setAttribute("class", INNER_CONTAINER_CLASS);

		m_outerCaja.appendChild(m_innerCaja);
		
		m_host = host;
		
		allowScrolling(false);
	}
	
	void detachIfNecessary()
	{
		if( m_outerCaja.getParentElement() == m_host )
		{
			m_host.removeChild(m_outerCaja);
		}
	}
	
	void allowScrolling(boolean yesOrNo)
	{
		if( yesOrNo )
		{
			m_innerCaja.getStyle().setOverflow(Overflow.AUTO);
		}
		else
		{
			m_innerCaja.setScrollLeft(0);
			m_innerCaja.setScrollTop(0);
			m_innerCaja.getStyle().setOverflow(Overflow.HIDDEN);
		}
	}
	
	private void ensureHostAttachment()
	{
		if( m_outerCaja.getParentElement() != m_host )
		{
			com.google.gwt.dom.client.Element child = m_host.getFirstChildElement();
			while( child != null )
			{
				com.google.gwt.dom.client.Element nextChild = child.getNextSiblingElement();
				child.removeFromParent();
				child = nextChild;
			}
			
			m_host.appendChild(m_outerCaja);
		}
		
		//--- DRK > Somehow, in IE-only, the inner container gets implicitly detached somewhere,
		//---		so just lazily reattached here when necessary. Sloppy, but not sure how to debug this one.
		if( m_innerCaja.getParentElement() != m_outerCaja )
		{
			m_outerCaja.appendChild(m_innerCaja);
		}
	}
	
	void setInnerHtml(String html)
	{
		ensureHostAttachment();
		
		m_innerCaja.setInnerHTML(html);
	}
	
	void setIdClass(String idClass)
	{
		if( m_idClass != null )
		{
			if( idClass.equals(m_idClass) )  return;
			
			m_innerCaja.removeClassName(m_idClass);
		}
		
		m_idClass = idClass;
		m_innerCaja.addClassName(idClass);
	}
}
