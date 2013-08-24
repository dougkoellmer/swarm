package swarm.client.states.camera;


import java.util.logging.Logger;

import swarm.client.app.sm_c;
import swarm.client.code.bhCompilerErrorMessageGenerator;
import swarm.client.entities.bhCamera;
import swarm.client.managers.bhCellCodeManager;
import swarm.client.entities.bhBufferCell;
import swarm.client.managers.bhCameraManager;
import swarm.client.managers.bhCellAddressManager;
import swarm.client.managers.bhCellBuffer;
import swarm.client.managers.bhCellBufferManager;
import swarm.client.managers.bhClientAccountManager;
import swarm.client.managers.bhUserManager;
import swarm.client.entities.bhA_ClientUser;
import swarm.client.managers.bhF_BufferUpdateOption;
import swarm.client.input.bhBrowserHistoryManager;
import swarm.client.input.bhBrowserAddressManager;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.State_AsyncDialog;
import swarm.client.states.State_GenericDialog;
import swarm.client.states.StateMachine_Base.OnGridResize;
import swarm.client.states.camera.State_GettingMapping.OnResponse.E_Type;
import swarm.client.structs.bhCellCodeCache;
import swarm.client.structs.bhI_LocalCodeRepository;
import swarm.client.structs.bhLocalCodeRepositoryWrapper;
import swarm.client.transaction.bhClientTransactionManager;
import swarm.client.transaction.bhE_TransactionAction;
import swarm.shared.app.bhS_App;
import swarm.shared.code.bhCompilerResult;
import swarm.shared.code.bhE_CompilationStatus;
import swarm.shared.debugging.bhU_Debug;
import swarm.shared.entities.bhA_Grid;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.statemachine.bhA_Action;

import swarm.shared.statemachine.bhA_ActionArgs;
import swarm.shared.statemachine.bhA_EventAction;
import swarm.shared.statemachine.bhA_State;
import swarm.shared.statemachine.bhA_StateMachine;
import swarm.shared.statemachine.bhI_StateEventListener;
import swarm.shared.statemachine.bhA_StateConstructor;
import swarm.shared.statemachine.bhStateEvent;
import swarm.shared.structs.bhCellAddress;
import swarm.shared.structs.bhCellAddressMapping;
import swarm.shared.structs.bhE_CellAddressParseError;
import swarm.shared.structs.bhGridCoordinate;
import swarm.shared.structs.bhPoint;
import swarm.shared.structs.bhTolerance;
import swarm.shared.structs.bhVector;

/**
 * ...
 * @author
 */
public class StateMachine_Camera extends bhA_StateMachine implements bhI_StateEventListener
{
	public static final double IGNORED_COMPONENT = Double.NaN;
	
	/**
	 * This makes various methods of bhCameraManager effectively "friended" to this StateMachine_Camera.
	 * @author Doug
	 *
	 */
	static class CameraManager extends bhCameraManager
	{
		CameraManager(bhCamera camera, double minSnapTime, double maxSnapTime)
		{
			super(camera, minSnapTime, maxSnapTime);
		}
		
		@Override
		protected void setCameraPosition(bhPoint point, boolean enforceZConstraints)
		{
			super.setCameraPosition(point, enforceZConstraints);
		}
		
		void internal_setTargetPosition(bhPoint point, boolean instant)
		{
			super.setTargetPosition(point, instant);
		}
		
		@Override
		protected void update(double timeStep)
		{
			super.update(timeStep);
		}
	}
	
	//TODO: Minor issue here is I think this has to be called before SetCameraViewSize by the UI.
	//		Could make Action API for this machine more robust by allowing any order of calls.
	public static class SetInitialPosition extends bhA_CameraAction
	{
		public static class Args extends bhA_ActionArgs
		{
			private bhPoint m_point;
			
			public void setPoint(bhPoint point)
			{
				m_point = point;
			}
		}
		
		@Override
		public void perform(bhA_ActionArgs args)
		{
			StateMachine_Camera machine = this.getState();
			
			bhPoint point = ((Args)args).m_point;
			
			CameraManager manager = (CameraManager) machine.getCameraManager();
			manager.setCameraPosition(point, false);
		}
		
		@Override
		public boolean isPerformable(bhA_ActionArgs args)
		{
			StateMachine_Camera machine = this.getState();
			return machine.getUpdateCount() == 0;
		}
		
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return StateMachine_Camera.class;
		}
	}
	
	public static class SetCameraViewSize extends bhA_Action
	{
		public static class Args extends bhA_ActionArgs
		{
			private final double[] m_dimensions = new double[2];
			
			public void set(double width, double height)
			{
				m_dimensions[0] = width;
				m_dimensions[1] = height;
			}
		}
		
		@Override
		public void perform(bhA_ActionArgs args)
		{
			Args args_cast = (Args) args;
			StateMachine_Camera machine = this.getState();
			
			machine.getCamera().setViewRect(args_cast.m_dimensions[0], args_cast.m_dimensions[1]);
			
			machine.updateBufferManager();
			
			if( machine.getCurrentState() instanceof State_CameraFloating )
			{
				machine.m_cameraManager.internal_setTargetPosition(machine.m_cameraManager.getTargetPosition(), false); // refreshes Z-constraints if necessary.
			}
			else if( machine.getCurrentState() == null )
			{
				machine.m_cameraManager.setCameraPosition(machine.m_cameraManager.getTargetPosition(), true);
			}
		}

		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return StateMachine_Camera.class;
		}
		
		@Override
		public boolean isPerformableInBackground()
		{
			return true;
		}
	}
	
	public static class SetCameraTarget extends bhA_CameraAction
	{
		public static class Args extends bhA_ActionArgs
		{
			private bhPoint m_point;
			private boolean m_instant;
			
			public Args()
			{
				m_point = null;
				m_instant = false;
			}
			
			public Args(bhPoint point)
			{
				m_point = point;
				m_instant = false;
			}
			
			public void initialize(bhPoint point, boolean instant)
			{
				m_point = point;
				m_instant = instant;
			}
			
			public void setPoint(bhPoint point)
			{
				m_point = point;
			}
			
			public bhPoint getPoint()
			{
				return m_point;
			}
			
			public boolean isInstant()
			{
				return m_instant;
			}
		}
		
		@Override
		public void perform(bhA_ActionArgs args)
		{
			StateMachine_Camera machine = this.getState();
			
			if( !(machine.getCurrentState() instanceof State_CameraFloating) )
			{
				machine_setState(machine, State_CameraFloating.class);
			}
			
			if( args == null )  return;
			
			bhPoint point = ((Args)args).m_point;
			boolean instant = ((Args)args).isInstant();
			
			if( point == null )  return;
			
			CameraManager manager = (CameraManager) machine.getCameraManager();
			manager.internal_setTargetPosition(point, instant);
			
			//--- DRK > If it's instant, it means the view layer's input is requesting a positional change,
			//---		and then will immediately draw the buffer with the assumption that the positional change
			//---		took place, so we have to update the buffer manually instead of waiting for next time step.
			if( instant )
			{
				machine.updateBufferManager();
			}
		}
		
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return StateMachine_Camera.class;
		}

		@Override
		public boolean suppressLog()
		{
			return true;
		}
	}
	
	public static class SnapToCoordinate extends bhA_CameraAction
	{
		public static class Args extends bhA_ActionArgs
		{
			private bhGridCoordinate m_coordinate;
			private boolean m_onlyCausedRefresh = false;
			
			public Args()
			{
				m_coordinate = null;
			}
			
			public Args(bhGridCoordinate coord)
			{
				m_coordinate = coord;
			}
			
			public void setCoordinate(bhGridCoordinate coordinate)
			{
				m_coordinate = coordinate;
			}
			
			public boolean onlyCausedRefresh()
			{
				return m_onlyCausedRefresh;
			}
		}
		
		@Override
		public void perform(bhA_ActionArgs args)
		{
			bhGridCoordinate coordinate = ((Args) args).m_coordinate;
			StateMachine_Camera machine = this.getState();
			bhA_State currentState = machine.getCurrentState();
			
			if( currentState instanceof State_ViewingCell )
			{
				if( ((State_ViewingCell)currentState).getCell().getCoordinate().isEqualTo(coordinate) )
				{
					((State_ViewingCell)currentState).refreshCell();
					((Args) args).m_onlyCausedRefresh = true;
					
					return;
				}
			}
			
			((Args) args).m_onlyCausedRefresh = false;
			((StateMachine_Camera)this.getState()).snapToCoordinate(null, coordinate);
		}
		
		@Override
		public boolean isPerformable(bhA_ActionArgs args)
		{
			bhGridCoordinate coordinate = ((Args) args).m_coordinate;
			
			bhA_Grid grid = sm_c.gridMngr.getGrid();
			
			if( !grid.isInBounds(coordinate) )
			{
				return false;
			}
			
			if( !grid.isTaken(coordinate) )
			{
				return false;
			}
			
			StateMachine_Camera machine = this.getState();
			bhA_State currentState = machine.getCurrentState();
			
			if( currentState instanceof State_CameraSnapping )
			{
				return !((State_CameraSnapping)currentState).getTargetCoordinate().isEqualTo(coordinate);
			}
			else
			{
				return true;
			}
		}
		
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return StateMachine_Camera.class;
		}
	}
	
	public static class SnapToAddress extends bhA_Action
	{
		public static class Args extends bhA_ActionArgs
		{
			private bhCellAddress m_address;
			private boolean m_onlyCausedRefresh = false;
			
			public Args()
			{
				m_address = null;
			}
			
			public Args(bhCellAddress address)
			{
				m_address = address;
			}
			
			public void setAddress(bhCellAddress address)
			{
				m_address = address;
			}
			
			public boolean onlyCausedRefresh()
			{
				return m_onlyCausedRefresh;
			}
		}
		
		@Override
		public void perform(bhA_ActionArgs args)
		{
			bhCellAddress address = ((Args) args).m_address;
			StateMachine_Camera machine = this.getState();
			bhA_State currentState = machine.getCurrentState();
			
			if( currentState instanceof State_ViewingCell )
			{
				bhCellAddress viewingAddress = ((State_ViewingCell)currentState).getCell().getCellAddress();
				if( viewingAddress != null )
				{
					if( viewingAddress.isEqualTo(address) )
					{
						((State_ViewingCell)currentState).refreshCell();
						((Args) args).m_onlyCausedRefresh = true;
						
						return;
					}
				}
			}
			
			((Args) args).m_onlyCausedRefresh = false;
			((StateMachine_Camera)this.getState()).snapToCellAddress(address);
		}
		
		@Override
		public boolean isPerformable(bhA_ActionArgs args)
		{
			bhCellAddress address = ((Args) args).m_address;
			
			if( address.getParseError() != bhE_CellAddressParseError.NO_ERROR )
			{
				return false;
			}
			
			if( sm_c.addressMngr.isWaitingOnResponse(address) )
			{
				return false;
			}
			
			StateMachine_Camera machine = this.getState();
			bhA_State currentState = machine.getCurrentState();
			
			if( currentState instanceof State_CameraSnapping )
			{
				bhCellAddress snapTargetAddress = ((State_CameraSnapping)currentState).getTargetAddress();
				if( snapTargetAddress == null )
				{
					return true;
				}
				else
				{
					return !snapTargetAddress.isEqualTo(address);
				}
			}
			else
			{
				return true;
			}
		}
		
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return StateMachine_Camera.class;
		}
	}
	
	public static class OnAddressResponse extends bhA_EventAction
	{
		public static enum E_Type
		{
			ON_FOUND, ON_NOT_FOUND, ON_RESPONSE_ERROR
		}
		
		public static class Args extends bhA_ActionArgs
		{
			private final bhCellAddress m_address;
			private final bhCellAddressMapping m_mapping;
			private final E_Type m_responseType;
			
			Args(E_Type responseType, bhCellAddress address, bhCellAddressMapping mapping )
			{
				m_responseType = responseType;
				m_address = address;
				m_mapping = mapping;
			}
			
			public bhCellAddress getAddress()
			{
				return m_address;
			}
			
			public bhCellAddressMapping getMapping()
			{
				return m_mapping;
			}
			
			public E_Type getType()
			{
				return m_responseType;
			}
		}
		
		@Override
		public boolean isPerformable(bhA_ActionArgs args)
		{
			StateMachine_Camera machine = this.getState();
			bhA_State currentState = machine.getCurrentState();
			
			return currentState instanceof State_CameraSnapping || currentState instanceof State_ViewingCell;
		}
		
		@Override
		public boolean isPerformableInBackground()
		{
			return true;
		}
		
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return StateMachine_Camera.class;
		}
	}
	
	static final class PendingSnap
	{
		PendingSnap(bhGridCoordinate coordinate, bhCellAddress address)
		{
			m_coordinate = coordinate;
			m_address = address;
		}
		
		private final bhGridCoordinate m_coordinate;
		private final bhCellAddress m_address;
	}
	
	private PendingSnap m_pendingSnap = null;
	
	private final CameraManager m_cameraManager;
	private final bhCamera m_camera;
	
	private static final Logger s_logger = Logger.getLogger(StateMachine_Camera.class.getName());

	private final bhLocalCodeRepositoryWrapper m_codeRepo = new bhLocalCodeRepositoryWrapper();
	
	private final bhCellAddressManagerListener m_addressManagerListener = new bhCellAddressManagerListener(this);
	
	public StateMachine_Camera(double minSnapTime, double maxSnapTime)
	{
		m_camera = new bhCamera();
		m_cameraManager = new CameraManager(m_camera, minSnapTime, maxSnapTime);
		
		//TODO(DRK): Not the biggest fan of setting these this way...
		sm_c.camera = m_camera;
		sm_c.cameraMngr = m_cameraManager;
		
		bhA_Action.register(new SetCameraViewSize());
		bhA_Action.register(new SnapToAddress());
		bhA_Action.register(new SnapToCoordinate());
		bhA_Action.register(new OnAddressResponse());
		bhA_Action.register(new SetCameraTarget());
		bhA_Action.register(new SetInitialPosition());
		
		bhA_ClientUser user = sm_c.userMngr.getUser();
		m_codeRepo.addSource(user);
		m_codeRepo.addSource(sm_c.codeCache);
	}
	
	public bhCamera getCamera()
	{
		return m_camera;
	}
	
	private void snapToCellAddress(bhCellAddress address)
	{
		bhCellAddressManager addressManager = sm_c.addressMngr;
		bhA_State currentState = this.getCurrentState();
		
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
				m_cameraManager.internal_setTargetPosition(sm_c.camera.getPosition(), false);
			}
			
			State_GettingMapping.Constructor constructor = new State_GettingMapping.Constructor(address);
			machine_pushState(this, State_GettingMapping.class, constructor);
		}
		
		addressManager.getCellAddressMapping(address, bhE_TransactionAction.MAKE_REQUEST);
	}
	
	/**
	 * Address can be null, I think..
	 */
	void snapToCoordinate(bhCellAddress address_nullable, bhGridCoordinate coord)
	{
		if( !this.isForegrounded() )
		{
			this.m_pendingSnap = new PendingSnap(coord, address_nullable);
			
			return;
		}
		
		bhA_State currentState = this.getCurrentState();
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
	
	public bhI_LocalCodeRepository getCodeRepository()
	{
		return m_codeRepo;
	}
	
	@Override
	protected void didEnter(bhA_StateConstructor constructor)
	{
		//--- DRK > Not enforcing z constraints here because UI probably hasn't told us camera view size yet.
		
		bhA_ClientUser user = sm_c.userMngr.getUser();
		m_cameraManager.setCameraPosition(user.getLastPosition(), false);

		bhCellCodeManager.getInstance().start(new bhCellCodeManager.I_SyncOrPreviewDelegate()
		{
			@Override
			public void onCompilationFinished(bhCompilerResult result)
			{
				String title = null, body = null;
				
				if( result.getStatus() == bhE_CompilationStatus.NO_ERROR )
				{
					if( result.getMessages() != null )
					{
						title = "Warnings...fix them if you want.";
						body = bhCompilerErrorMessageGenerator.getInstance().generate(result);
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
					
					body = bhCompilerErrorMessageGenerator.getInstance().generate(result);
				}
				
				if( title != null )
				{
					StateMachine_Base baseController = bhA_State.getEnteredInstance(StateMachine_Base.class);
					
					State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor(title, body);
					baseController.queueAsyncDialog(State_AsyncDialog.class, constructor);
				}
			}
		});
		
		sm_c.addressMngr.start(m_addressManagerListener);
	}
	
	@Override
	protected void didForeground(Class<? extends bhA_State> revealingState, Object[] argsFromRevealingState)
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
		m_cameraManager.update(timeStep);
		
		if( !m_cameraManager.isCameraAtRest() )
		{
			updateBufferManager();
		}
	}
	
	private void updateBufferManager()
	{
		if( this.getCurrentState() instanceof State_CameraSnapping )
		{
			State_CameraSnapping snappingState = ((State_CameraSnapping)this.getCurrentState());
			bhI_LocalCodeRepository compiledStaticHtmlSource = snappingState.getCompiledStaticHtmlSource();
			
			int options = bhF_BufferUpdateOption.CREATE_VISUALIZATIONS;
			
			bhCellBufferManager.getInstance().update(sm_c.gridMngr.getGrid(), sm_c.camera, compiledStaticHtmlSource, options);
			
			//--- DRK > As soon as target cell comes into sight, we start trying to populate
			//---		it with compiled_dynamic and source html from the snapping state's html source(s).
			bhCellBuffer buffer = bhCellBufferManager.getInstance().getDisplayBuffer();
			if( buffer.getSubCellCount() == 1 )
			{
				if( buffer.isInBoundsAbsolute(snappingState.getTargetCoordinate()) )
				{
					bhBufferCell cell = buffer.getCellAtAbsoluteCoord(snappingState.getTargetCoordinate());
					bhI_LocalCodeRepository targetCellHtmlSource = snappingState.getHtmlSourceForTargetCell();

					bhCellCodeManager populator = bhCellCodeManager.getInstance();
					populator.populateCell(cell, targetCellHtmlSource, 1, false, false, bhE_CodeType.COMPILED);
					populator.populateCell(cell, targetCellHtmlSource, 1, false, false, bhE_CodeType.SOURCE);
					
					//--- DRK > NOTE: Don't need to flush populator because we're not telling it to communicate with server.
				}
			}
		}
		else
		{
			int options = bhF_BufferUpdateOption.ALL;
			bhCellBufferManager.getInstance().update(sm_c.gridMngr.getGrid(), sm_c.camera, m_codeRepo, options);
		}
	}
	
	protected void willExit()
	{
		sm_c.addressMngr.stop();
		
		bhCellCodeManager.getInstance().stop();
	}
	
	public bhCameraManager getCameraManager()
	{
		return m_cameraManager;
	}

	@Override
	public void onStateEvent(bhStateEvent event)
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
					sm_c.camera.onGridSizeChanged();
					
					updateBufferManager();
				}
				
				break;
			}
		}
	}
}