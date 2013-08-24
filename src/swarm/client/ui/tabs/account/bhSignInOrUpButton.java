package swarm.client.ui.tabs.account;

import swarm.client.ui.tooltip.bhE_ToolTipType;
import swarm.client.ui.tooltip.bhToolTipConfig;
import swarm.client.ui.tooltip.bhToolTipManager;
import swarm.client.ui.widget.bhDefaultButton;
import com.google.gwt.dom.client.Style.Unit;

public class bhSignInOrUpButton extends bhDefaultButton
{
	bhSignInOrUpButton()
	{
		this.getElement().getStyle().setMarginRight(2, Unit.PX);
	}
}
