package b33hive.client.app;

import b33hive.client.entities.bhCamera;
import b33hive.client.input.bhClickManager;
import b33hive.client.managers.bhCameraManager;
import b33hive.client.managers.bhCellAddressManager;
import b33hive.client.managers.bhCellCodeManager;
import b33hive.client.managers.bhClientAccountManager;
import b33hive.client.managers.bhGridManager;
import b33hive.client.managers.bhUserManager;
import b33hive.client.navigation.bhMasterNavigator;
import b33hive.client.thirdparty.captcha.bhRecaptchaWrapper;
import b33hive.client.transaction.bhClientTransactionManager;
import b33hive.client.ui.tabs.code.bhCellSandbox;
import b33hive.client.ui.tooltip.bhToolTipManager;
import b33hive.shared.app.bh;

public class bh_c extends bh
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
}
