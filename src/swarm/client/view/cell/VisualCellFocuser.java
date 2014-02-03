package swarm.client.view.cell;

import java.util.logging.Logger;

import swarm.client.app.AppContext;
import swarm.client.entities.Camera;
import swarm.client.entities.BufferCell;
import swarm.client.managers.CellBuffer;
import swarm.client.managers.CellBufferManager;
import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_CameraSnapping;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.view.E_ZIndex;
import swarm.client.view.I_UIElement;
import swarm.client.view.S_UI;
import swarm.shared.app.S_CommonApp;
import swarm.shared.debugging.U_Debug;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.E_StateTimeType;
import swarm.shared.statemachine.StateContext;
import swarm.shared.statemachine.StateEvent;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

public class VisualCellFocuser extends FlowPanel implements I_UIElement
{
	private enum AnimationState
	{
		NONE,
		FADING_IN,
		FOCUSED,
		FADING_OUT;
	};
	
	private static final Logger s_logger = Logger.getLogger(VisualCellFocuser.class.getName());
	
	private AnimationState m_animationState = AnimationState.NONE;
	
	private double m_alpha = 0;
	private double m_startAlpha = 0;
	private double m_fadeStartTime = 0;
	
	private double m_startCameraDistance = 0;
	
	private VisualCell m_poppedCell = null;
	private final GridCoordinate m_poppedCellCoord = new GridCoordinate();
	
	private final AppContext m_appContext;
	private final StateContext m_stateContext;
	
	public VisualCellFocuser(StateContext stateContext, AppContext appContext)
	{
		m_appContext = appContext;
		m_stateContext = stateContext;
		
		this.addStyleName("cell_focuser");
		
		this.getElement().setAttribute("ondragstart", "return false;");
		
		this.setVisible(false);
		
		E_ZIndex.CELL_FOCUSER.assignTo(this);
	}
	
	private void setAlpha(double alpha)
	{
		m_alpha = alpha;
		this.getElement().getStyle().setBackgroundColor("rgba(0,0,0," + alpha + ")");
	}
	
	private boolean popUpTargetCell(A_State state)
	{
		CellBuffer buffer = m_appContext.cellBufferMngr.getDisplayBuffer();
		
		if( buffer.getSubCellCount() == 1 )
		{
			GridCoordinate targetCoord = null;
			
			if( state instanceof State_CameraSnapping )
			{
				targetCoord = ((State_CameraSnapping)state).getTargetCoordinate();
			}
			else if( state instanceof State_ViewingCell )
			{
				targetCoord = ((State_ViewingCell)state).getCell().getCoordinate();
			}
			
			if( targetCoord == null )
			{
				U_Debug.ASSERT(false, "popUpTargetCell1");
				
				return false;
			}
			
			if( buffer.isInBoundsAbsolute(targetCoord) )
			{
				BufferCell cell = buffer.getCellAtAbsoluteCoord(targetCoord);
				VisualCell visualCell = (VisualCell) cell.getVisualization();
				visualCell.popUp();
				m_poppedCell = visualCell;
				m_poppedCellCoord.copy(cell.getCoordinate());
				
				return true;
			}
		}
		
		return false;
	}
	
	private void setAnimationState(AnimationState state)
	{
		AnimationState oldState = m_animationState;
		m_animationState = state;
		
		switch(m_animationState )
		{
			case NONE:
			{
				if( this.m_poppedCell != null )
				{
					this.m_poppedCell.pushDown();
					this.m_poppedCell = null;
				}
				
				this.setVisible(false);
				
				this.setAlpha(0); // just to be sure.
				
				break;
			}
			
			case FADING_IN:
			{
				switch( oldState )
				{
					case FADING_IN: // This is the only animation state that can be entered more than once in a row.
					{
						if( this.m_poppedCell != null )
						{
							State_CameraSnapping snappingState = m_stateContext.getEnteredState(State_CameraSnapping.class);
							
							if( snappingState == null || !snappingState.getTargetCoordinate().isEqualTo(m_poppedCellCoord))
							{
								this.m_poppedCell.pushDown();
								this.m_poppedCell = null;
								m_startAlpha = m_alpha;
							}
						}
						
						break;
					}
					
					case FADING_OUT:
					{
						m_startAlpha = m_alpha;
						
						if( this.m_poppedCell != null )
						{
							StateMachine_Camera cameraController = m_stateContext.getEnteredState(StateMachine_Camera.class);
							
							State_CameraSnapping snappingState = (State_CameraSnapping) cameraController.getCurrentState();
							
							GridCoordinate targetSnapCoord = snappingState.getTargetCoordinate();
							
							if( !targetSnapCoord.isEqualTo(m_poppedCellCoord) )
							{
								this.m_poppedCell.pushDown();
								this.m_poppedCell = null;
							}
							else
							{
								m_startCameraDistance = this.calcCameraDistanceToTarget();
							}
						}
						
						break;
					}
					
					case NONE:
					{
						m_startAlpha = m_alpha;
						this.setVisible(true);
						
						break;
					}
				}
				break;
			}
			
			case FOCUSED:
			{
				this.setAlpha(S_UI.CELL_FOCUSER_MAX_ALPHA); // just to be sure.
				
				if( m_poppedCell == null )
				{
					//--- DRK > This can happen when directly transitioning from snap to viewing....fringe case,
					//---		and in the future (maybe as you're reading this!) possibly not allowed to begin with.
					//---
					//--- DRK > Note to past doug, this is future doug...it should now be an impossible case, with a
					//---		forced update in between snapping and viewing, but you never know.
					StateMachine_Camera cameraController = m_stateContext.getEnteredState(StateMachine_Camera.class);
					this.popUpTargetCell(cameraController.getCurrentState());
				}
				
				break;
			}
			
			case FADING_OUT:
			{
				StateMachine_Camera cameraController = m_stateContext.getEnteredState(StateMachine_Camera.class);
				
				m_fadeStartTime = cameraController.getTimeInState(E_StateTimeType.TOTAL);
				m_startAlpha = m_alpha;
				
				break;
			}
		}
	}
	
	private double calcCameraDistanceToTarget()
	{
		StateMachine_Camera cameraController = m_stateContext.getEnteredState(StateMachine_Camera.class);
		Point cameraPoint = m_appContext.cameraMngr.getCamera().getPosition();
		Point cameraTarget = m_appContext.cameraMngr.getTargetPosition();
		return cameraTarget.calcDistanceTo(cameraPoint);
	}
	
	private void updateAnimationState(A_State currentState)
	{
		switch( m_animationState)
		{
			case FADING_IN:
			{
				if( m_alpha == S_UI.CELL_FOCUSER_MAX_ALPHA )
				{
					if( m_poppedCell == null )
					{
						this.popUpTargetCell(currentState);
					}
				}
				else
				{
					double cameraDistance = calcCameraDistanceToTarget();
					
					if( m_poppedCell == null )
					{
						if( this.popUpTargetCell(currentState) )
						{
							m_startCameraDistance = cameraDistance;
						}
					}

					if( m_poppedCell != null )
					{
						double alphaRatio = (1 - (cameraDistance / m_startCameraDistance));
						double alpha = m_startAlpha + alphaRatio * (S_UI.CELL_FOCUSER_MAX_ALPHA - m_startAlpha);
						
						this.setAlpha(alpha);
					}
				}
				
				break;
			}
			
			case FADING_OUT:
			{
				if( m_poppedCell != null )
				{
					CellBuffer buffer = m_appContext.cellBufferMngr.getDisplayBuffer();
					
					if( buffer.getSubCellCount() != 1 || !buffer.isInBoundsAbsolute(m_poppedCellCoord) )
					{
						m_poppedCell.pushDown();
						m_poppedCell = null;
					}
				}
				
				double elapsed = currentState.getParent().getTimeInState(E_StateTimeType.TOTAL) - m_fadeStartTime;
				double ratio = elapsed / S_UI.CELL_FOCUSER_FADE_OUT_TIME;
				
				if( ratio >= 1 )
				{
					this.setAnimationState(AnimationState.NONE);
					return;
				}
				
				double totalAlphaToFade = m_startAlpha;
				this.setAlpha(m_startAlpha - totalAlphaToFade*ratio);
				
				break;
			}
		}
	}

	public void onStateEvent(StateEvent event)
	{
		switch( event.getType() )
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof State_ViewingCell )
				{
					this.setAnimationState(AnimationState.FOCUSED);
				}
				else if( event.getState() instanceof State_CameraSnapping )
				{
					this.setAnimationState(AnimationState.FADING_IN);
				}
				
				break;
			}
			
			case DID_EXIT:
			{
				if( event.getState() instanceof State_ViewingCell )
				{
					this.setAnimationState(AnimationState.FADING_OUT);
				}
				else if( event.getState() instanceof State_CameraSnapping )
				{
					//--- DRK > If we're exiting the snapping state because we're entering the viewing state, then
					//---		this call to set the animation state is ignored.
					
					//TODO: Minor thing, but perhaps check here if the viewing state is entered, and if so, don't
					//		set the animation state.
					this.setAnimationState(AnimationState.FADING_OUT);
				}
				
				break;
			}
			
			case DID_UPDATE:
			{
				if( event.getState().getParent() instanceof StateMachine_Camera )
				{
					this.updateAnimationState(event.getState());
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == Action_Camera_SnapToCoordinate.class )
				{
					State_CameraSnapping state = event.getContext().getEnteredState(State_CameraSnapping.class);
					
					if( state != null )
					{
						if( state.getUpdateCount() > 0 )
						{
							//--- DRK > This covers the case of snapping to a new cell while already snapping.
							this.setAnimationState(AnimationState.FADING_IN);
						}
					}
					else
					{
						//--- DRK > Snapping to the same coordinate you're viewing is now allowed, which
						//---		doesn't trigger a state change, so this assert is now invalid.
						//smU_Debug.ASSERT(false, "smVisucellfoc1");
					}
				}
				
				break;
			}
		}
	}
}