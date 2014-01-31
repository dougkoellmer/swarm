package swarm.client.view;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;

public class FpsViewer
{
	private final Element m_element;
	
	private final Element m_container;
	
	private final FpsTracker m_tracker = new FpsTracker();
	
	public FpsViewer(Element container)
	{
		m_container = container;
		
		m_element = DOM.createDiv();
		m_element.getStyle().setPosition(Position.ABSOLUTE);
		m_container.appendChild(m_element);
	}
	
	public void update(double timeStep)
	{
		m_tracker.update(timeStep);
		
		String fps = Math.round(m_tracker.getFrameRate()) + "";
		m_element.setInnerHTML(fps + "FPS");
	}
}
