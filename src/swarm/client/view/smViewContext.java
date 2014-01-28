package swarm.client.view;

import swarm.client.app.smAppContext;
import swarm.client.app.smClientAppConfig;
import swarm.client.input.smClickManager;
import swarm.client.navigation.smBrowserNavigator;
import swarm.client.navigation.smMouseNavigator;
import swarm.client.navigation.smScrollNavigator;
import swarm.client.thirdparty.captcha.smRecaptchaWrapper;
import swarm.client.view.cell.smAlertManager;
import swarm.client.view.cell.smI_CellSpinnerFactory;
import swarm.client.view.cell.smVisualCellManager;
import swarm.client.view.tooltip.smToolTipManager;
import swarm.shared.statemachine.smStateContext;

public class smViewContext
{
	public smAppContext appContext;
	public smStateContext stateContext;
	
	public smClientAppConfig appConfig;
	public smViewConfig viewConfig;
	
	public smSplitPanel splitPanel;
	public smVisualCellManager cellMngr;
	public smClickManager clickMngr;
	public smToolTipManager toolTipMngr;
	public smRecaptchaWrapper recaptchaWrapper;
	public smAlertManager alertMngr;
	public smConsoleBlocker consoleBlocker;
	public smI_CellSpinnerFactory spinnerFactory;
	
	public smBrowserNavigator browserNavigator;
	public smMouseNavigator mouseNavigator;
	public smScrollNavigator scrollNavigator;
}
