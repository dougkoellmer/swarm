package swarm.client.app;

import swarm.client.entities.bhCamera;
import swarm.client.input.bhClickManager;
import swarm.client.managers.bhCameraManager;
import swarm.client.managers.bhCellAddressManager;
import swarm.client.managers.bhCellCodeManager;
import swarm.client.managers.bhClientAccountManager;
import swarm.client.managers.bhGridManager;
import swarm.client.managers.bhUserManager;
import swarm.client.navigation.bhMasterNavigator;
import swarm.client.structs.bhCellCodeCache;
import swarm.client.thirdparty.captcha.bhRecaptchaWrapper;
import swarm.client.transaction.bhClientTransactionManager;
import swarm.client.ui.tabs.code.bhCellSandbox;
import swarm.client.ui.tooltip.bhToolTipManager;
import swarm.shared.app.sm;

public class sm_c extends sm
{
	public static bhClickManager clickMngr;
	public static bhPlatformInfo platformInfo;
	public static bhCellAddressManager addressMngr;
	public static bhUserManager userMngr;
	public static bhClientAccountManager accountMngr;
	public static bhClientTransactionManager txnMngr;
	public static bhCellCodeManager codeMngr;
	public static bhGridManager gridMngr;
	public static bhRecaptchaWrapper recaptchaWrapper;
	public static bhToolTipManager toolTipMngr;
	public static bhCellSandbox cellSandbox;
	public static bhMasterNavigator navigator;
	public static bhCamera camera;
	public static bhCameraManager cameraMngr;
	public static bhCellCodeCache codeCache;
	public static bhA_ClientApp app;
}
