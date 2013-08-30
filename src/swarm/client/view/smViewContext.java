package swarm.client.view;

import swarm.client.app.smAppContext;
import swarm.client.input.smClickManager;
import swarm.client.thirdparty.captcha.smRecaptchaWrapper;
import swarm.client.view.cell.smVisualCellManager;
import swarm.client.view.tooltip.smToolTipManager;

public class smViewContext
{
	public smSplitPanel splitPanel;
	public smVisualCellManager cellMngr;
	public smClickManager clickMngr;
	public smToolTipManager toolTipMngr;
	public smRecaptchaWrapper recaptchaWrapper;
	
}
