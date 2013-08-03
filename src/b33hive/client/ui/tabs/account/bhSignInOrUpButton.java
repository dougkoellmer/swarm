package b33hive.client.ui.tabs.account;

import b33hive.client.ui.tooltip.bhE_ToolTipType;
import b33hive.client.ui.tooltip.bhToolTipConfig;
import b33hive.client.ui.tooltip.bhToolTipManager;
import b33hive.client.ui.widget.bhDefaultButton;
import com.google.gwt.dom.client.Style.Unit;

public class bhSignInOrUpButton extends bhDefaultButton
{
	bhSignInOrUpButton()
	{
		this.getElement().getStyle().setMarginRight(2, Unit.PX);
	}
}
