package swarm.client.view.tooltip;

import swarm.client.view.alignment.AlignmentDefinition;

public class ToolTipConfig
{
	private final String m_text;
	private final E_ToolTipMood m_severity;
	private final E_ToolTipType m_type;
	private final AlignmentDefinition m_alignment;
	
	public ToolTipConfig(E_ToolTipType type, AlignmentDefinition alignment, String text)
	{
		m_text = text;
		m_severity = E_ToolTipMood.NORMAL;
		m_type = type;
		m_alignment = alignment;
	}
	
	public ToolTipConfig(E_ToolTipType type, String text, E_ToolTipMood severity)
	{
		m_text = text;
		m_severity = severity;
		m_type = type;
		m_alignment = null;
	}
	
	public ToolTipConfig(E_ToolTipType type, AlignmentDefinition alignment, String text, E_ToolTipMood severity)
	{
		m_text = text;
		m_severity = severity;
		m_type = type;
		m_alignment = alignment;
	}
	
	public ToolTipConfig(E_ToolTipType type, String text)
	{
		m_type = type;
		m_text = text;
		m_severity = E_ToolTipMood.NORMAL;
		m_alignment = null;
	}
	
	public AlignmentDefinition getAlignmentDefinition()
	{
		return m_alignment;
	}
	
	public String getText()
	{
		return m_text;
	}
	
	public E_ToolTipMood getSeverity()
	{
		return m_severity;
	}
	
	public E_ToolTipType getType()
	{
		return m_type;
	}
}
