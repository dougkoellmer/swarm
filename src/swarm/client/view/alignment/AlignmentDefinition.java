package swarm.client.view.alignment;

public class AlignmentDefinition
{	
	private E_AlignmentPosition[] m_types = new E_AlignmentPosition[E_AlignmentType.values().length];
	private Double[] m_definitions = new Double[E_AlignmentType.values().length];
	private Double[] m_paddings = new Double[E_AlignmentType.values().length];
	
	private AlignmentRect m_masterRect = null;
	
	public AlignmentDefinition()
	{
		
	}
	
	public AlignmentRect getMasterRect()
	{
		return m_masterRect;
	}
	
	public void setMasterRect(AlignmentRect rect)
	{
		m_masterRect = rect;
	}
	
	public void clear()
	{
		for( int i = 0; i < E_AlignmentType.values().length; i++ )
		{
			m_types[i] = null;
			m_definitions[i] = null;
			m_paddings[i] = null;
		}
		
		this.m_masterRect = null;
	}
	
	public void copy(AlignmentDefinition source)
	{
		for( int i = 0; i < E_AlignmentType.values().length; i++ )
		{
			m_types[i] = source.m_types[i];
			m_definitions[i] = source.m_definitions[i];
			m_paddings[i] = source.m_paddings[i];
		}
		
		this.m_masterRect = source.m_masterRect;
	}
	
	public Double getPadding(E_AlignmentType type)
	{
		return m_paddings[type.ordinal()];
	}
	
	public void setPadding(E_AlignmentType type, Double value)
	{
		m_paddings[type.ordinal()] = value;
	}
	
	public Double getDefinedPosition(E_AlignmentType type)
	{
		return m_definitions[type.ordinal()];
	}
	
	public void setDefinedPosition(E_AlignmentType type, Double value)
	{
		m_definitions[type.ordinal()] = value;
		m_types[type.ordinal()] = E_AlignmentPosition.DEFINED;
	}
	
	public void setPosition(E_AlignmentType type, E_AlignmentPosition value)
	{
		m_types[type.ordinal()] = value;
	}
	
	public E_AlignmentPosition getPosition(E_AlignmentType type)
	{
		return m_types[type.ordinal()];
	}
}
