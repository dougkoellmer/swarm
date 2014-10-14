package swarm.client.states.camera;

import java.util.logging.Logger;

import swarm.client.app.AppContext;
import swarm.client.entities.Camera;
import swarm.client.managers.CellBuffer;
import swarm.client.entities.BufferCell;
import swarm.client.managers.CellBufferManager;
import swarm.client.entities.A_ClientUser;
import swarm.client.entities.ClientGrid;
import swarm.client.entities.E_CellNuke;
import swarm.client.input.BrowserHistoryManager;
import swarm.client.managers.CameraManager;
import swarm.client.managers.CellAddressManager;
import swarm.client.managers.CellCodeManager;
import swarm.client.managers.ClientAccountManager;
import swarm.client.managers.F_BufferUpdateOption;
import swarm.client.managers.UserManager;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.StateMachine_Base.OnGridUpdate;
import swarm.client.states.code.StateMachine_EditingCode;
import swarm.client.structs.CellCodeCache;
import swarm.client.structs.I_LocalCodeRepository;
import swarm.client.structs.LocalCodeRepositoryWrapper;
import swarm.client.transaction.E_TransactionAction;
import swarm.shared.app.S_CommonApp;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.A_Grid;
import swarm.shared.entities.E_CodeType;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.I_StateEventListener;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.StateEvent;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;

public class State_CameraSnapping extends A_State implements I_StateEventListener, I_State_SnappingOrViewing
{
	private static final GridCoordinate INVALID = new GridCoordinate(-1, -1);
	
	private static final Logger s_logger = Logger.getLogger(State_CameraSnapping.class.getName());
	
	public static class Constructor extends StateArgs
	{
		private GridCoordinate m_targetCoordinate;
		private CellAddress m_targetAddress;
		private Point m_targetPoint;
		
		public Constructor(GridCoordinate targetCoordinate, CellAddress targetAddress_nullable, Point targetPoint)
		{
			m_targetCoordinate = targetCoordinate;
			m_targetAddress = targetAddress_nullable;
			m_targetPoint = targetPoint;
		}
		
		private void clear()
		{
			m_targetCoordinate = null;
			m_targetAddress = null;
			m_targetPoint = null;
		}
	}
	
	private CellAddress m_targetAddress = null;
	
	private final GridCoordinate m_targetGridCoordinate = new GridCoordinate();
	
	private final Camera m_snapCamera = new Camera(null);
	private final CellBufferManager m_snapBufferManager;
	
	//--- DRK > This is exposed externally so the main cell buffer manager can extract cell code that m_internalCodeRepo has.
	private final LocalCodeRepositoryWrapper m_externalCompiledStaticCodeRepo = new LocalCodeRepositoryWrapper();
	
	//--- DRK > This is used internally to help populate m_snapBufferManager without having to hit the server.
	private final LocalCodeRepositoryWrapper m_internalCodeRepo = new LocalCodeRepositoryWrapper();
	
	private boolean m_hasRequestedSourceCode = false;
	private boolean m_hasRequestedCompiledCode = false;
	
	private double m_snapProgressBase;
	private final AppContext m_appContext;
	
	private BufferCell m_targetCell = null;
	
	public State_CameraSnapping(AppContext appContext, double cellHudHeight)
	{
		m_appContext = appContext;
		
		m_snapBufferManager = new CellBufferManager(m_appContext.codeMngr, m_appContext.cellSizeMngr, 1);
		
		UserManager userManager = m_appContext.userMngr;
		A_ClientUser user = userManager.getUser();
		
		m_externalCompiledStaticCodeRepo.addSource(user);
		m_externalCompiledStaticCodeRepo.addSource(m_snapBufferManager);
		m_externalCompiledStaticCodeRepo.addSource(m_appContext.codeCache);
		
		m_internalCodeRepo.addSource(user);
		m_internalCodeRepo.addSource(m_appContext.cellBufferMngr);
		m_internalCodeRepo.addSource(m_appContext.codeCache);
		
		register(new Event_CameraSnapping_OnTargetCellAppeared());
	}

	void updateGridCoordinate(GridCoordinate targetCoordinate, CellAddress targetAddress_nullable, Point targetPoint)
	{		
		StateMachine_Camera machine = this.getParent();
		
		CellAddress oldTargetAddress = m_targetAddress;
		m_targetAddress = targetAddress_nullable;
		
		boolean firstUpdate = m_targetGridCoordinate.isEqualTo(INVALID);
		boolean sameCoordinateAsLastTime = m_targetGridCoordinate.isEqualTo(targetCoordinate);
		
		m_targetGridCoordinate.copy(targetCoordinate);
		
		m_snapCamera.setPosition(targetPoint);
		
		if( !sameCoordinateAsLastTime )
		{
			m_targetCell = null;
			
			tryToGetTargetCell(/*fireEvent=*/false);
			
			//--- This "nuke" used to get rid of everything, but that sort of broke the UI experience,
			//--- and most of the time resulted in too much network traffic, so now only errors are cleared.
			//--- User can still get guaranteed fresh version from server using refresh button.
			E_CellNuke nukeType = E_CellNuke.ERRORS_ONLY;
			m_appContext.codeMngr.nukeFromOrbit(targetCoordinate, nukeType);
			
			//--- DRK > Not flushing populator here because requestCodeForTargetCell() will do it for us.
			this.updateSnapBufferManager(false);
			
			this.m_hasRequestedSourceCode = false;
			this.m_hasRequestedCompiledCode = false;
			
			if( m_targetAddress == null )
			{
				//--- DRK > Try to get address ourselves...could turn up null.
				CellAddressMapping mapping = new CellAddressMapping(m_targetGridCoordinate);
				CellAddressManager addyManager = m_appContext.addressMngr;
				addyManager.getCellAddress(mapping, E_TransactionAction.QUEUE_REQUEST);
			}
			
			requestCodeForTargetCell();
		}
		else
		{
			if( m_targetAddress == null && oldTargetAddress != null )
			{
				m_targetAddress = oldTargetAddress;
			}
			
			//--- DRK > Same target cell as last time, but target position might
			//---		have changed enough to require loading/deleting of different nearby cells
			this.updateSnapBufferManager(true);
		}
		
		CameraManager manager = m_appContext.cameraMngr;
		
		if( firstUpdate )
		{
			m_snapProgressBase = 0.0;
		}
		else
		{
			m_snapProgressBase = this.getOverallSnapProgress();
		}
		
		manager.setTargetPosition(targetPoint, false, !sameCoordinateAsLastTime);
	}
	
	public double getOverallSnapProgress()
	{
		CameraManager manager = m_appContext.cameraMngr;
		
		//s_logger.severe("snap prog base: " + m_snapProgressBase + "   snap prog: " + manager.getSnapProgress());
		return m_snapProgressBase + (1-m_snapProgressBase)*manager.getSnapProgress();
	}

	private void requestCodeForTargetCell()
	{
		CellCodeManager populator = m_appContext.codeMngr;
		
		if( m_hasRequestedSourceCode && m_hasRequestedCompiledCode )
		{
			populator.flush(); // just in case something else needs flushing...harmless if not.
			
			return;
		}
		
		CellBuffer displayBuffer = m_snapBufferManager.getLowestDisplayBuffer();
		
		//--- DRK > Not entering here should be an impossible case, but avoid a null pointer exception just to be sure.
		if( displayBuffer.isInBoundsAbsolute(m_targetGridCoordinate) )
		{
			BufferCell cell = displayBuffer.getCellAtAbsoluteCoord(m_targetGridCoordinate);
			I_LocalCodeRepository localCodeRepo = m_internalCodeRepo;

			if( !m_hasRequestedSourceCode )
			{
				//--- DRK > As an optimization, we only retrieve the source html if we're in the html state.
				if( getContext().isForegrounded(StateMachine_EditingCode.class) )
				{
					populator.populateCell(cell, localCodeRepo, 1, true, E_CodeType.SOURCE);
					
					m_hasRequestedSourceCode = true;
				}
			}
			
			if( !m_hasRequestedCompiledCode )
			{
				populator.populateCell(cell, localCodeRepo, 1, true, E_CodeType.COMPILED);

				//--- DRK > NOTE that COMPILED_STATIC html will be retrieved implicitly because we update the buffer manager
				//---		itself before we get into this method...it will be in the same batch too, automatically...cool.
				
				m_hasRequestedCompiledCode = true;
			}
		}
		else
		{
			U_Debug.ASSERT(false, "requestCodeForTargetCell1");
			
			m_hasRequestedSourceCode = true;
			m_hasRequestedCompiledCode = true;
		}
		
		populator.flush();
	}
	
	private void updateSnapBufferManager(boolean flushPopulator)
	{
		ClientGrid grid = m_appContext.gridMngr.getGrid();
		I_LocalCodeRepository htmlSource = m_internalCodeRepo;
		
		int options = F_BufferUpdateOption.COMMUNICATE_WITH_SERVER;
		if( flushPopulator )
		{
			options |= F_BufferUpdateOption.FLUSH_CELL_POPULATOR;
		}
		
		m_snapBufferManager.update_cameraMoving(0.0, grid, m_snapCamera, null, htmlSource, options);
	}
	
	I_LocalCodeRepository getCompiledStaticHtmlSource()
	{
		return m_externalCompiledStaticCodeRepo;
	}
	
	I_LocalCodeRepository getHtmlSourceForTargetCell()
	{
		return m_snapBufferManager;
	}
	
	public GridCoordinate getTargetCoord()
	{
		return m_targetGridCoordinate;
	}
	
	public CellAddress getTargetAddress()
	{
		return m_targetAddress;
	}
	
	@Override public BufferCell getCell()
	{
		return m_targetCell;
	}
	
	@Override
	protected void didEnter(StateArgs constructor)
	{
		StateMachine_Camera machine = getParent();
		
		Constructor constructor_cast = (Constructor) constructor;
		
		m_appContext.registerBufferMngr(m_snapBufferManager);
		
		m_appContext.cellBufferMngr.overrideSubCellCount();
		
		m_targetGridCoordinate.copy(INVALID);
		
		Camera camera = m_appContext.cameraMngr.getCamera();
		
		m_snapCamera.setViewRect(camera.getViewWidth(), camera.getViewHeight());
		
		m_hasRequestedSourceCode = false;
		m_hasRequestedCompiledCode = false;
		
		m_snapProgressBase = 0;

		updateGridCoordinate(constructor_cast.m_targetCoordinate, constructor_cast.m_targetAddress, constructor_cast.m_targetPoint);
		
		constructor_cast.clear();
	}
	
	@Override
	protected void didForeground(Class<? extends A_State> revealingState, Object[] argsFromRevealingState)
	{
		
	}
	
	private void tryToGetTargetCell(boolean fireEvent)
	{
		if( m_targetCell != null )  return;
		
		m_targetCell = m_appContext.cellBufferMngr.getLowestDisplayBuffer().getCellAtAbsoluteCoord(m_targetGridCoordinate);
		
		if( m_targetCell != null && fireEvent )
		{
			perform(Event_CameraSnapping_OnTargetCellAppeared.class);
		}
	}
	
	@Override
	protected void update(double timeStep)
	{
		tryToGetTargetCell(/*fireEvent=*/true);
	
		StateMachine_Camera machine = ((StateMachine_Camera) getParent());
		
		if ( m_appContext.cameraMngr.isCameraAtRest() )
		{
//			s_logger.severe(""+m_targetGridCoordinate);
			
			State_ViewingCell.Constructor constructor = new State_ViewingCell.Constructor(m_targetCell);
			
			if( m_targetAddress != null )
			{
				//--- DRK > This cell might have already been given its address by the address manager,
				//---		but it doesn't hurt to do this twice.
				m_targetCell.onAddressFound(m_targetAddress);
			}
			
			set(State_ViewingCell.class, constructor);
			
			return;
		}
	}
	
	@Override
	protected void willExit()
	{
		m_appContext.cellBufferMngr.removeOverrideSubCellCount();
		
		m_targetAddress = null;
		m_targetCell = null;
		m_targetGridCoordinate.copy(INVALID);
		
		//--- DRK > Might implement a more elegant expiry system for cells/code hanging out in the snap buffer,
		//---		but for now it just gets nuked every time we stop snapping.
		//---		Code will generally hang out in the LRU cache anyway for a little bit, and cells are kept
		//---		in a pool, so not a huge deal as far as thrashing memory or anything.
		m_snapBufferManager.drain();
		
		m_appContext.unregisterBufferMngr(m_snapBufferManager);
	}

	@Override
	public void onStateEvent(StateEvent event)
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
				if( event.getAction() == Action_Camera_SetViewSize.class )
				{
					Camera camera = m_appContext.cameraMngr.getCamera();
					m_snapCamera.setViewRect(camera.getViewWidth(), camera.getViewHeight());
					
					//--- DRK > Updating snap point if need be...like a view size smaller
					//---		than the cell size will push the target point up to the upper left.
					this.updateGridCoordinate(m_targetGridCoordinate, m_targetAddress, m_appContext.cameraMngr.getTargetPosition());
					
					tryToGetTargetCell(/*fireEvent=*/true);
					
					this.updateSnapBufferManager(true);
				}
				else if( event.getAction() == StateMachine_Base.OnGridUpdate.class )
				{
					this.updateSnapBufferManager(true);
				}
				else if( event.getAction() == Event_Camera_OnAddressResponse.class )
				{
					Event_Camera_OnAddressResponse.Args args = event.getActionArgs();
					
					if( args.getType() == Event_Camera_OnAddressResponse.E_Type.ON_FOUND )
					{
						m_targetAddress = args.getAddress();
					}
				}
				
				break;
			}
		}
	}
}