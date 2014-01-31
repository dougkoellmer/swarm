package swarm.client.view.cell;

import java.util.HashMap;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class SpritePlateSpinner extends FlowPanel implements I_CellSpinner
{
	//--- DRK > Just caches class names so each update for each animation doesn't do string concatenation.
	private static final HashMap<String, String[]> s_classNames = new HashMap<String, String[]>();
	
	private final String[] m_classNames;
	private final int m_frameCount;
	private final double m_frameRate;
	
	private double m_time = 0.0;
	private int m_frame = 0;
	
	public SpritePlateSpinner(String className, int frameCount, double frameRate)
	{
		m_frameCount = frameCount;
		m_frameRate = frameRate;
		
		String[] classNames = s_classNames.get(className);
		
		if( classNames == null )
		{
			classNames = new String[frameCount];
			
			for( int i = 0; i < frameCount; i++ )
			{
				classNames[i] = className + "_" + i;
			}
			
			s_classNames.put(className, classNames);
		}
		
		m_classNames = classNames;
		
		this.reset();
	}
	
	private void setFrame(int frame)
	{
		m_frame = frame % m_frameCount;
		String newClassName = m_classNames[m_frame];
		this.setStyleName(newClassName);
	}

	@Override
	public void update(double timeStep)
	{
		m_time += timeStep;
		
		if( m_time >= m_frameRate )
		{
			m_time = 0.0;
			this.setFrame(m_frame+1);
		}
	}

	@Override
	public void reset()
	{
		this.setFrame(0);
	}
}
