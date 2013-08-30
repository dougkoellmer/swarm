package swarm.client.view.tooltip;

import swarm.client.view.alignment.smAlignmentDefinition;

public class smToolTipConfig
{
	private final String m_text;
	private final smE_ToolTipMood m_severity;
	private final smE_ToolTipType m_type;
	private final smAlignmentDefinition m_alignment;
	
	public smToolTipConfig(smE_ToolTipType type, smAlignmentDefinition alignment, String text)
	{
		m_text = text;
		m_severity = smE_ToolTipMood.NORMAL;
		m_type = type;
		m_alignment = alignment;
	}
	
	public smToolTipConfig(smE_ToolTipType type, String text, smE_ToolTipMood severity)
	{
		m_text = text;
		m_severity = severity;
		m_type = type;
		m_alignment = null;
	}
	
	public smToolTipConfig(smE_ToolTipType type, smAlignmentDefinition alignment, String text, smE_ToolTipMood severity)
	{
		m_text = text;
		m_severity = severity;
		m_type = type;
		m_alignment = alignment;
	}
	
	public smToolTipConfig(smE_ToolTipType type, String text)
	{
		m_type = type;
		m_text = text;
		m_severity = smE_ToolTipMood.NORMAL;
		m_alignment = null;
	}
	
	public smAlignmentDefinition getAlignmentDefinition()
	{
		return m_alignment;
	}
	
	public String getText()
	{
		return m_text;
	}
	
	public smE_ToolTipMood getSeverity()
	{
		return m_severity;
	}
	
	public smE_ToolTipType getType()
	{
		return m_type;
	}
}
