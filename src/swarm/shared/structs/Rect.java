package swarm.shared.structs; 

/**
 * ...
 * @author 
 */
public class Rect
{
	private double m_width;
	private double m_height;
	
	public Rect() 
	{
		
	}
	
	public void set(double width, double height)
	{
		m_width = width;
		m_height = height;
	}
	
	public double getWidth()
	{
		return m_width;
	}
	
	public double getHeight()
	{
		return m_height;
	}
	
	public void incWidth(double delta)
	{
		m_width += delta;
	}
	
	public void incHeight(double delta)
	{
		m_height += delta;
	}
}