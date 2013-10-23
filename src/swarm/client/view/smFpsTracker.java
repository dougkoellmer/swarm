package swarm.client.view;

public class smFpsTracker
{
	private static final double UPDATE_RATE = .5;
	
	private double m_frameCount;
	private double m_time = 0;
	private double m_frameRate = 0;
	
	public smFpsTracker()
	{
	}
	
	public void update(double timeStep)
	{
		m_frameCount++;
		m_time += timeStep;
		
		if( m_time >= UPDATE_RATE )
		{
			m_frameRate = Math.round((m_frameCount) / m_time);
			
			m_frameCount = 0;
			m_time = 0;
		}
	}
	
	public double getFrameRate()
	{
		return m_frameRate;
	}
}
