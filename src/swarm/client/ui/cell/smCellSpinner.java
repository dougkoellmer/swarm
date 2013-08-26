package swarm.client.ui.cell;

import java.util.logging.Logger;

import swarm.client.ui.smU_UI;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

public class smCellSpinner extends FlowPanel
{
	private static final double WING_FLAP_TIME = 1.0/30.0;
	
	private static final Logger s_logger = Logger.getLogger(smCellSpinner.class.getName());
	
	private final FlowPanel m_inner = new FlowPanel();
	private final Image[] m_b33s = new Image[2];
	private double m_rotationRate;
	private double m_currentRotation = 0;
	private double m_timeProgressed = 0;
	private int m_currentB33 = 0;
	
	public smCellSpinner(double rotationRate)
	{
		m_rotationRate = rotationRate;
		
		m_inner.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
		m_inner.setSize("100%", "100%");
		//bhU_UI.setTransformOrigin(m_inner.getElement(), "50%", "50%");
		
		
		for( int i = 0; i < m_b33s.length; i++ )
		{
			Image b33 = m_b33s[i] = new Image();
			
			if( i == 0 )
			{
				b33.setUrl("r.img/b33_spinner0001.png?v=5");
			}
			else
			{
				b33.setUrl("r.img/b33_spinner0002.png?v=5");
			}
			b33.getElement().getStyle().setPosition(Position.RELATIVE);
			b33.getElement().getStyle().setLeft(15, Unit.PX);
			//b33.getElement().getStyle().setOpacity(.75);
			b33.setVisible(false);
			m_inner.add(b33);
		}
		this.add(m_inner);
		
		reset();
	}
	
	public void reset()
	{
		m_currentRotation = (Math.PI*2)*.75;
		m_b33s[0].setVisible(true);
		m_b33s[1].setVisible(false);
		m_currentB33 = 0;
	}
	
	private void updateSpinnerRotation()
	{
		double deg = m_currentRotation * (180/Math.PI);
		deg = Math.round(deg);
		String transform = bhU_UI.createRotate2dTransform(deg);
		bhU_UI.setTransform(m_inner.getElement(), transform);
	}
	
	public void update(double timeStep)
	{
		//m_currentRotation += timeStep * m_rotationRate;
		m_timeProgressed += timeStep;
		
		if( m_timeProgressed >= WING_FLAP_TIME )
		{
			m_currentB33++;
			m_currentB33 = m_currentB33 % m_b33s.length;
			m_timeProgressed = 0;
			
			for( int i = 0; i < m_b33s.length; i++ )
			{
				Image b33 = m_b33s[i];
				if( i == m_currentB33 )
				{
					b33.setVisible(true);
				}
				else
				{
					b33.setVisible(false);
				}
			}
		}
		
		
		
	//	updateSpinnerRotation();
	}
}
