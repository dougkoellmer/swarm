package swarm.client.states.camera;

import java.util.logging.Logger;

import swarm.client.app.sm_c;
import swarm.client.entities.smCamera;
import swarm.client.managers.smCellBuffer;
import swarm.client.entities.smBufferCell;
import swarm.client.managers.smCellBufferManager;
import swarm.client.entities.smA_ClientUser;
import swarm.client.entities.smE_CellNuke;
import swarm.client.input.smBrowserHistoryManager;
import swarm.client.managers.smCellAddressManager;
import swarm.client.managers.smCellCodeManager;
import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smF_BufferUpdateOption;
import swarm.client.managers.smUserManager;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.StateMachine_Base.OnGridResize;
import swarm.client.states.camera.StateMachine_Camera.CameraManager;
import swarm.client.states.camera.StateMachine_Camera.SetCameraViewSize;
import swarm.client.states.code.StateMachine_EditingCode;
import swarm.client.structs.smCellCodeCache;
import swarm.client.structs.smI_LocalCodeRepository;
import swarm.client.structs.smLocalCodeRepositoryWrapper;
import swarm.client.transaction.smE_TransactionAction;
import swarm.shared.app.smS_App;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smA_Grid;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.statemachine.smA_Action;

import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smI_StateEventListener;
import swarm.shared.statemachine.smA_StateConstructor;
import swarm.shared.statemachine.smStateEvent;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCellAddressMapping;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;


public class State_CameraSnapping extends smA_State implements smI_StateEventListener
{
	private static final Logger s_logger = Logger.getLogger(State_CameraSnapping.class.getName());
	
	public static class Constructor extends smA_StateConstructor
	{
		public Constructor(smGridCoordinate targetCoordinate)
		{
			m_targetCoordinate = targetCoordinate;
			m_targetAddress = null;
		}
		
		public Constructor(smGridCoordinate targetCoordinate, smCellAddress targetAddress)
		{
			m_targetCoordinate = targetCoordinate;
			m_targetAddress = targetAddress;
		}
		
		private final smGridCoordinate m_targetCoordinate;
		private final smCellAddress m_targetAddress;
	}
	
	private smCellAddress m_targetAddress = null;
	
	private final smGridCoordinate m_targetGridCoordinate = new smGridCoordinate();
	private final smPoint m_utilPoint = new smPoint();
	
	private final smCamera m_snapCamera = new smCamera();
	private final smCellBufferManager m_snapBufferManager = new smCellBufferManager();
	
	//--- DRK > This is exposed externally so the main cell buffer manager can extract cell code that m_internalCodeRepo has.
	private final smLocalCodeRepositoryWrapper m_externalCompiledStaticCodeRepo = new smLocalCodeRepositoryWrapper();
	
	//--- DRK > This is used internally to help populate m_snapBufferManager without having to hit the server.
	private final smLocalCodeRepositoryWrapper m_internalCodeRepo = new smLocalCodeRepositoryWrapper();
	
	private boolean m_countTowardsHistory = true;
	
	private boolean m_hasRequestedSourceCode = false;
	private boolean m_hasRequestedCompiledCode = false;
	
	private final double m_cellHudHeight;
	
	private double m_snapProgressBase;
	
	public State_CameraSnapping(double cellHudHeight)
	{
		m_cellHudHeight = cellHudHeight;
		
		bhUserManager userManager = sm_c.userMngr;
		smA_ClientUser user = userManager.getUser();
		
		m_externalCompiledStaticCodeRepo.addSource(user);
		m_externalCompiledStaticCodeRepo.addSource(m_snapBufferManager);
		m_externalCompiledStaticCodeRepo.addSource(sm_c.codeCache);
		
		m_internalCodeRepo.addSource(user);
		m_internalCodeRepo.addSource(smCellBufferManager.getInstance());
		m_internalCodeRepo.addSource(sm_c.codeCache);
	}

	void updateGridCoordinate(smGridCoordinate targetCoordinate, smCellAddress targetAddress_nullable)
	{
		smA_Grid grid = sm_c.gridMngr.getGrid();
		
		m_targetAddress = targetAddress_nullable;
		
		if( m_targetGridCoordinate.isEqualTo(targetCoordinate) )
		{
			return;
		}
		
		//--- DRK > This case implies we changed snap target mid-flight, which cancels out the fact
		//---		that the user might have initially pressed forward or back to initiate this snap state.
		if( !this.isEntering() )
		{
			m_countTowardsHistory = true;
		}
		
		m_targetGridCoordinate.copy(targetCoordinate);
		m_targetGridCoordinate.calcCenterPoint(m_utilPoint, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
		m_utilPoint.incY(-m_cellHudHeight/2);
		
		m_snapCamera.getPosition().copy(m_utilPoint);
		
		//--- This "nuke" used to get rid of everything, but that sort of broke the UI experience,
		//--- and most of the time resulted in too much network traffic, so now only errors are cleared.
		//--- User can still get guaranteed fresh version from server using refresh button.
		smE_CellNuke nukeType = smE_CellNuke.ERRORS_ONLY;
		smCellCodeManager.getInstance().nukeFromOrbit(targetCoordinate, nukeType);
		
		//--- DRK > Not flushing populator here because requestCodeForTargetCell() will do it for us.
		this.updateSnapBufferManager(false);
		
		this.m_hasRequestedSourceCode = false;
		this.m_hasRequestedCompiledCode = false;
		
		if( m_targetAddress == null )
		{
			//--- DRK > Try to get address ourselves...could turn up null.
			smCellAddressMapping mapping = new smCellAddressMapping(m_targetGridCoordinate);
			smCellAddressManager addyManager = sm_c.addressMngr;
			addyManager.getCellAddress(mapping, smE_TransactionAction.QUEUE_REQUEST);
		}
		
		requestCodeForTargetCell();
		
		m_snapProgressBase = this.getOverallSnapProgress();
		
		CameraManager manager = (CameraManager) ((StateMachine_Camera) getParent()).getCameraManager();
		manager.internal_setTargetPosition(m_utilPoint, false);
	}
	
	public double getOverallSnapProgress()
	{
		CameraManager manager = (CameraManager) ((StateMachine_Camera) getParent()).getCameraManager();
		
		//s_logger.severe("snap prog base: " + m_snapProgressBase + "   snap prog: " + manager.getSnapProgress());
		return m_snapProgressBase + (1-m_snapProgressBase)*manager.getSnapProgress();
	}

	private void requestCodeForTargetCell()
	{
		smCellCodeManager populator = smCellCodeManager.getInstance();
		
		if( m_hasRequestedSourceCode && m_hasRequestedCompiledCode )
		{
			populator.flush(); // just in case something else needs flushing...harmless if not.
			
			return;
		}
		
		smCellBuffer displayBuffer = m_snapBufferManager.getDisplayBuffer();
		
		//--- DRK > Not entering here should be an impossible case, but avoid a null pointer exception just to be sure.
		if( displayBuffer.isInBoundsAbsolute(m_targetGridCoordinate) )
		{
			smBufferCell cell = displayBuffer.getCellAtAbsoluteCoord(m_targetGridCoordinate);
			smI_LocalCodeRepository localCodeRepo = m_internalCodeRepo;

			if( !m_hasRequestedSourceCode )
			{
				//--- DRK > As an optimization, we only retrieve the source html if we're in the html state.
				if( smA_State.isForegrounded(StateMachine_EditingCode.class) )
				{
					populator.populateCell(cell, localCodeRepo, 1, false, true, smE_CodeType.SOURCE);
					
					m_hasRequestedSourceCode = true;
				}
			}
			
			if( !m_hasRequestedCompiledCode )
			{
				populator.populateCell(cell, localCodeRepo, 1, false, true, smE_CodeType.COMPILED);

				//--- DRK > NOTE that COMPILED_STATIC html will be retrieved implicitly because we update the buffer manager
				//---		itself before we get into this method...it will be in the same batch too, automatically...cool.
				
				m_hasRequestedCompiledCode = true;
			}
		}
		else
		{
			bhU_Debug.ASSERT(false, "requestCodeForTargetCell1");
			
			m_hasRequestedSourceCode = true;
			m_hasRequestedCompiledCode = true;
		}
		
		populator.flush();
	}
	
	private void updateSnapBufferManager(boolean flushPopulator)
	{
		smA_Grid grid = sm_c.gridMngr.getGrid();
		smI_LocalCodeRepository htmlSource = m_internalCodeRepo;
		
		int options = bhF_BufferUpdateOption.COMMUNICATE_WITH_SERVER;
		if( flushPopulator )
		{
			options |= bhF_BufferUpdateOption.FLUSH_CELL_POPULATOR;
		}
		
		m_snapBufferManager.update(grid, m_snapCamera, htmlSource, options);
	}
	
	smI_LocalCodeRepository getCompiledStaticHtmlSource()
	{
		return m_externalCompiledStaticCodeRepo;
	}
	
	smI_LocalCodeRepository getHtmlSourceForTargetCell()
	{
		return m_snapBufferManager;
	}
	
	public smGridCoordinate getTargetCoordinate()
	{
		return m_targetGridCoordinate;
	}
	
	smCellAddress getTargetAddress()
	{
		return m_targetAddress;
	}
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
		StateMachine_Camera machine = getParent();
		
		Constructor castConstructor = (Constructor) constructor;
		
		smCellBufferManager.registerInstance(m_snapBufferManager);
		
		m_targetGridCoordinate.set(-1, -1);
		
		bhCamera camera = sm_c.camera;
		
		m_snapCamera.setViewRect(camera.getViewWidth(), camera.getViewHeight());
		
		m_hasRequestedSourceCode = false;
		m_hasRequestedCompiledCode = false;
		
		m_snapProgressBase = 0;

		updateGridCoordinate(castConstructor.m_targetCoordinate, castConstructor.m_targetAddress);
	}
	
	@Override
	protected void didForeground(Class<? extends smA_State> revealingState, Object[] argsFromRevealingState)
	{
		
	}
	
	@Override
	protected void update(double timeStep)
	{
		StateMachine_Camera machine = ((StateMachine_Camera) getParent());
		if ( machine.getCameraManager().isCameraAtRest() )
		{
			smBufferCell testCell = smCellBufferManager.getInstance().getDisplayBuffer().getCellAtAbsoluteCoord(m_targetGridCoordinate);
			
			State_ViewingCell.Constructor constructor = new State_ViewingCell.Constructor(testCell);
			
			if( m_targetAddress != null )
			{
				//--- DRK > This cell might have already been given its address by the address manager,
				//---		but it doesn't hurt to do this twice.
				testCell.onAddressFound(m_targetAddress);
			}
			
			machine_setState(getParent(), State_ViewingCell.class, constructor);
			
			return;
		}
	}
	
	@Override
	protected void willBackground(Class<? extends smA_State> blockingState)
	{
		
	}
	
	@Override
	protected void willExit()
	{
		m_targetAddress = null;
		
		m_targetGridCoordinate.set(-1, -1);
		
		//--- DRK > Might implement a more elegant expiry system for cells/code hanging out in the snap buffer,
		//---		but for now it just gets nuked every time we stop snapping.
		//---		Code will generally hang out in the LRU cache anyway for a little bit, and cells are kept
		//---		in a pool, so not a huge deal as far as thrashing memory or anything.
		m_snapBufferManager.drain();
		
		smCellBufferManager.unregisterInstance(m_snapBufferManager);
	}

	@Override
	public void onStateEvent(smStateEvent event)
	{
		switch(event.getType())
		{
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof StateMachine_EditingCode )
				{
					this.requestCodeForTargetCell();
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == StateMachine_Camera.SetCameraViewSize.class )
				{
					bhCamera camera = sm_c.camera;
					m_snapCamera.setViewRect(camera.getViewWidth(), camera.getViewHeight());
					
					this.updateSnapBufferManager(true);
				}
				else if( event.getAction() == StateMachine_Base.OnGridResize.class )
				{
					this.updateSnapBufferManager(true);
				}
				else if( event.getAction() == StateMachine_Camera.OnAddressResponse.class )
				{
					StateMachine_Camera.OnAddressResponse.Args args = event.getActionArgs();
					
					if( args.getType() == StateMachine_Camera.OnAddressResponse.E_Type.ON_FOUND )
					{
						m_targetAddress = args.getAddress();
					}
				}
				
				break;
			}
		}
	}
}