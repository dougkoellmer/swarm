package swarm.client.view;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;

public class smFpsViewer
{
	private final Element m_element;
	
	private final Element m_container;
	
	private final smFpsTracker m_tracker = new smFpsTracker();
	
	public smFpsViewer(Element container)
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
