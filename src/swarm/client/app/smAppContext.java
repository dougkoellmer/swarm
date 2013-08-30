package swarm.client.app;

import swarm.client.entities.smCamera;
import swarm.client.input.smClickManager;
import swarm.client.managers.smCameraManager;
import swarm.client.managers.smCellAddressManager;
import swarm.client.managers.smCellBufferManager;
import swarm.client.managers.smCellCodeManager;
import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smGridManager;
import swarm.client.managers.smUserManager;
import swarm.client.navigation.smMasterNavigator;
import swarm.client.structs.smCellCodeCache;
import swarm.client.thirdparty.captcha.smRecaptchaWrapper;
import swarm.client.transaction.smClientTransactionManager;
import swarm.client.view.tabs.code.smCellSandbox;
import swarm.client.view.tooltip.smToolTipManager;
import swarm.shared.app.smSharedAppContext;

public class smAppContext extends smSharedAppContext
{
	public smPlatformInfo platformInfo;
	public smCellAddressManager addressMngr;
	public smUserManager userMngr;
	public smClientAccountManager accountMngr;
	public smClientTransactionManager txnMngr;
	public smCellCodeManager codeMngr;
	public smGridManager gridMngr;
	public smCellSandbox cellSandbox;
	public smCameraManager cameraMngr;
	public smCellCodeCache codeCache;
	public smCellBufferManager cellBufferMngr;
}
