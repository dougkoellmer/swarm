package swarm.client.view.tabs.account;

import swarm.client.view.tooltip.smE_ToolTipType;
import swarm.client.view.tooltip.smToolTipConfig;
import swarm.client.view.tooltip.smToolTipManager;
import swarm.client.view.widget.smDefaultButton;

import com.google.gwt.dom.client.Style.Unit;

public class smSignInOrUpButton extends smDefaultButton
{
	smSignInOrUpButton()
	{
		this.getElement().getStyle().setMarginRight(2, Unit.PX);
	}
}
