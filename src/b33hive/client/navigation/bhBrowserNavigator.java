package b33hive.client.navigation;

import java.util.logging.Logger;

import b33hive.client.app.bh_c;
import b33hive.client.entities.bhBufferCell;
import b33hive.client.entities.bhCamera;
import b33hive.client.input.bhBrowserHistoryManager;
import b33hive.client.input.bhBrowserAddressManager;
import b33hive.client.managers.bhCameraManager;
import b33hive.client.states.camera.StateMachine_Camera;
import b33hive.client.states.camera.State_CameraFloating;
import b33hive.client.states.camera.State_CameraSnapping;
import b33hive.client.states.camera.State_GettingMapping;
import b33hive.client.states.camera.State_ViewingCell;
import b33hive.client.ui.tooltip.bhToolTipManager;
import b33hive.client.ui.widget.bhMagnifier;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.statemachine.bhE_StateTimeType;
import b33hive.shared.statemachine.bhI_StateEventListener;
import b33hive.shared.statemachine.bhStateEvent;
import b33hive.shared.structs.bhCellAddress;
import b33hive.shared.structs.bhCellAddressMapping;
import b33hive.shared.structs.bhE_CellAddressParseError;
import b33hive.shared.structs.bhE_GetCellAddressMappingError;
import b33hive.shared.structs.bhGetCellAddressMappingResult;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.structs.bhPoint;

/**
 * Responsible for piping user navigation to the state machine via the browser back/forward/refresh buttons and address bar.
 * 
 * @author Doug
 *
 */
public class bhBrowserNavigator implements bhI_StateEventListener
{
	private static final Logger s_logger = Logger.getLogger(bhBrowserNavigator.class.getName());
	
	private final static String FLOATING_STATE_PATH = "/";
	
	private final bhHistoryStateManager m_historyManager;
	private final bhBrowserAddressManager m_addressManager;

	private StateMachine_Camera.OnAddressResponse.Args 			m_args_OnAddressResponse	= null;
	private State_GettingMapping.OnResponse.Args	 			m_args_OnMappingResponse	= null;
	private final StateMachine_Camera.SetCameraTarget.Args		m_args_SetCameraTarget		= new StateMachine_Camera.SetCameraTarget.Args();
	private final StateMachine_Camera.SetInitialPosition.Args	m_args_SetInitialPosition 	= new StateMachine_Camera.SetInitialPosition.Args();
	private final StateMachine_Camera.SnapToAddress.Args 		m_args_SnapToAddress		= new StateMachine_Camera.SnapToAddress.Args();
	private final StateMachine_Camera.SnapToCoordinate.Args 	m_args_SnapToCoordinate		= new StateMachine_Camera.SnapToCoordinate.Args();

	
	private Class<? extends bhA_Action> m_lastSnapAction = null;
	private boolean m_pushHistoryStateForFloating = true;
	private boolean m_receivedFloatingStateEntered = false;
	private boolean m_stateAlreadyPushedForViewingExit = false;
	
	private double m_lastTimeFloatingStateSet = 0;
	
	private final bhHistoryStateManager.I_Listener m_historyStateListener;
	
	private final double m_floatingHistoryUpdateRate;
	
	bhBrowserNavigator(double floatingHistoryUpdateRate_seconds)
	{
		m_floatingHistoryUpdateRate = floatingHistoryUpdateRate_seconds;
		
		m_args_SnapToCoordinate.setUserData(this.getClass());
		m_args_SetCameraTarget.setUserData(this.getClass());
		
		m_historyStateListener = new bhHistoryStateManager.I_Listener()
		{
			@Override
			public void onStateChange(String path, bhHistoryState state)
			{
				if( state == null )
				{
					bhU_Debug.ASSERT(false, "History state shouldn't be null.");
					
					return;
				}
				
				if( state.getMapping() != null )
				{
					m_args_SnapToCoordinate.setCoordinate(state.getMapping().getCoordinate());
					
					if( !bhA_Action.isPerformable(StateMachine_Camera.SnapToCoordinate.class, m_args_SnapToCoordinate) )
					{
						m_historyManager./*re*/pushState(path, state.getMapping());
					}
					else
					{
						bhA_Action.perform(StateMachine_Camera.SnapToCoordinate.class, m_args_SnapToCoordinate);
					}
				}
				else if( state.getPoint() != null )
				{
					if( !bhA_Action.isPerformable(StateMachine_Camera.SetCameraTarget.class) )
					{
						m_historyManager./*re*/pushState(path, state.getPoint());
					}
					else
					{
						//--- DRK > Always try to set camera's initial position first.  This can essentially only be done
						//---		at app start up.  The rest of the time it will fail and we'll set camera target normally.
						m_args_SetInitialPosition.setPoint(state.getPoint());
						if( bhA_Action.perform(StateMachine_Camera.SetInitialPosition.class, m_args_SetInitialPosition) )
						{
							//s_logger.info("SETTING INITIAL POINT: " + state.getPoint());
						}
						else
						{
							m_args_SetCameraTarget.setPoint(state.getPoint());
							bhA_Action.perform(StateMachine_Camera.SetCameraTarget.class, m_args_SetCameraTarget);
							
							//s_logger.info("SETTING TARGET POINT: " + state.getPoint());
						}
					}
				}
				else
				{
					//--- DRK > Should mean that we navigated to b33hive.net/blahblah from other domain,
					//---		pressed backwards (or whatever) while still snapping to blahblah, then pressed forward
					//---		once again.
				}
			}
		};
		
		m_historyManager = new bhHistoryStateManager(bh_c.app.getViewConfig().defaultPageTitle, m_historyStateListener);
		
		m_addressManager = new bhBrowserAddressManager();
	}
	
	public void go(int offset)
	{
		m_historyManager.go(offset);
	}
	
	private bhCellAddress getBrowserCellAddress()
	{
		String path = m_addressManager.getCurrentPathLowercased();
		bhCellAddress address = new bhCellAddress(path);
		bhE_CellAddressParseError parseError = address.getParseError();
		
		if( parseError != bhE_CellAddressParseError.EMPTY )
		{
			return address;
		}
		else
		{
			return null;
		}
	}
	
	@Override
	public void onStateEvent(bhStateEvent event)
	{
		switch(event.getType())
		{
			case DID_ENTER:
			{
				if ( event.getState() instanceof StateMachine_Camera )
				{
					bhHistoryState currentHistoryState = m_historyManager.getCurrentState();
					bhCellAddress address = getBrowserCellAddress();
					
					if( currentHistoryState == null || currentHistoryState.isEmpty() )
					{
						if( address != null )
						{
							m_pushHistoryStateForFloating = false;
							m_stateAlreadyPushedForViewingExit = true;
							
							m_historyManager.setState(address.getRawAddress(), new bhHistoryState()); // set empty state
							m_args_SnapToAddress.setAddress(address);
							event.getState().performAction(StateMachine_Camera.SnapToAddress.class, m_args_SnapToAddress);
						}
						else
						{
							//--- DRK > Just make sure here that the address bar is "clean".
							//---		This should get rid of things like url parameters, hash tags, etc.,
							//---		if for some strange reason the user put them there.
							m_pushHistoryStateForFloating = false;
							m_historyManager.setState(FLOATING_STATE_PATH, bh_c.camera.getPosition());
						}
					}
					
					//--- DRK > This case essentially manually fires a state change event when we're coming from a different domain.
					else
					{
						String path = address == null ? FLOATING_STATE_PATH : address.getRawAddress();
						
						m_historyStateListener.onStateChange(path, currentHistoryState);
					}
				}
				else if( event.getState() instanceof State_ViewingCell )
				{					
					if( m_lastSnapAction == null )
					{
						//--- DRK > This case implies that browser navigation (pressing forward/backward)
						//---		was the cause of entering this state.
						//---		There is a case where you can manually snap to a cell, get there, but 
						//---		address hasn't come in yet, then you navigate away, then the address comes in.  If you press the back
						//---		button to return to the cell, the address won't show unless we manually set it here.
						State_ViewingCell state = event.getState();
						bhBufferCell cell = state.getCell();
						bhCellAddress address = cell.getCellAddress();
						if( address != null )
						{
							m_historyManager.setState(address.getRawAddress(), new bhCellAddressMapping(cell.getCoordinate()));
						}
					}
					else
					{
						if( m_lastSnapAction == StateMachine_Camera.SnapToCoordinate.class )
						{
							bhU_Debug.ASSERT(m_args_OnMappingResponse == null, "bh_bro_nav_112387");
							
							boolean pushEmptyState = false;
							bhCellAddressMapping mapping = null;
							if( m_args_OnAddressResponse != null )
							{
								mapping = m_args_OnAddressResponse.getMapping();
							
								if( m_args_OnAddressResponse.getType() == StateMachine_Camera.OnAddressResponse.E_Type.ON_FOUND )
								{
									bhHistoryState state = new bhHistoryState(m_args_OnAddressResponse.getMapping());
									
									if( m_stateAlreadyPushedForViewingExit )
									{
										m_historyManager.setState(m_args_OnAddressResponse.getAddress(), state);
									}
									else
									{
										m_historyManager.pushState(m_args_OnAddressResponse.getAddress(), state);
									}
								}
								else
								{
									pushEmptyState = true;
								}
							}
							else
							{
								mapping = new bhCellAddressMapping(((State_ViewingCell)event.getState()).getCell().getCoordinate());
								
								pushEmptyState = true;
							}
							
							if( pushEmptyState )
							{
								if( m_stateAlreadyPushedForViewingExit )
								{
									m_historyManager.setState(FLOATING_STATE_PATH, mapping);
								}
								else
								{
									m_historyManager.pushState(FLOATING_STATE_PATH, mapping);
								}
							}
						}
						else if( m_lastSnapAction == StateMachine_Camera.SnapToAddress.class )
						{
							bhU_Debug.ASSERT(m_args_OnAddressResponse == null, "bh_bro_nav_112387");
							
							if( m_args_OnMappingResponse != null )
							{
								if( m_args_OnMappingResponse.getType() == State_GettingMapping.OnResponse.E_Type.ON_FOUND )
								{
									if( m_historyManager.getCurrentState() == null )
									{
										bhCellAddress address = this.getBrowserCellAddress();
										if( address != null )
										{
											m_historyManager.setState(address.getRawAddress(), m_args_OnMappingResponse.getMapping());
										}
										else
										{
											bhU_Debug.ASSERT(false, "with current history state null with last snap action being to address, browser address should have existed");
										}
										
									}
									else
									{
										if( m_stateAlreadyPushedForViewingExit )
										{
											m_historyManager.setState(m_args_OnMappingResponse.getAddress().getRawAddress(), m_args_OnMappingResponse.getMapping());
										}
										else
										{
											m_historyManager.pushState(m_args_OnMappingResponse.getAddress().getRawAddress(), m_args_OnMappingResponse.getMapping());
										}
									}
								}
								else
								{
									bhU_Debug.ASSERT(false, "bh_bro_nav_asaswwewe");
								}
							}
							else
							{
								bhU_Debug.ASSERT(false, "bh_bro_nav_87654332");
							}
						}
						else
						{
							bhU_Debug.ASSERT(false, "bh_bro_nav_193498");
						}
					}
					
					m_args_OnAddressResponse = null;
					m_args_OnMappingResponse = null;
					m_lastSnapAction = null;
					m_stateAlreadyPushedForViewingExit = false;
				}
				else if( event.getState() instanceof State_CameraFloating )
				{
					m_receivedFloatingStateEntered = true;
					m_lastTimeFloatingStateSet = 0;
					
					if( m_historyManager.getCurrentState() == null )
					{
						m_historyManager.setState(FLOATING_STATE_PATH, bh_c.camera.getPosition());
					}
					else
					{
						if( m_pushHistoryStateForFloating )
						{
							StateMachine_Camera machine = bhA_State.getEnteredInstance(StateMachine_Camera.class);
							
							if( m_stateAlreadyPushedForViewingExit || event.getState().getPreviousState() == State_CameraSnapping.class )
							{
								m_historyManager.setState(FLOATING_STATE_PATH, machine.getCameraManager().getTargetPosition());
							}
							else
							{
								m_historyManager.pushState(FLOATING_STATE_PATH, machine.getCameraManager().getTargetPosition());
							}
						}
					}
				}
				else if( event.getState() instanceof State_CameraSnapping )
				{
					if( event.getState().getPreviousState() == State_ViewingCell.class )
					{
						//StateMachine_Camera machine = bhA_State.getEnteredInstance(StateMachine_Camera.class);
						
						if( m_lastSnapAction != null )
						{
							m_historyManager.pushState(FLOATING_STATE_PATH, bh_c.camera.getPosition());
							
							m_stateAlreadyPushedForViewingExit = true;
						}
					}
				}
				
				break;
			}
			
			case DID_UPDATE:
			{
				if( event.getState() instanceof State_CameraFloating )
				{
					if( event.getState().isEntered() ) // just to make sure
					{
						if( bh_c.cameraMngr.didCameraJustComeToRest() )
						{
							this.setPositionForFloatingState(event.getState(), bh_c.camera.getPosition(), true);
						}
					}
				}
				
				break;
			}
			
			case DID_EXIT:
			{
				if( event.getState() instanceof State_CameraFloating )
				{
					//--- DRK > A null snap action implies that a browser back/forward event initiated the state change,
					//---		not a user action within the app...in this case, it's too late to set the "floating" history
					//---		state because we're already in the previous or next history state. Unfortunately the history
					//---		API doesn't tell us when we *will* change history states, only that we already have.
					if( m_lastSnapAction != null )
					{
						//--- DRK > Kind of a hack here to prevent the browser URL path from being temporarily cleared when
						//---		you navigate from a different page...if this check isn't here, the path goes like
						//---		mydomain.com/mypath, mydomain.com while snapping, then mydomain.com/mypath again.
						//---		It would be better if the statemachine didn't enter the floating state initially.
						bhA_State state = bhA_State.getEnteredInstance(StateMachine_Camera.class);
						if( state != null && state.getUpdateCount() > 0 ) // dunno why it would be null, just being paranoid before a deploy
						{
							this.setPositionForFloatingState(event.getState(), bh_c.camera.getPosition(), true);
						}
						
						m_receivedFloatingStateEntered = false;
					}
				}
				
				break;
			}

			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == StateMachine_Camera.OnAddressResponse.class )
				{
					StateMachine_Camera.OnAddressResponse.Args args = event.getActionArgs();
					
					if( bhA_State.isEntered(State_CameraSnapping.class) )
					{
						m_args_OnAddressResponse = args;
					}
					else if( bhA_State.isEntered(State_ViewingCell.class) )
					{
						m_args_OnAddressResponse = null;
						
						if( args.getType() == StateMachine_Camera.OnAddressResponse.E_Type.ON_FOUND )
						{
							m_historyManager.setState(args.getAddress().getRawAddress(), args.getMapping());
						}
					}
					else
					{
						bhU_Debug.ASSERT(false, "bh_nav_1");
					}
				}
				else if( event.getAction() == State_GettingMapping.OnResponse.class )
				{
					State_GettingMapping.OnResponse.Args args = event.getActionArgs();

					m_args_OnMappingResponse = args;
					
					if( args.getType() != State_GettingMapping.OnResponse.E_Type.ON_FOUND )
					{
						//--- DRK > This takes care of the case where a user navigates to a cell with a valid address format
						//---		through the url bar, but the address can't be resolved, so we just wipe the url bar completely.
						m_historyManager.setState(FLOATING_STATE_PATH, bh_c.camera.getPosition());
					}
				}
				else if( event.getAction() == StateMachine_Camera.SnapToAddress.class ||
						 event.getAction() == StateMachine_Camera.SnapToCoordinate.class )
				{
					m_args_OnAddressResponse = null;
					m_args_OnMappingResponse = null;
					
					if( event.getAction() == StateMachine_Camera.SnapToCoordinate.class )
					{
						StateMachine_Camera.SnapToCoordinate.Args args = event.getActionArgs();
						
						Object userData = event.getActionArgs().getUserData();
						if( userData == this.getClass() ) // signifies that snap was because of browser navigation event.
						{
							m_lastSnapAction = null;
							
							return;
						}
						
						if( args.onlyCausedRefresh() )
						{
							m_lastSnapAction = null;
							
							return;
						}
					}
					else if( event.getAction() == StateMachine_Camera.SnapToAddress.class )
					{
						StateMachine_Camera.SnapToAddress.Args args = event.getActionArgs();
						
						if( args.onlyCausedRefresh() )
						{
							m_lastSnapAction = null;
							
							return;
						}
					}
					
					m_lastSnapAction = event.getAction();
				}
				else if( event.getAction() == StateMachine_Camera.SetCameraTarget.class )
				{
					State_CameraFloating floatingState = bhA_State.getEnteredInstance(State_CameraFloating.class);
					
					m_pushHistoryStateForFloating = true;
					
					if( floatingState != null )
					{
						StateMachine_Camera.SetCameraTarget.Args args = event.getActionArgs();
						
						if( args != null )
						{
							m_pushHistoryStateForFloating = args.getUserData() != this.getClass();
							
							if( m_receivedFloatingStateEntered )
							{
								bhPoint point = args.getPoint();
								
								if( point != null )
								{
									//setPositionForFloatingState(floatingState, point, false);
								}
							}
						}
					}
					else
					{
						bhU_Debug.ASSERT(false, "floating state should have been entered.");
					}
				}
				
				break;
			}
		}
	}
	
	private void setPositionForFloatingState(bhA_State state, bhPoint point, boolean force)
	{
		double timeInState = state.getTimeInState(bhE_StateTimeType.TOTAL);
		if( force || timeInState - m_lastTimeFloatingStateSet >= m_floatingHistoryUpdateRate )
		{
			m_historyManager.setState(FLOATING_STATE_PATH, point);
			
			m_lastTimeFloatingStateSet = timeInState;
		}
	}
}
