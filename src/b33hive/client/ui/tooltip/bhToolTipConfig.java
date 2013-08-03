package b33hive.client.ui.tooltip;

import b33hive.client.ui.alignment.bhAlignmentDefinition;

public class bhToolTipConfig
{
	private final String m_text;
	private final bhE_ToolTipMood m_severity;
	private final bhE_ToolTipType m_type;
	private final bhAlignmentDefinition m_alignment;
	
	public bhToolTipConfig(bhE_ToolTipType type, bhAlignmentDefinition alignment, String text)
	{
		m_text = text;
		m_severity = bhE_ToolTipMood.NORMAL;
		m_type = type;
		m_alignment = alignment;
	}
	
	public bhToolTipConfig(bhE_ToolTipType type, String text, bhE_ToolTipMood severity)
	{
		m_text = text;
		m_severity = severity;
		m_type = type;
		m_alignment = null;
	}
	
	public bhToolTipConfig(bhE_ToolTipType type, bhAlignmentDefinition alignment, String text, bhE_ToolTipMood severity)
	{
		m_text = text;
		m_severity = severity;
		m_type = type;
		m_alignment = alignment;
	}
	
	public bhToolTipConfig(bhE_ToolTipType type, String text)
	{
		m_type = type;
		m_text = text;
		m_severity = bhE_ToolTipMood.NORMAL;
		m_alignment = null;
	}
	
	public bhAlignmentDefinition getAlignmentDefinition()
	{
		return m_alignment;
	}
	
	public String getText()
	{
		return m_text;
	}
	
	public bhE_ToolTipMood getSeverity()
	{
		return m_severity;
	}
	
	public bhE_ToolTipType getType()
	{
		return m_type;
	}
}
