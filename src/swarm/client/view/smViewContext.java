package swarm.client.view;

import swarm.client.app.smAppContext;
import swarm.client.input.smClickManager;
import swarm.client.thirdparty.captcha.smRecaptchaWrapper;
import swarm.client.view.cell.smVisualCellManager;
import swarm.client.view.tooltip.smToolTipManager;
import swarm.shared.statemachine.smStateContext;

public class smViewContext
{
	public smAppContext appContext;
	public smStateContext stateContext;
	
	public smSplitPanel splitPanel;
	public smVisualCellManager cellMngr;
	public smClickManager clickMngr;
	public smToolTipManager toolTipMngr;
	public smRecaptchaWrapper recaptchaWrapper;
	
}
