package swarm.shared.structs; 

/**
 * ...
 * @author 
 */
public class smRect
{
	private double m_width;
	private double m_height;
	
	public smRect() 
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
}