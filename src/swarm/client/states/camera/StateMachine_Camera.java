package swarm.client.states.camera;


import java.util.logging.Logger;

import swarm.client.app.AppContext;
import swarm.client.code.CompilerErrorMessageGenerator;
import swarm.client.entities.Camera;
import swarm.client.managers.CellCodeManager;
import swarm.client.entities.BufferCell;
import swarm.client.managers.CameraManager;
import swarm.client.managers.CellAddressManager;
import swarm.client.managers.CellBuffer;
import swarm.client.managers.CellBufferManager;
import swarm.client.managers.CellSizeManager;
import swarm.client.managers.ClientAccountManager;
import swarm.client.managers.UserManager;
import swarm.client.entities.A_ClientUser;
import swarm.client.managers.F_BufferUpdateOption;
import swarm.client.input.BrowserHistoryManager;
import swarm.client.input.BrowserAddressManager;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.State_AsyncDialog;
import swarm.client.states.State_GenericDialog;
import swarm.client.states.StateMachine_Base.OnGridUpdate;
import swarm.client.structs.CellCodeCache;
import swarm.client.structs.I_LocalCodeRepository;
import swarm.client.structs.LocalCodeRepositoryWrapper;
import swarm.client.transaction.ClientTransactionManager;
import swarm.client.transaction.E_TransactionAction;
import swarm.shared.app.S_CommonApp;
import swarm.shared.code.CompilerResult;
import swarm.shared.code.E_CompilationStatus;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.A_Cell;
import swarm.shared.entities.A_Grid;
import swarm.shared.entities.E_CodeType;
import swarm.shared.json.I_JsonObject;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_Action_Event;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.A_StateMachine;
import swarm.shared.statemachine.I_StateEventListener;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.A_BaseStateEvent;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.CellSize;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;
import swarm.shared.structs.Tolerance;
import swarm.shared.structs.Vector;
import swarm.shared.utils.U_Math;

/**
 * ...
 * @author
 */
public class StateMachine_Camera extends A_StateMachine implements I_StateEventListener
{
	private static final double DISABLE_TIMER = -1.0;
	private static final double START_TIMER = 0.0;
	
	public static final class OnCellWithNaturalDimensionsLoaded extends A_Action_Event{}
	
	public static final double IGNORED_COMPONENT = Double.NaN;
	
	static final class PendingSnap
	{
		PendingSnap(GridCoordinate coordinate, CellAddress address)
		{
			m_coordinate = coordinate;
			m_address = address;
		}
		
		private final GridCoordinate m_coordinate;
		private final CellAddress m_address;
	}
	
	public static class SignalBufferDirty extends A_Action
	{
		@Override public void perform(StateArgs args)
		{
			StateMachine_Camera machine = this.getState();
			machine.m_bufferDirty = true;
		}
	}
	
	private PendingSnap m_pendingSnap = null;
	
	private static final Logger s_logger = Logger.getLogger(StateMachine_Camera.class.getName());

	private final LocalCodeRepositoryWrapper m_codeRepo = new LocalCodeRepositoryWrapper();
	
	private final CellAddressManagerListener m_addressManagerListener = new CellAddressManagerListener(this);
	private final Action_Camera_SnapToCoordinate.Args m_snapToCoordArgs = new Action_Camera_SnapToCoordinate.Args(null);
	private final Event_Camera_OnCellSizeFound.Args m_onCellSizeFoundArgs = new Event_Camera_OnCellSizeFound.Args();
	
	private final AppContext m_appContext;
	
	private boolean m_bufferDirty = false;
	
	private double m_metaStickAroundTimer = DISABLE_TIMER;
	
	public StateMachine_Camera(AppContext appContext, Action_Camera_SnapToCoordinate.I_Filter snapFilter)
	{
		m_appContext = appContext;
		
		A_ClientUser user = m_appContext.userMngr.getUser();
		m_codeRepo.addSource(user);
		m_codeRepo.addSource(m_appContext.codeCache);
		
		register(new Action_Camera_SetViewSize(m_appContext.cameraMngr));
		register(new Action_Camera_SnapToAddress(m_appContext.addressMngr));
		register(new Action_Camera_SnapToCoordinate(snapFilter, m_appContext.gridMngr));
		register(new Event_Camera_OnAddressResponse());
		register(new Event_Camera_OnCellSizeFound());
		register(new Action_Camera_SnapToPoint(m_appContext.cameraMngr));
		register(new Action_Camera_SetInitialPosition(m_appContext.cameraMngr));
		register(new SignalBufferDirty());
		register(new OnCellWithNaturalDimensionsLoaded());
	}
	
	void snapToCellAddress(CellAddress address)
	{
		CellAddressManager addressManager = m_appContext.addressMngr;
		A_State currentState = this.getCurrentState();
		
		if( currentState instanceof State_GettingMapping )
		{
			((State_GettingMapping)currentState).updateAddress(address);
		}
		else
		{
			if( currentState instanceof State_CameraSnapping )
			{
				set(this, State_CameraFloating.class);
				
				//TODO: Might want to ease the camera instead of stopping it short.
				m_appContext.cameraMngr.setTargetPosition(m_appContext.cameraMngr.getCamera().getPosition(), false, true);
			}
			
			State_GettingMapping.Constructor constructor = new State_GettingMapping.Constructor(address);
			pushV(this, State_GettingMapping.class, constructor);
		}
		
		addressManager.getCellAddressMapping(address, E_TransactionAction.MAKE_REQUEST);
	}

	void snapToCoordinate(CellAddress address_nullable, GridCoordinate coord, Point targetPoint)
	{
		if( !this.isForegrounded() )
		{
			this.m_pendingSnap = new PendingSnap(coord, address_nullable);
			
			return;
		}
		
		A_State currentState = this.getCurrentState();
		if( currentState instanceof State_CameraSnapping )
		{
			((State_CameraSnapping)currentState).updateGridCoordinate(coord, address_nullable, targetPoint);
		}
		else
		{
			State_CameraSnapping.Constructor constructor = new State_CameraSnapping.Constructor(coord, address_nullable, targetPoint);
			set(this, State_CameraSnapping.class, constructor);
		}
	}
	
	void tryPoppingGettingAddressState()
	{
		if( this.getCurrentState() instanceof State_GettingMapping )
		{
			if( this.isForegrounded() )
			{
				popV(this);
			}
			else
			{
				((State_GettingMapping) this.getCurrentState()).updateAddress(null);
			}
		}
	}
	
	public I_LocalCodeRepository getCodeRepository()
	{
		return m_codeRepo;
	}
	
	@Override
	protected void didEnter()
	{
		//--- DRK > Not enforcing z constraints here because UI probably hasn't told us camera view size yet.
		
		A_ClientUser user = m_appContext.userMngr.getUser();
		Point start = m_appContext.config.startingPoint != null ? m_appContext.config.startingPoint : user.getLastPosition();
//		start = new Point(start.getX(), start.getY(), start.getZ()+20000);
		m_appContext.cameraMngr.setCameraPosition(start, false);
		m_appContext.registerBufferMngr(m_appContext.cellBufferMngr);

		CellCodeManager codeMngr = m_appContext.codeMngr;

		codeMngr.start(new CellCodeManager.I_Listener()
		{
			@Override
			public void onCompilationFinished(CompilerResult result)
			{
				String title = null, body = null;
				
				if( result.getStatus() == E_CompilationStatus.NO_ERROR )
				{
					if( result.getMessages() != null )
					{
						title = "Warnings...fix them if you want.";
						body = m_appContext.compilerErrorMsgGenerator.generate(result);
					}
				}
				else
				{
					if( result.getMessages() != null )
					{
						title = "Errors...must be fixed!";
					}
					else
					{
						title = "Compiler Error";
					}
					
					body = m_appContext.compilerErrorMsgGenerator.generate(result);
				}
				
				if( title != null )
				{
					StateMachine_Base baseController = getContext().getEntered(StateMachine_Base.class);
					
					State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor(title, body);
					baseController.queueAsyncDialog(State_AsyncDialog.class, constructor);
				}
			}
		});

		m_appContext.addressMngr.start(m_addressManagerListener);
		
		m_appContext.cellSizeMngr.start(new CellSizeManager.I_Listener()
		{
			@Override
			public void onCellSizeFound(CellAddressMapping mapping_copied, CellSize cellSize_copied)
			{
				A_State currentState = StateMachine_Camera.this.getCurrentState();
				if( currentState instanceof State_CameraSnapping || currentState instanceof State_ViewingCell )
				{
					I_State_SnappingOrViewing state = (I_State_SnappingOrViewing) currentState;
					
					if( state.getTargetCoord().isEqualTo(mapping_copied.getCoordinate()) )
					{
						StateMachine_Camera.this.m_onCellSizeFoundArgs.init(cellSize_copied, mapping_copied);
						StateMachine_Camera.this.perform(Event_Camera_OnCellSizeFound.class, StateMachine_Camera.this.m_onCellSizeFoundArgs);
					}
				}
			}
		});
	}
	
	@Override
	protected void didForeground(Class<? extends A_State> revealingState, StateArgs argsFromRevealingState)
	{
		if( getCurrentState() == null )
		{
			if( revealingState == null ) // DRK > Hmm, not sure why I put this check here, but shouldn't be harmful I guess.
			{
				set(this, State_CameraFloating.class);
			}
		}
		else if( this.getCurrentState() instanceof State_GettingMapping )
		{
			if( ((State_GettingMapping) this.getCurrentState()).getAddress() == null )
			{
				this.tryPoppingGettingAddressState();
			}
		}
		
		if( m_pendingSnap != null )
		{
			m_snapToCoordArgs.init(m_pendingSnap.m_address, m_pendingSnap.m_coordinate);
			
			m_pendingSnap = null;
		}
	}
	
	private void updateMetaCountOverride()
	{
		CellBufferManager bufferMngr = m_appContext.cellBufferMngr;
		Camera camera = m_appContext.cameraMngr.getCamera();
		double deltaZ = camera.getPosition().getZ() - camera.getPrevPosition().getZ();
		boolean cameraMovingInZ = deltaZ != 0.0;
		
		int currentNonOverriddenMetaCount = bufferMngr.getNonOverriddenSubCellCount();
		
		if( !bufferMngr.isOverridingSubCellCount() )
		{
			m_metaStickAroundTimer = DISABLE_TIMER;
			
			if( currentNonOverriddenMetaCount > 1 && deltaZ < 0.0 )
			{
				bufferMngr.overrideSubCellCount();
			}
		}
		else
		{
			int overrideMetaCount = bufferMngr.getOverrideSubCellCount();
			
			if( currentNonOverriddenMetaCount > overrideMetaCount )
			{
				//--- DRK > Instantly remove the override and start displaying higher meta cells.
				m_metaStickAroundTimer = DISABLE_TIMER;
				bufferMngr.removeOverrideSubCellCount();
			}
			else if( overrideMetaCount > currentNonOverriddenMetaCount )
			{
				if( cameraMovingInZ )
				{
					//--- DRK > Ensure that timer is set or reset back to disabled.
					m_metaStickAroundTimer = DISABLE_TIMER;
				}
				else
				{
					//--- DRK > Start the timer if it hasn't been started yet.
					if( m_metaStickAroundTimer == DISABLE_TIMER )
					{
						m_metaStickAroundTimer = START_TIMER;
					}
				}
			}
		}
	}
	
	private boolean updateMetaStickAroundTimer_didTimerJustFinish(double timestep)
	{
		CellBufferManager bufferMngr = m_appContext.cellBufferMngr;
		
		if( m_metaStickAroundTimer >= START_TIMER )
		{
			m_metaStickAroundTimer += timestep;
			
			if( m_metaStickAroundTimer > m_appContext.config.timeThatMetaCellSticksAroundAfterCameraStopsZooming )
			{
				m_metaStickAroundTimer = DISABLE_TIMER;
				bufferMngr.removeOverrideSubCellCount();
				
				return true;
			}
		}
		
		return false;
	}
	
	@Override protected void willEnter(Class<? extends A_State> stateClass, StateArgs args)
	{
		if( stateClass == State_ViewingCell.class )
		{
			CellBufferManager bufferMngr = m_appContext.cellBufferMngr;
			m_metaStickAroundTimer = DISABLE_TIMER;
			bufferMngr.removeOverrideSubCellCount();
		}
	}
	
	@Override
	protected void update(double timeStep)
	{
		boolean didTimerJustFinish = updateMetaStickAroundTimer_didTimerJustFinish(timeStep);
		
		m_appContext.cameraMngr.update(timeStep);
		
		boolean cameraAtRest = m_appContext.cameraMngr.isCameraAtRest();
		
		updateMetaCountOverride();
		
		if( !cameraAtRest || m_bufferDirty || didTimerJustFinish)
		{
			m_bufferDirty = false;
			updateBufferManager(timeStep);
		}
		else
		{
			CellBufferManager bufferMngr = m_appContext.cellBufferMngr;
			bufferMngr.update_cameraStill(m_appContext.gridMngr.getGrid(), timeStep);
		}
	}
	
	void updateBufferManager(double timestep)
	{
		CellBufferManager bufferMngr = m_appContext.cellBufferMngr;
		
		if( this.getCurrentState() instanceof State_CameraSnapping )
		{
			State_CameraSnapping snappingState = ((State_CameraSnapping)this.getCurrentState());
			I_LocalCodeRepository compiledStaticHtmlSource = snappingState.getCompiledStaticHtmlSource();
			
			int options = F_BufferUpdateOption.CREATE_VISUALIZATIONS;
			
//			s_logger.severe(snappingState.getTargetCoord()+"");
			
			bufferMngr.update_cameraMoving(timestep, m_appContext.gridMngr.getGrid(), m_appContext.cameraMngr.getCamera(), snappingState.getTargetCoord(), compiledStaticHtmlSource, options);
			
			//--- DRK > As soon as target cell comes into sight, we start trying to populate
			//---		it with compiled_dynamic and source html from the snapping state's html source(s).
			CellBuffer buffer = bufferMngr.getLowestDisplayBuffer();
			if( buffer.getSubCellCount() == 1 )
			{
				if( buffer.isInBoundsAbsolute(snappingState.getTargetCoord()) )
				{
					BufferCell cell = buffer.getCellAtAbsoluteCoord(snappingState.getTargetCoord());
					
					if( cell != null )
					{
						I_LocalCodeRepository targetCellHtmlSource = snappingState.getHtmlSourceForTargetCell();
	
						m_appContext.codeMngr.populateCell(cell, targetCellHtmlSource, 1, false, E_CodeType.COMPILED);
						m_appContext.codeMngr.populateCell(cell, targetCellHtmlSource, 1, false, E_CodeType.SOURCE);
					}
					
					//--- DRK > NOTE: Don't need to flush populator because we're not telling it to communicate with server.
				}
			}
		}
		else
		{
			GridCoordinate targetCoord = null;
			if( this.getCurrentState() instanceof State_ViewingCell )
			{
				targetCoord = ((State_ViewingCell)this.getCurrentState()).getTargetCoord();
			}
			
			int options = F_BufferUpdateOption.ALL_DEFAULTS;
			bufferMngr.update_cameraMoving(timestep, m_appContext.gridMngr.getGrid(), m_appContext.cameraMngr.getCamera(), targetCoord, m_codeRepo, options);
		}
	}
	
	protected void willExit()
	{
		m_appContext.unregisterBufferMngr(m_appContext.cellBufferMngr);
		
		m_appContext.cellSizeMngr.stop();
		m_appContext.addressMngr.stop();
		m_appContext.codeMngr.stop();
	}

	@Override
	public void onStateEvent(A_BaseStateEvent event)
	{
		switch(event.getType())
		{
			case DID_ENTER:
			{
				//--- DRK > Used to be relevant but now doing this below for when we enter viewing state, so that should cover it.
				//---		Keeping for reference in case I'm missing something.
//				if( event.getState() instanceof State_CameraSnapping )
//				{
//					//--- DRK > In most cases I think this is a somewhat wasteful update of the buffer,
//					//---		but it's needed for fringe cases of the snapping state going immediately
//					//---		into the viewing state without an update in between.
//					//---		Arguably, a better strategy might be to force at least one or more updates between states.
//					//---		Either way, kinda hacky, but oh well.
//					this.updateBufferManager(0.0);
//				}
				
				//--- DRK > This is needed for cases where we snap from a higher meta level down to cell_1. In these cases
				//---		the meta level override is active which means that cell_1s aren't created. Have to stoke the buffers
				//---		here to force creation of cell_1s surrounding the target cell
				if( event.getState() instanceof State_ViewingCell )
				{
					this.updateBufferManager(0.0);
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getTargetClass() == StateMachine_Base.OnGridUpdate.class )
				{
					m_appContext.cameraMngr.getCamera().onGridSizeChanged();
					
					updateBufferManager(0.0);
				}
				
				break;
			}
		}
	}
}