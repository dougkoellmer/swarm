package swarm.client.view.tooltip;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Label;

public class ToolTip extends Label
{
	private Element m_targetElement = null;
	private ToolTipConfig m_config;
	
	public ToolTip()
	{
		this.addStyleName("sm_tool_tip");
		this.setWordWrap(false);
	}
	
	void init(Element targetElement, ToolTipConfig config)
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
	
	public ToolTipConfig getConfig()
	{
		return m_config;
	}
	
	private void setSeverity(E_ToolTipMood severity)
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
