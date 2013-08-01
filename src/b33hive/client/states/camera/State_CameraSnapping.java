package com.b33hive.client.states.camera;

import com.b33hive.client.app.bhS_ClientApp;
import com.b33hive.client.entities.bhCamera;
import com.b33hive.client.managers.bhCellBuffer;
import com.b33hive.client.entities.bhBufferCell;
import com.b33hive.client.managers.bhCellBufferManager;
import com.b33hive.client.entities.bhClientGrid;
import com.b33hive.client.entities.bhClientUser;
import com.b33hive.client.entities.bhE_CellNuke;
import com.b33hive.client.input.bhBrowserHistoryManager;
import com.b33hive.client.managers.bhCellAddressManager;
import com.b33hive.client.managers.bhCellCodeManager;
import com.b33hive.client.managers.bhF_BufferUpdateOption;
import com.b33hive.client.states.StateMachine_Base;
import com.b33hive.client.states.StateMachine_Base.OnGridResize;
import com.b33hive.client.states.camera.StateMachine_Camera.CameraManager;
import com.b33hive.client.states.camera.StateMachine_Camera.SetCameraViewSize;
import com.b33hive.client.states.code.StateMachine_EditingCode;
import com.b33hive.client.structs.bhCellCodeCache;
import com.b33hive.client.structs.bhI_LocalCodeRepository;
import com.b33hive.client.structs.bhLocalCodeRepositoryWrapper;
import com.b33hive.client.transaction.bhE_TransactionAction;
import com.b33hive.shared.app.bhS_App;
import com.b33hive.shared.debugging.bhU_Debug;
import com.b33hive.shared.entities.bhE_CodeType;
import com.b33hive.shared.statemachine.bhA_Action;

import com.b33hive.shared.statemachine.bhA_State;
import com.b33hive.shared.statemachine.bhI_StateEventListener;
import com.b33hive.shared.statemachine.bhA_StateConstructor;
import com.b33hive.shared.statemachine.bhStateEvent;
import com.b33hive.shared.structs.bhCellAddress;
import com.b33hive.shared.structs.bhCellAddressMapping;
import com.b33hive.shared.structs.bhGridCoordinate;
import com.b33hive.shared.structs.bhPoint;

public class State_CameraSnapping extends bhA_State implements bhI_StateEventListener
{
	public static class Constructor extends bhA_StateConstructor
	{
		public Constructor(bhGridCoordinate targetCoordinate)
		{
			m_targetCoordinate = targetCoordinate;
			m_targetAddress = null;
		}
		
		public Constructor(bhGridCoordinate targetCoordinate, bhCellAddress targetAddress)
		{
			m_targetCoordinate = targetCoordinate;
			m_targetAddress = targetAddress;
		}
		
		private final bhGridCoordinate m_targetCoordinate;
		private final bhCellAddress m_targetAddress;
	}
	
	private bhCellAddress m_targetAddress = null;
	
	private final bhGridCoordinate m_targetGridCoordinate = new bhGridCoordinate();
	private final bhPoint m_utilPoint = new bhPoint();
	
	private final bhCamera m_snapCamera = new bhCamera();
	private final bhCellBufferManager m_snapBufferManager = new bhCellBufferManager();
	
	//--- DRK > This is exposed externally so the main cell buffer manager can extract cell code that m_internalCodeRepo has.
	private final bhLocalCodeRepositoryWrapper m_externalCompiledStaticCodeRepo = new bhLocalCodeRepositoryWrapper();
	
	//--- DRK > This is used internally to help populate m_snapBufferManager without having to hit the server.
	private final bhLocalCodeRepositoryWrapper m_internalCodeRepo = new bhLocalCodeRepositoryWrapper();
	
	private boolean m_countTowardsHistory = true;
	
	private boolean m_hasRequestedSourceCode = false;
	private boolean m_hasRequestedCompiledCode = false;
	
	public State_CameraSnapping()
	{
		m_externalCompiledStaticCodeRepo.addSource(bhClientUser.getInstance());
		m_externalCompiledStaticCodeRepo.addSource(m_snapBufferManager);
		m_externalCompiledStaticCodeRepo.addSource(bhCellCodeCache.getInstance());
		
		m_internalCodeRepo.addSource(bhClientUser.getInstance());
		m_internalCodeRepo.addSource(bhCellBufferManager.getInstance());
		m_internalCodeRepo.addSource(bhCellCodeCache.getInstance());
	}

	void updateGridCoordinate(bhGridCoordinate targetCoordinate, bhCellAddress targetAddress_nullable)
	{
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
		m_targetGridCoordinate.calcCenterPoint(m_utilPoint, 1);
		m_utilPoint.incY(-bhS_ClientApp.CELL_HUD_HEIGHT/2);
		
		m_snapCamera.getPosition().copy(m_utilPoint);
		
		//--- This "nuke" used to get rid of everything, but that sort of broke the UI experience,
		//--- and most of the time resulted in too much network traffic, so now only errors are cleared.
		//--- User can still get guaranteed fresh version from server using refresh button.
		bhE_CellNuke nukeType = bhE_CellNuke.ERRORS_ONLY;
		bhCellCodeManager.getInstance().nukeFromOrbit(targetCoordinate, nukeType);
		
		//--- DRK > Not flushing populator here because requestCodeForTargetCell() will do it for us.
		this.updateSnapBufferManager(false);
		
		this.m_hasRequestedSourceCode = false;
		this.m_hasRequestedCompiledCode = false;
		
		if( m_targetAddress == null )
		{
			//--- DRK > Try to get address ourselves...could turn up null.
			bhCellAddressMapping mapping = new bhCellAddressMapping(m_targetGridCoordinate);
			bhCellAddressManager.getInstance().getCellAddress(mapping, bhE_TransactionAction.QUEUE_REQUEST);
		}
		
		requestCodeForTargetCell();
		
		CameraManager manager = (CameraManager) ((StateMachine_Camera) getParent()).getCameraManager();
		manager.internal_setTargetPosition(m_utilPoint, false);
	}

	private void requestCodeForTargetCell()
	{
		bhCellCodeManager populator = bhCellCodeManager.getInstance();
		
		if( m_hasRequestedSourceCode && m_hasRequestedCompiledCode )
		{
			populator.flush(); // just in case something else needs flushing...harmless if not.
			
			return;
		}
		
		bhCellBuffer displayBuffer = m_snapBufferManager.getDisplayBuffer();
		
		//--- DRK > Not entering here should be an impossible case, but avoid a null pointer exception just to be sure.
		if( displayBuffer.isInBoundsAbsolute(m_targetGridCoordinate) )
		{
			bhBufferCell cell = displayBuffer.getCellAtAbsoluteCoord(m_targetGridCoordinate);
			bhI_LocalCodeRepository localCodeRepo = m_internalCodeRepo;

			if( !m_hasRequestedSourceCode )
			{
				//--- DRK > As an optimization, we only retrieve the source html if we're in the html state.
				if( bhA_State.isForegrounded(StateMachine_EditingCode.class) )
				{
					populator.populateCell(cell, localCodeRepo, 1, false, true, bhE_CodeType.SOURCE);
					
					m_hasRequestedSourceCode = true;
				}
			}
			
			if( !m_hasRequestedCompiledCode )
			{
				populator.populateCell(cell, localCodeRepo, 1, false, true, bhE_CodeType.COMPILED);

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
		bhI_LocalCodeRepository htmlSource = m_internalCodeRepo;
		
		int options = bhF_BufferUpdateOption.COMMUNICATE_WITH_SERVER;
		if( flushPopulator )
		{
			options |= bhF_BufferUpdateOption.FLUSH_CELL_POPULATOR;
		}
		
		m_snapBufferManager.update(bhClientGrid.getInstance(), m_snapCamera, htmlSource, options);
	}
	
	bhI_LocalCodeRepository getCompiledStaticHtmlSource()
	{
		return m_externalCompiledStaticCodeRepo;
	}
	
	bhI_LocalCodeRepository getHtmlSourceForTargetCell()
	{
		return m_snapBufferManager;
	}
	
	public bhGridCoordinate getTargetCoordinate()
	{
		return m_targetGridCoordinate;
	}
	
	bhCellAddress getTargetAddress()
	{
		return m_targetAddress;
	}
	
	@Override
	protected void didEnter(bhA_StateConstructor constructor)
	{
		StateMachine_Camera machine = getParent();
		
		Constructor castConstructor = (Constructor) constructor;
		
		bhCellBufferManager.registerInstance(m_snapBufferManager);
		
		m_targetGridCoordinate.set(-1, -1);
		
		bhCamera camera = bhCamera.getInstance();
		
		m_snapCamera.setViewRect(camera.getViewWidth(), camera.getViewHeight());
		
		m_hasRequestedSourceCode = false;
		m_hasRequestedCompiledCode = false;

		updateGridCoordinate(castConstructor.m_targetCoordinate, castConstructor.m_targetAddress);
	}
	
	@Override
	protected void didForeground(Class<? extends bhA_State> revealingState, Object[] argsFromRevealingState)
	{
		
	}
	
	@Override
	protected void update(double timeStep)
	{
		StateMachine_Camera machine = ((StateMachine_Camera) getParent());
		if ( machine.getCameraManager().isCameraAtRest() )
		{
			bhBufferCell testCell = bhCellBufferManager.getInstance().getDisplayBuffer().getCellAtAbsoluteCoord(m_targetGridCoordinate);
			
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
	protected void willBackground(Class<? extends bhA_State> blockingState)
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
		
		bhCellBufferManager.unregisterInstance(m_snapBufferManager);
	}

	@Override
	public void onStateEvent(bhStateEvent event)
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
					bhCamera camera = bhCamera.getInstance();
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


