package swarm.client.app;

import java.util.ArrayList;
import java.util.Iterator;

import swarm.client.code.CompilerErrorMessageGenerator;
import swarm.client.entities.Camera;
import swarm.client.input.ClickManager;
import swarm.client.managers.CameraManager;
import swarm.client.managers.CellAddressManager;
import swarm.client.managers.CellBufferManager;
import swarm.client.managers.CellCodeManager;
import swarm.client.managers.CellSizeManager;
import swarm.client.managers.ClientAccountManager;
import swarm.client.managers.GridManager;
import swarm.client.managers.UserManager;
import swarm.client.structs.CellCodeCache;
import swarm.client.thirdparty.captcha.RecaptchaWrapper;
import swarm.client.transaction.ClientTransactionManager;
import swarm.client.view.sandbox.SandboxManager;
import swarm.client.view.tooltip.ToolTipManager;
import swarm.shared.app.BaseAppContext;
import swarm.shared.time.I_TimeSource;

public class AppContext extends BaseAppContext
{
	public ClientAppConfig config;
	public PlatformInfo platformInfo;
	public CellAddressManager addressMngr;
	public UserManager userMngr;
	public ClientAccountManager accountMngr;
	public ClientTransactionManager txnMngr;
	public CellCodeManager codeMngr;
	public CellSizeManager cellSizeMngr;
	public GridManager gridMngr;
	public SandboxManager cellSandbox;
	public CameraManager cameraMngr;
	public CellCodeCache codeCache;
	public CellBufferManager cellBufferMngr;
	public CompilerErrorMessageGenerator compilerErrorMsgGenerator;
	public I_TimeSource timeSource;
	
	private final ArrayList<CellBufferManager> m_registeredBuffers = new ArrayList<CellBufferManager>();
	private final CellBufferManager.Iterator m_iterator = new CellBufferManager.Iterator(m_registeredBuffers);
	
	public void registerBufferMngr(CellBufferManager instance)
	{
		m_registeredBuffers.add(instance);
	}
	
	public CellBufferManager.Iterator getRegisteredBufferMngrs()
	{
		m_iterator.reset();
		
		return m_iterator;
	}
	
	public void unregisterBufferMngr(CellBufferManager instance)
	{
		for( int i = m_registeredBuffers.size()-1; i >= 0; i-- )
		{
			if( m_registeredBuffers.get(i) == instance )
			{
				m_registeredBuffers.remove(i);
				
				return;
			}
		}
	}
}
