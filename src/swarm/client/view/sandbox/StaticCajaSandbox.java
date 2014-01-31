package swarm.client.view.sandbox;

import java.util.logging.Logger;

import swarm.client.view.tabs.code.I_CodeLoadListener;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

public class StaticCajaSandbox implements I_CellSandbox
{
	private static final Logger s_logger = Logger.getLogger(StaticCajaSandbox.class.getName());
	
	private static final String OUTER_CONTAINER_STYLE = "position: relative; overflow: hidden; display: block; margin: 0px; padding: 0px;";
	private static final String OUTER_CONTAINER_CLASS = "caja-vdoc-outer caja-vdoc-wrapper";
	private static final String INNER_CONTAINER_STYLE = "display: block; position: relative;";
	private static final String INNER_CONTAINER_CLASS = "static-caja-vdoc caja-vdoc-inner caja-vdoc-wrapper vdoc-container___";

	private final Element m_outerCaja;
	private final Element m_innerCaja;
	private String m_lastCellNamespace = null;
	
	StaticCajaSandbox()
	{
		m_outerCaja = DOM.createElement("div");
		m_outerCaja.setAttribute("style", OUTER_CONTAINER_STYLE);
		m_outerCaja.setAttribute("class", OUTER_CONTAINER_CLASS);
		m_innerCaja = DOM.createElement("div");
		m_innerCaja.setAttribute("style", INNER_CONTAINER_STYLE);
		m_innerCaja.setAttribute("class", INNER_CONTAINER_CLASS);

		m_outerCaja.appendChild(m_innerCaja);
		
		allowScrolling(false);
	}
	
	private void detachIfNecessary(Element host)
	{
		if( m_outerCaja.getParentElement() == host )
		{
			host.removeChild(m_outerCaja);
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
	
	private void ensureHostAttachment(Element host)
	{
		if( m_outerCaja.getParentElement() != host )
		{
			com.google.gwt.dom.client.Element child = host.getFirstChildElement();
			while( child != null )
			{
				com.google.gwt.dom.client.Element nextChild = child.getNextSiblingElement();
				child.removeFromParent();
				child = nextChild;
			}
			
			host.appendChild(m_outerCaja);
		}
		
		//--- DRK > Somehow, in IE-only, the inner container gets implicitly detached somewhere,
		//---		so just lazily reattached here when necessary. Sloppy, but not sure how to debug this one.
		if( m_innerCaja.getParentElement() != m_outerCaja )
		{
			m_outerCaja.appendChild(m_innerCaja);
		}
	}
	
	private void setCellNamespace(String cellNamespace)
	{
		if( m_lastCellNamespace != null )
		{
			if( cellNamespace.equals(m_lastCellNamespace) )  return;
			
			m_innerCaja.removeClassName(m_lastCellNamespace);
		}
		
		m_innerCaja.addClassName(cellNamespace);
		
		m_lastCellNamespace = cellNamespace;
	}

	@Override
	public void start(Element host, String rawCode, String cellNamespace, I_CodeLoadListener listener)
	{
		ensureHostAttachment(host);
		setCellNamespace(cellNamespace);
		m_innerCaja.setInnerHTML(rawCode);
		
		listener.onCodeLoad();
	}

	@Override
	public void stop(Element host) 
	{
		detachIfNecessary(host);
	}
}
