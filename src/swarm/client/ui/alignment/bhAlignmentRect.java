package swarm.client.ui.alignment;

public class bhAlignmentRect
{
	private final double[] m_parameters = new double[4];
	
	public bhAlignmentRect(double x, double y, double width, double height)
	{
		m_parameters[0] = x;
		m_parameters[1] = y;
		m_parameters[2] = width;
		m_parameters[3] = height;
	}
	
	public double getWidth()
	{
		return m_parameters[2];
	}
	
	public double getHeight()
	{
		return m_parameters[3];
	}
	
	public double getPositionComponent(int index)
	{
		return m_parameters[index];
	}
	
	public double getDimensionComponent(int index)
	{
		return m_parameters[index+2];
	}
}
