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
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.A_StateMachine;
import swarm.shared.statemachine.I_StateEventListener;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.statemachine.StateEvent;
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
	
	private PendingSnap m_pendingSnap = null;
	
	private static final Logger s_logger = Logger.getLogger(StateMachine_Camera.class.getName());

	private final LocalCodeRepositoryWrapper m_codeRepo = new LocalCodeRepositoryWrapper();
	
	private final CellAddressManagerListener m_addressManagerListener = new CellAddressManagerListener(this);
	private final Action_Camera_SnapToCoordinate.Args m_snapToCoordArgs = new Action_Camera_SnapToCoordinate.Args();
	private final Event_Camera_OnCellSizeFound.Args m_onCellSizeFoundArgs = new Event_Camera_OnCellSizeFound.Args();
	
	private final AppContext m_appContext;
	
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
	protected void didEnter(StateArgs constructor)
	{
		//--- DRK > Not enforcing z constraints here because UI probably hasn't told us camera view size yet.
		
		A_ClientUser user = m_appContext.userMngr.getUser();
		m_appContext.cameraMngr.setCameraPosition(user.getLastPosition(), false);
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
	protected void didForeground(Class<? extends A_State> revealingState, Object[] argsFromRevealingState)
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
	
	@Override
	protected void update(double timeStep)
	{
		m_appContext.cameraMngr.update(timeStep);
		
		if( !m_appContext.cameraMngr.isCameraAtRest() )
		{
			updateBufferManager();
		}
	}
	
	void updateBufferManager()
	{
		CellBufferManager bufferMngr = m_appContext.cellBufferMngr;
		
		if( this.getCurrentState() instanceof State_CameraSnapping )
		{
			State_CameraSnapping snappingState = ((State_CameraSnapping)this.getCurrentState());
			I_LocalCodeRepository compiledStaticHtmlSource = snappingState.getCompiledStaticHtmlSource();
			
			int options = F_BufferUpdateOption.CREATE_VISUALIZATIONS;
			
			bufferMngr.update(m_appContext.gridMngr.getGrid(), m_appContext.cameraMngr.getCamera(), snappingState.getTargetCoord(), compiledStaticHtmlSource, options);
			
			//--- DRK > As soon as target cell comes into sight, we start trying to populate
			//---		it with compiled_dynamic and source html from the snapping state's html source(s).
			CellBuffer buffer = bufferMngr.getBaseDisplayBuffer();
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
			int options = F_BufferUpdateOption.ALL;
			bufferMngr.update(m_appContext.gridMngr.getGrid(), m_appContext.cameraMngr.getCamera(), null, m_codeRepo, options);
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
	public void onStateEvent(StateEvent event)
	{
		switch(event.getType())
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof State_CameraSnapping )
				{
					//--- DRK > In most cases I think this is a somewhat wasteful update of the buffer,
					//---		but it's needed for fringe cases of the snapping state going immediately
					//---		into the viewing state without an update in between.
					//---		Arguably, a better strategy might be to force at least one or more updates between states.
					//---		Either way, kinda hacky, but oh well.
					this.updateBufferManager();
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == StateMachine_Base.OnGridUpdate.class )
				{
					m_appContext.cameraMngr.getCamera().onGridSizeChanged();
					
					updateBufferManager();
				}
				
				break;
			}
		}
	}
}