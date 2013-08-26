package swarm.client.app;

import swarm.client.entities.smCamera;
import swarm.client.input.smClickManager;
import swarm.client.managers.smCameraManager;
import swarm.client.managers.smCellAddressManager;
import swarm.client.managers.smCellCodeManager;
import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smGridManager;
import swarm.client.managers.smUserManager;
import swarm.client.navigation.smMasterNavigator;
import swarm.client.structs.smCellCodeCache;
import swarm.client.thirdparty.captcha.smRecaptchaWrapper;
import swarm.client.transaction.smClientTransactionManager;
import swarm.client.ui.tabs.code.smCellSandbox;
import swarm.client.ui.tooltip.smToolTipManager;
import swarm.shared.app.sm;

public class sm_c extends sm
{
	public static bhClickManager clickMngr;
	public static bhPlatformInfo platformInfo;
	public static smCellAddressManager addressMngr;
	public static bhUserManager userMngr;
	public static bhClientAccountManager accountMngr;
	public static bhClientTransactionManager txnMngr;
	public static smCellCodeManager codeMngr;
	public static bhGridManager gridMngr;
	public static bhRecaptchaWrapper recaptchaWrapper;
	public static bhToolTipManager toolTipMngr;
	public static smCellSandbox cellSandbox;
	public static bhMasterNavigator navigator;
	public static bhCamera camera;
	public static bhCameraManager cameraMngr;
	public static smCellCodeCache codeCache;
	public static smA_ClientApp app;
}
