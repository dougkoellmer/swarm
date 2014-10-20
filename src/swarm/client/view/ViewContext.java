package swarm.client.view;

import swarm.client.app.AppContext;
import swarm.client.app.ClientAppConfig;
import swarm.client.input.ClickManager;
import swarm.client.navigation.BrowserNavigator;
import swarm.client.navigation.HistoryStateManager;
import swarm.client.navigation.MouseNavigator;
import swarm.client.navigation.ScrollNavigator;
import swarm.client.thirdparty.captcha.RecaptchaWrapper;
import swarm.client.view.cell.AlertManager;
import swarm.client.view.cell.I_CellSpinnerFactory;
import swarm.client.view.cell.VisualCellManager;
import swarm.client.view.tooltip.ToolTipManager;
import swarm.shared.statemachine.StateContext;

public class ViewContext
{
	public AppContext appContext;
	public StateContext stateContext;
	
	public ClientAppConfig appConfig;
	public ViewConfig config;
	
	public SplitPanel splitPanel;
	public VisualCellManager cellMngr;
	public ClickManager clickMngr;
	public ToolTipManager toolTipMngr;
	public RecaptchaWrapper recaptchaWrapper;
	public AlertManager alertMngr;
	public ConsoleBlocker consoleBlocker;
	public I_CellSpinnerFactory spinnerFactory;
	
	public BrowserNavigator browserNavigator;
	public MouseNavigator mouseNavigator;
	public ScrollNavigator scrollNavigator;
}
