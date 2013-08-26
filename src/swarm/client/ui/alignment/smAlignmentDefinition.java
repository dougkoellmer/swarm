package swarm.client.ui.alignment;

public class smAlignmentDefinition
{	
	private smE_AlignmentPosition[] m_types = new smE_AlignmentPosition[smE_AlignmentType.values().length];
	private Double[] m_definitions = new Double[smE_AlignmentType.values().length];
	private Double[] m_paddings = new Double[smE_AlignmentType.values().length];
	
	private bhAlignmentRect m_masterRect = null;
	
	public smAlignmentDefinition()
	{
		
	}
	
	public smAlignmentRect getMasterRect()
	{
		return m_masterRect;
	}
	
	public void setMasterRect(smAlignmentRect rect)
	{
		m_masterRect = rect;
	}
	
	public void clear()
	{
		for( int i = 0; i < smE_AlignmentType.values().length; i++ )
		{
			m_types[i] = null;
			m_definitions[i] = null;
			m_paddings[i] = null;
		}
		
		this.m_masterRect = null;
	}
	
	public void copy(smAlignmentDefinition source)
	{
		for( int i = 0; i < smE_AlignmentType.values().length; i++ )
		{
			m_types[i] = source.m_types[i];
			m_definitions[i] = source.m_definitions[i];
			m_paddings[i] = source.m_paddings[i];
		}
		
		this.m_masterRect = source.m_masterRect;
	}
	
	public Double getPadding(smE_AlignmentType type)
	{
		return m_paddings[type.ordinal()];
	}
	
	public void setPadding(smE_AlignmentType type, Double value)
	{
		m_paddings[type.ordinal()] = value;
	}
	
	public Double getDefinedPosition(smE_AlignmentType type)
	{
		return m_definitions[type.ordinal()];
	}
	
	public void setDefinedPosition(smE_AlignmentType type, Double value)
	{
		m_definitions[type.ordinal()] = value;
		m_types[type.ordinal()] = smE_AlignmentPosition.DEFINED;
	}
	
	public void setPosition(smE_AlignmentType type, smE_AlignmentPosition value)
	{
		m_types[type.ordinal()] = value;
	}
	
	public smE_AlignmentPosition getPosition(smE_AlignmentType type)
	{
		return m_types[type.ordinal()];
	}
}
