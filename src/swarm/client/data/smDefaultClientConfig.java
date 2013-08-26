package swarm.client.data;

import java.util.ArrayList;

public class smDefaultClientConfig implements smI_Config
{
	private final ArrayList<Object> m_values = new ArrayList<Object>();
	
	public smDefaultClientConfig()
	{
		
	}
	
	protected void setProperty(Enum property, Object value)
	{
		if( m_values.size() <= property.ordinal() )
		{
			m_values.ensureCapacity(property.ordinal()+1);
		}
		
		m_values.set(property.ordinal(), value);
	}
	
	@Override
	public int getInt(Enum property)
	{
		return (Integer) m_values.get(property.ordinal());
	}

	@Override
	public double getDouble(Enum property)
	{
		return (Double) m_values.get(property.ordinal());
	}
}
