package swarm.client.view.widget;

import swarm.client.view.U_Css;
import swarm.client.view.U_Image;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.DOM;

public class Animation
{
	private final Element m_container;
	private final Element[] m_elements;
	private double m_time;
	private final double m_frameRate;
	private int m_currentIndex;
	
	public Animation(double frameRate, String ... imagePaths)
	{
		m_frameRate = frameRate;
		
		m_container = DOM.createDiv();
		
		m_elements = new Element[imagePaths.length];
		
		for( int i = 0; i < imagePaths.length; i++ )
		{
			Element frame = U_Image.createNonCachedImage(m_container, imagePaths[i], 1);
			frame.getStyle().setPosition(Position.ABSOLUTE);
			U_Css.setPosition(frame, 0, 0);
			frame.getStyle().setDisplay(Display.NONE);
			
			m_elements[i] = frame;
		}
		
		m_currentIndex = 0;
		m_time = 0;
		
		showFrame(0);
	}
	
	private void showFrame(int index)
	{
		int lastIndex = index-1;
		if( lastIndex < 0 )
		{
			lastIndex = m_elements.length-1;
		}
		
		m_elements[lastIndex].getStyle().setDisplay(Display.NONE);
		m_elements[index].getStyle().clearDisplay();
	}
	
	public void update(double timeStep)
	{
		m_time += timeStep;
		
		if( m_time >= m_frameRate )
		{
			m_currentIndex = (m_currentIndex + 1) % m_elements.length;
			
			showFrame(m_currentIndex);
			
			m_time = 0;
		}
	}
	
	public Element getContainer()
	{
		return m_container;
	}
}
