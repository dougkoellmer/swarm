package com.b33hive.client.ui.alignment;

public class bhAlignmentDefinition
{	
	private bhE_AlignmentPosition[] m_types = new bhE_AlignmentPosition[bhE_AlignmentType.values().length];
	private Double[] m_definitions = new Double[bhE_AlignmentType.values().length];
	private Double[] m_paddings = new Double[bhE_AlignmentType.values().length];
	
	private bhAlignmentRect m_masterRect = null;
	
	public bhAlignmentDefinition()
	{
		
	}
	
	public bhAlignmentRect getMasterRect()
	{
		return m_masterRect;
	}
	
	public void setMasterRect(bhAlignmentRect rect)
	{
		m_masterRect = rect;
	}
	
	public void clear()
	{
		for( int i = 0; i < bhE_AlignmentType.values().length; i++ )
		{
			m_types[i] = null;
			m_definitions[i] = null;
			m_paddings[i] = null;
		}
		
		this.m_masterRect = null;
	}
	
	public void copy(bhAlignmentDefinition source)
	{
		for( int i = 0; i < bhE_AlignmentType.values().length; i++ )
		{
			m_types[i] = source.m_types[i];
			m_definitions[i] = source.m_definitions[i];
			m_paddings[i] = source.m_paddings[i];
		}
		
		this.m_masterRect = source.m_masterRect;
	}
	
	public Double getPadding(bhE_AlignmentType type)
	{
		return m_paddings[type.ordinal()];
	}
	
	public void setPadding(bhE_AlignmentType type, Double value)
	{
		m_paddings[type.ordinal()] = value;
	}
	
	public Double getDefinedPosition(bhE_AlignmentType type)
	{
		return m_definitions[type.ordinal()];
	}
	
	public void setDefinedPosition(bhE_AlignmentType type, Double value)
	{
		m_definitions[type.ordinal()] = value;
		m_types[type.ordinal()] = bhE_AlignmentPosition.DEFINED;
	}
	
	public void setPosition(bhE_AlignmentType type, bhE_AlignmentPosition value)
	{
		m_types[type.ordinal()] = value;
	}
	
	public bhE_AlignmentPosition getPosition(bhE_AlignmentType type)
	{
		return m_types[type.ordinal()];
	}
}
