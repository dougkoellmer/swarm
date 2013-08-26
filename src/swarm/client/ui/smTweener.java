package swarm.client.ui;

import swarm.shared.utils.smU_Math;

public class smTweener
{
	private double m_tweenTime;
	private double m_startValue;
	private double m_endValue;
	private double m_elapsedTime;
	
	public smTweener(double tweenTime)
	{
		m_tweenTime = tweenTime;
		m_elapsedTime = m_tweenTime;
		m_startValue = m_endValue = 0;
	}
	
	public boolean isTweening()
	{
		return m_elapsedTime < m_tweenTime;
	}
	
	public void start(double startValue, double endValue)
	{
		m_startValue = startValue;
		m_endValue = endValue;
		m_elapsedTime = 0;
	}
	
	public void stop()
	{
		m_elapsedTime = m_tweenTime;
	}
	
	public double update(double timeStep)
	{
		m_elapsedTime += timeStep;
		
		m_elapsedTime = smU_Math.clamp(m_elapsedTime, 0, m_tweenTime);
		
		double timeRatio = 1 - m_elapsedTime / m_tweenTime;
		
		double tweenRatio = 1 - Math.pow(timeRatio, 6);
		
		double tweenValue = m_startValue + (m_endValue - m_startValue) * tweenRatio;
		
		return tweenValue;
	}
}
