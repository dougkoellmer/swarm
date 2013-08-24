package swarm.client.ui.tooltip;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Label;

public class bhToolTip extends Label
{
	private Element m_targetElement = null;
	private bhToolTipConfig m_config;
	
	public bhToolTip()
	{
		this.addStyleName("sm_tool_tip");
		this.setWordWrap(false);
	}
	
	void init(Element targetElement, bhToolTipConfig config)
	{
		m_targetElement = targetElement;
		m_config = config;
		
		this.setSeverity(m_config.getSeverity());
		this.setText(m_config.getText());
	}
	
	void deinit()
	{
		m_targetElement = null;
		m_config = null;
	}
	
	public Element getTargetElement()
	{
		return m_targetElement;
	}
	
	public bhToolTipConfig getConfig()
	{
		return m_config;
	}
	
	private void setSeverity(bhE_ToolTipMood severity)
	{
		String color = null;
		
		switch( severity )
		{
			case NORMAL:
			{
				color = "black";
				break;
			}
			
			case OOPS:
			{
				color = "red";
				break;
			}
			
			case PAT_ON_BACK:
			{
				color = "green";
				break;
			}
		}
		
		if( color != null )
		{
			this.getElement().getStyle().setColor(color);
		}
	}
}
