package swarm.client.view.tabs.account;

import swarm.client.view.tooltip.E_ToolTipType;
import swarm.client.view.tooltip.ToolTipConfig;
import swarm.client.view.tooltip.ToolTipManager;
import swarm.client.view.widget.DefaultButton;

import com.google.gwt.dom.client.Style.Unit;

public class SignInOrUpButton extends DefaultButton
{
	SignInOrUpButton()
	{
		this.getElement().getStyle().setMarginRight(2, Unit.PX);
	}
}
