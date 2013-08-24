package swarm.client.ui.cell;

import java.util.logging.Logger;

import swarm.client.app.sm_c;
import swarm.client.entities.bhCamera;
import swarm.client.entities.bhBufferCell;
import swarm.client.managers.bhCellBuffer;
import swarm.client.managers.bhCellBufferManager;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_CameraSnapping;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.ui.bhE_ZIndex;
import swarm.client.ui.bhI_UIElement;
import swarm.client.ui.bhS_UI;
import swarm.shared.app.bhS_App;
import swarm.shared.debugging.bhU_Debug;
import swarm.shared.statemachine.bhA_Action;
import swarm.shared.statemachine.bhA_State;
import swarm.shared.statemachine.bhE_StateTimeType;
import swarm.shared.statemachine.bhStateEvent;
import swarm.shared.structs.bhGridCoordinate;
import swarm.shared.structs.bhPoint;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

public class bhVisualCellFocuser extends FlowPanel implements bhI_UIElement
{
	private enum AnimationState
	{
		NONE,
		FADING_IN,
		FOCUSED,
		FADING_OUT;
	};
	
	private static final Logger s_logger = Logger.getLogger(bhVisualCellFocuser.class.getName());
	
	private AnimationState m_animationState = AnimationState.NONE;
	
	private double m_alpha = 0;
	private double m_startAlpha = 0;
	private double m_fadeStartTime = 0;
	
	private double m_startCameraDistance = 0;
	
	private bhVisualCell m_poppedCell = null;
	private final bhGridCoordinate m_poppedCellCoord = new bhGridCoordinate();
	
	public bhVisualCellFocuser(Panel parent)
	{
		this.addStyleName("cell_focuser");
		
		this.getElement().setAttribute("ondragstart", "return false;");
		
		this.setVisible(false);
		
		bhE_ZIndex.CELL_FOCUSER.assignTo(this);
		
		parent.add(this);
	}
	
	private void setAlpha(double alpha)
	{
		m_alpha = alpha;
		this.getElement().getStyle().setBackgroundColor("rgba(0,0,0," + alpha + ")");
	}
	
	private boolean popUpTargetCell(bhA_State state)
	{
		bhCellBuffer buffer = bhCellBufferManager.getInstance().getDisplayBuffer();
		
		if( buffer.getSubCellCount() == 1 )
		{
			bhGridCoordinate targetCoord = null;
			
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
				bhU_Debug.ASSERT(false, "popUpTargetCell1");
				
				return false;
			}
			
			if( buffer.isInBoundsAbsolute(targetCoord) )
			{
				bhBufferCell cell = buffer.getCellAtAbsoluteCoord(targetCoord);
				bhVisualCell visualCell = (bhVisualCell) cell.getVisualization();
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
				m_startAlpha = m_alpha;
				
				switch( oldState )
				{
					case FADING_IN: // This is the only animation state that can be entered more than once in a row.
					{
						if( this.m_poppedCell != null )
						{
							this.m_poppedCell.pushDown();
							this.m_poppedCell = null;
						}
						
						break;
					}
					
					case FADING_OUT:
					{
						if( this.m_poppedCell != null )
						{
							StateMachine_Camera cameraController = bhA_State.getEnteredInstance(StateMachine_Camera.class);
							
							State_CameraSnapping snappingState = (State_CameraSnapping) cameraController.getCurrentState();
							
							bhGridCoordinate targetSnapCoord = snappingState.getTargetCoordinate();
							
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
						this.setVisible(true);
						
						break;
					}
				}
				break;
			}
			
			case FOCUSED:
			{
				this.setAlpha(bhS_UI.CELL_FOCUSER_MAX_ALPHA); // just to be sure.
				
				if( m_poppedCell == null )
				{
					//--- DRK > This can happen when directly transitioning from snap to viewing....fringe case,
					//---		and in the future (maybe as you're reading this!) possibly not allowed to begin with.
					//---
					//--- DRK > Note to past doug, this is future doug...it should now be an impossible case, with a
					//---		forced update in between snapping and viewing, but you never know.
					StateMachine_Camera cameraController = bhA_State.getEnteredInstance(StateMachine_Camera.class);
					this.popUpTargetCell(cameraController.getCurrentState());
				}
				
				break;
			}
			
			case FADING_OUT:
			{
				StateMachine_Camera cameraController = bhA_State.getEnteredInstance(StateMachine_Camera.class);
				
				m_fadeStartTime = cameraController.getTimeInState(bhE_StateTimeType.TOTAL);
				m_startAlpha = m_alpha;
				
				break;
			}
		}
	}
	
	private double calcCameraDistanceToTarget()
	{
		StateMachine_Camera cameraController = bhA_State.getEnteredInstance(StateMachine_Camera.class);
		bhPoint cameraPoint = sm_c.camera.getPosition();
		bhPoint cameraTarget = cameraController.getCameraManager().getTargetPosition();
		return cameraTarget.calcDistanceTo(cameraPoint);
	}
	
	private void updateAnimationState(bhA_State currentState)
	{
		switch( m_animationState)
		{
			case FADING_IN:
			{
				if( m_alpha == bhS_UI.CELL_FOCUSER_MAX_ALPHA )
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
						double alpha = m_startAlpha + alphaRatio * (bhS_UI.CELL_FOCUSER_MAX_ALPHA - m_startAlpha);
						
						this.setAlpha(alpha);
					}
				}
				
				break;
			}
			
			case FADING_OUT:
			{
				if( m_poppedCell != null )
				{
					bhCellBuffer buffer = bhCellBufferManager.getInstance().getDisplayBuffer();
					
					if( buffer.getSubCellCount() != 1 || !buffer.isInBoundsAbsolute(m_poppedCellCoord) )
					{
						m_poppedCell.pushDown();
						m_poppedCell = null;
					}
				}
				
				double elapsed = currentState.getParent().getTimeInState(bhE_StateTimeType.TOTAL) - m_fadeStartTime;
				double ratio = elapsed / bhS_UI.CELL_FOCUSER_FADE_OUT_TIME;
				
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

	public void onStateEvent(bhStateEvent event)
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
				if( event.getAction() == StateMachine_Camera.SnapToCoordinate.class )
				{
					StateMachine_Camera machine = bhA_State.getEnteredInstance(StateMachine_Camera.class);
					
					if( machine.getCurrentState() instanceof State_CameraSnapping )
					{
						if( machine.getCurrentState().getUpdateCount() > 0 )
						{
							//--- DRK > This covers the case of snapping to a new cell while already snapping.
							this.setAnimationState(AnimationState.FADING_IN);
						}
					}
					else
					{
						//--- DRK > Snapping to the same coordinate you're viewing is now allowed, which
						//---		doesn't trigger a state change, so this assert is now invalid.
						//bhU_Debug.ASSERT(false, "bhVisucellfoc1");
					}
				}
				
				break;
			}
		}
	}
}
