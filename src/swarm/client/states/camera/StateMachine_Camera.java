package swarm.client.states.camera;


import java.util.logging.Logger;

import swarm.client.app.smAppContext;
import swarm.client.code.smCompilerErrorMessageGenerator;
import swarm.client.entities.smCamera;
import swarm.client.managers.smCellCodeManager;
import swarm.client.entities.smBufferCell;
import swarm.client.managers.smCameraManager;
import swarm.client.managers.smCellAddressManager;
import swarm.client.managers.smCellBuffer;
import swarm.client.managers.smCellBufferManager;
import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smUserManager;
import swarm.client.entities.smA_ClientUser;
import swarm.client.managers.smF_BufferUpdateOption;
import swarm.client.input.smBrowserHistoryManager;
import swarm.client.input.smBrowserAddressManager;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.State_AsyncDialog;
import swarm.client.states.State_GenericDialog;
import swarm.client.states.StateMachine_Base.OnGridResize;
import swarm.client.structs.smCellCodeCache;
import swarm.client.structs.smI_LocalCodeRepository;
import swarm.client.structs.smLocalCodeRepositoryWrapper;
import swarm.client.transaction.smClientTransactionManager;
import swarm.client.transaction.smE_TransactionAction;
import swarm.shared.app.smS_App;
import swarm.shared.code.smCompilerResult;
import swarm.shared.code.smE_CompilationStatus;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.statemachine.smA_Action;

import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateMachine;
import swarm.shared.statemachine.smI_StateEventListener;
import swarm.shared.statemachine.smA_StateConstructor;
import swarm.shared.statemachine.smStateEvent;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smTolerance;
import swarm.shared.structs.smVector;

/**
 * ...
 * @author
 */
public class StateMachine_Camera extends smA_StateMachine implements smI_StateEventListener
{
	public static final double IGNORED_COMPONENT = Double.NaN;
	
	static final class PendingSnap
	{
		PendingSnap(smGridCoordinate coordinate, smCellAddress address)
		{
			m_coordinate = coordinate;
			m_address = address;
		}
		
		private final smGridCoordinate m_coordinate;
		private final smCellAddress m_address;
	}
	
	private PendingSnap m_pendingSnap = null;
	
	private static final Logger s_logger = Logger.getLogger(StateMachine_Camera.class.getName());

	private final smLocalCodeRepositoryWrapper m_codeRepo = new smLocalCodeRepositoryWrapper();
	
	private final smCellAddressManagerListener m_addressManagerListener = new smCellAddressManagerListener(this);
	
	private final smAppContext m_appContext;
	
	public StateMachine_Camera(smAppContext appContext)
	{
		m_appContext = appContext;
		
		smA_Action.register(new Action_Camera_SetCameraViewSize(m_appContext.cameraMngr));
		smA_Action.register(new Action_Camera_SnapToAddress(m_appContext.addressMngr));
		smA_Action.register(new Action_Camera_SnapToCoordinate(m_appContext.gridMngr));
		smA_Action.register(new Event_Camera_OnAddressResponse());
		smA_Action.register(new Action_Camera_SetCameraTarget(m_appContext.cameraMngr));
		smA_Action.register(new Action_Camera_SetInitialPosition(m_appContext.cameraMngr));
		
		smA_ClientUser user = m_appContext.userMngr.getUser();
		m_codeRepo.addSource(user);
		m_codeRepo.addSource(m_appContext.codeCache);
	}
	
	void snapToCellAddress(smCellAddress address)
	{
		smCellAddressManager addressManager = m_appContext.addressMngr;
		smA_State currentState = this.getCurrentState();
		
		if( currentState instanceof State_GettingMapping )
		{
			((State_GettingMapping)currentState).updateAddress(address);
		}
		else
		{
			if( currentState instanceof State_CameraSnapping )
			{
				machine_setState(this, State_CameraFloating.class);
				
				//TODO: Might want to ease the camera instead of stopping it short.
				m_appContext.cameraMngr.setTargetPosition(m_appContext.cameraMngr.getCamera().getPosition(), false);
			}
			
			State_GettingMapping.Constructor constructor = new State_GettingMapping.Constructor(address);
			machine_pushState(this, State_GettingMapping.class, constructor);
		}
		
		addressManager.getCellAddressMapping(address, smE_TransactionAction.MAKE_REQUEST);
	}
	
	/**
	 * Address can be null, I think..
	 */
	void snapToCoordinate(smCellAddress address_nullable, smGridCoordinate coord)
	{
		if( !this.isForegrounded() )
		{
			this.m_pendingSnap = new PendingSnap(coord, address_nullable);
			
			return;
		}
		
		smA_State currentState = this.getCurrentState();
		if( currentState instanceof State_CameraSnapping )
		{
			((State_CameraSnapping)currentState).updateGridCoordinate(coord, address_nullable);
		}
		else
		{
			State_CameraSnapping.Constructor constructor = new State_CameraSnapping.Constructor(coord, address_nullable);
			machine_setState(this, State_CameraSnapping.class, constructor);
		}
	}
	
	void tryPoppingGettingAddressState()
	{
		if( this.getCurrentState() instanceof State_GettingMapping )
		{
			if( this.isForegrounded() )
			{
				machine_popState(this);
			}
			else
			{
				((State_GettingMapping) this.getCurrentState()).updateAddress(null);
			}
		}
	}
	
	public smI_LocalCodeRepository getCodeRepository()
	{
		return m_codeRepo;
	}
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
		//--- DRK > Not enforcing z constraints here because UI probably hasn't told us camera view size yet.
		
		smA_ClientUser user = m_appContext.userMngr.getUser();
		m_appContext.cameraMngr.setCameraPosition(user.getLastPosition(), false);

		smCellCodeManager codeMngr = m_appContext.codeMngr;

		codeMngr.start(new smCellCodeManager.I_SyncOrPreviewDelegate()
		{
			@Override
			public void onCompilationFinished(smCompilerResult result)
			{
				String title = null, body = null;
				
				if( result.getStatus() == smE_CompilationStatus.NO_ERROR )
				{
					if( result.getMessages() != null )
					{
						title = "Warnings...fix them if you want.";
						body = smCompilerErrorMessageGenerator.getInstance().generate(result);
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
					
					body = smCompilerErrorMessageGenerator.getInstance().generate(result);
				}
				
				if( title != null )
				{
					StateMachine_Base baseController = smA_State.getEnteredInstance(StateMachine_Base.class);
					
					State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor(title, body);
					baseController.queueAsyncDialog(State_AsyncDialog.class, constructor);
				}
			}
		});
		
		m_appContext.addressMngr.start(m_addressManagerListener);
	}
	
	@Override
	protected void didForeground(Class<? extends smA_State> revealingState, Object[] argsFromRevealingState)
	{
		if( getCurrentState() == null )
		{
			if( revealingState == null ) // DRK > Hmm, not sure why I put this check here, but shouldn't be harmful I guess.
			{
				machine_setState(this, State_CameraFloating.class);
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
			this.snapToCoordinate(m_pendingSnap.m_address, m_pendingSnap.m_coordinate);
			
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
		smCellBufferManager bufferMngr = m_appContext.cellBufferMngr;
		
		if( this.getCurrentState() instanceof State_CameraSnapping )
		{
			State_CameraSnapping snappingState = ((State_CameraSnapping)this.getCurrentState());
			smI_LocalCodeRepository compiledStaticHtmlSource = snappingState.getCompiledStaticHtmlSource();
			
			int options = smF_BufferUpdateOption.CREATE_VISUALIZATIONS;
			
			bufferMngr.update(m_appContext.gridMngr.getGrid(), m_appContext.cameraMngr.getCamera(), compiledStaticHtmlSource, options);
			
			//--- DRK > As soon as target cell comes into sight, we start trying to populate
			//---		it with compiled_dynamic and source html from the snapping state's html source(s).
			smCellBuffer buffer = bufferMngr.getDisplayBuffer();
			if( buffer.getSubCellCount() == 1 )
			{
				if( buffer.isInBoundsAbsolute(snappingState.getTargetCoordinate()) )
				{
					smBufferCell cell = buffer.getCellAtAbsoluteCoord(snappingState.getTargetCoordinate());
					smI_LocalCodeRepository targetCellHtmlSource = snappingState.getHtmlSourceForTargetCell();

					m_appContext.codeMngr.populateCell(cell, targetCellHtmlSource, 1, false, false, smE_CodeType.COMPILED);
					m_appContext.codeMngr.populateCell(cell, targetCellHtmlSource, 1, false, false, smE_CodeType.SOURCE);
					
					//--- DRK > NOTE: Don't need to flush populator because we're not telling it to communicate with server.
				}
			}
		}
		else
		{
			int options = smF_BufferUpdateOption.ALL;
			bufferMngr.update(m_appContext.gridMngr.getGrid(), m_appContext.cameraMngr.getCamera(), m_codeRepo, options);
		}
	}
	
	protected void willExit()
	{
		m_appContext.addressMngr.stop();
		
		m_appContext.codeMngr.stop();
	}

	@Override
	public void onStateEvent(smStateEvent event)
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
				if( event.getAction() == StateMachine_Base.OnGridResize.class )
				{
					m_appContext.cameraMngr.getCamera().onGridSizeChanged();
					
					updateBufferManager();
				}
				
				break;
			}
		}
	}
}