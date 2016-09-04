package swarm.client.view.tooltip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import swarm.client.input.SimpleMouse;
import swarm.client.view.E_ZIndex;
import swarm.client.view.S_UI;
import swarm.client.view.alignment.AlignmentDefinition;
import swarm.client.view.alignment.E_AlignmentPosition;
import swarm.client.view.alignment.E_AlignmentType;
import swarm.client.view.alignment.U_Alignment;
import swarm.shared.debugging.U_Debug;
import swarm.shared.structs.Point;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class ToolTipManager
{
	private static final Logger s_logger = Logger.getLogger(ToolTipManager.class.getName());
	
	private final I_ToolTipSubManager[] m_subManagers;
	
	private final ArrayList<ToolTip> m_activeTips = new ArrayList<ToolTip>();
	
	private final SimpleMouse m_mouse = new SimpleMouse(RootPanel.get());
	
	private final Integer[] m_defaultZIndeces = new Integer[E_ToolTipType.values().length];
	
	private Double m_defaultPadding = null;
	
	private final AlignmentDefinition m_reusedAlignment = new AlignmentDefinition();
	
	private final boolean m_isMouseOverEnabled;
	
	public ToolTipManager(boolean enabled, int mouseOverDelayMilliseconds)
	{
		m_isMouseOverEnabled = enabled;
		
		m_subManagers = new I_ToolTipSubManager[E_ToolTipType.values().length];
		RootPanel.get().addDomHandler(new MouseDownHandler()
		{
			@Override
			public void onMouseDown(MouseDownEvent event)
			{
				for( int i = 0; i < m_subManagers.length; i++ )
				{
					m_subManagers[i].onGlobalMouseDown(event);
				}
			}
	
		}, MouseDownEvent.getType());
		
		m_subManagers[E_ToolTipType.FOCUS.ordinal()]			= new ToolTipSubManager_Focus(this);
		m_subManagers[E_ToolTipType.MOUSE_OVER.ordinal()]		= new ToolTipSubManager_MouseOver(this, mouseOverDelayMilliseconds);
		m_subManagers[E_ToolTipType.NOTIFICATION.ordinal()]	= new ToolTipSubManager_Notification(this);
		m_subManagers[E_ToolTipType.TUTORIAL.ordinal()]		= new ToolTipSubManager_Tutorial(this);
		m_subManagers[E_ToolTipType.STATUS.ordinal()]			= new ToolTipSubManager_Status(this);
	}
	
	private void setAlignmentDefaults(ToolTip toolTip, AlignmentDefinition alignment, Point mousePoint)
	{
		//--- DRK > Set up defaults for alignment.
		if( mousePoint != null )
		{
			E_AlignmentPosition position = alignment.getPosition(E_AlignmentType.MASTER_ANCHOR_HORIZONTAL);
			
			if( position == null || position == E_AlignmentPosition.DEFINED )
			{
				int toolTipWidth = toolTip.getElement().getClientWidth();
				int toolTipHeight = toolTip.getElement().getClientHeight();
				int screenHeight = RootPanel.get().getOffsetHeight();
				int screenWidth = RootPanel.get().getOffsetWidth();
				
				int positionY = (int) (mousePoint.getY() + S_UI.CURSOR_HEIGHT + m_defaultPadding);
				
				if( positionY + toolTipHeight + m_defaultPadding > screenHeight )
				{
					m_reusedAlignment.setPosition(E_AlignmentType.MASTER_ANCHOR_VERTICAL, E_AlignmentPosition.LEFT_OR_TOP);
					m_reusedAlignment.setPosition(E_AlignmentType.SLAVE_ANCHOR_VERTICAL, E_AlignmentPosition.RIGHT_OR_BOTTOM);
				}
				else
				{
					m_reusedAlignment.setDefinedPosition(E_AlignmentType.MASTER_ANCHOR_VERTICAL, (double) positionY);
					m_reusedAlignment.setPosition(E_AlignmentType.SLAVE_ANCHOR_VERTICAL, E_AlignmentPosition.LEFT_OR_TOP);
				}
				
				if( mousePoint.getX() + toolTipWidth + m_defaultPadding > screenWidth )
				{
					m_reusedAlignment.setDefinedPosition(E_AlignmentType.MASTER_ANCHOR_HORIZONTAL, (double)screenWidth - m_defaultPadding);
					m_reusedAlignment.setPosition(E_AlignmentType.SLAVE_ANCHOR_HORIZONTAL, E_AlignmentPosition.RIGHT_OR_BOTTOM);
				}
				else
				{
					alignment.setDefinedPosition(E_AlignmentType.MASTER_ANCHOR_HORIZONTAL, mousePoint.getX());
				}
			}
		}
		
		if( alignment.getPosition(E_AlignmentType.MASTER_ANCHOR_HORIZONTAL) == null )
		{
			alignment.setPosition(E_AlignmentType.MASTER_ANCHOR_HORIZONTAL, E_AlignmentPosition.LEFT_OR_TOP);
		}
		if( alignment.getPosition(E_AlignmentType.MASTER_ANCHOR_VERTICAL) == null )
		{
			alignment.setPosition(E_AlignmentType.MASTER_ANCHOR_VERTICAL, E_AlignmentPosition.RIGHT_OR_BOTTOM);
		}
		
		
		if( alignment.getPosition(E_AlignmentType.SLAVE_ANCHOR_HORIZONTAL) == null )
		{
			alignment.setPosition(E_AlignmentType.SLAVE_ANCHOR_HORIZONTAL, E_AlignmentPosition.LEFT_OR_TOP);
		}
		if( alignment.getPosition(E_AlignmentType.SLAVE_ANCHOR_VERTICAL) == null )
		{
			alignment.setPosition(E_AlignmentType.SLAVE_ANCHOR_VERTICAL, E_AlignmentPosition.LEFT_OR_TOP);
			alignment.setPadding(E_AlignmentType.SLAVE_ANCHOR_VERTICAL, m_defaultPadding);
		}
	}
	
	void showTip(ToolTip toolTip, Point mousePoint)
	{
		Element targetElement = toolTip.getTargetElement();
		ToolTipConfig config = toolTip.getConfig();
		AlignmentDefinition alignment = config.getAlignmentDefinition();
		
		if( alignment != null )
		{
			m_reusedAlignment.copy(alignment);
		}
		else
		{
			m_reusedAlignment.clear();
		}
	
		setAlignmentDefaults(toolTip, m_reusedAlignment, mousePoint);
		
		U_Alignment.performAlignment(targetElement, toolTip.getElement(), m_reusedAlignment);
		/*
		int targetElementBottom = targetElement.getAbsoluteBottom();
		int targetElementHeight = targetElement.getClientHeight();

		int x = (int) (mousePoint != null ? mousePoint.getX() : targetElement.getAbsoluteRight() + offset);
		int y = mousePoint != null ? targetElementBottom + offset : targetElement.getAbsoluteTop() + targetElementHeight/2 - toolTipHeight/2;
		
		if( mousePoint != null )
		{
			if( x + toolTipWidth + offset > screenWidth )
			{
				x = screenWidth - offset - toolTipWidth;
			}
			
			if( x < offset )
			{
				x = offset;
			}
			
			int mouseY = (int) (mousePoint.getY() + smS_UI.CURSOR_HEIGHT + offset);
			
			if( y < mouseY )
			{
				y = mouseY;
			}
			
			if( (y + toolTipHeight + offset) > screenHeight )
			{
				y = targetElement.getAbsoluteTop() - offset - toolTipHeight;
			}
		}

		toolTip.getElement().getStyle().setLeft(x, Unit.PX);
		toolTip.getElement().getStyle().setTop(y, Unit.PX);*/
		
		toolTip.getElement().getStyle().setVisibility(Visibility.VISIBLE);
	}
	
	void prepareTip(ToolTip toolTip, Element targetElement, ToolTipConfig config)
	{
		toolTip.getElement().getStyle().setVisibility(Visibility.HIDDEN);
		
		E_ZIndex.TOOL_TIP_1.assignTo(toolTip, config.getType());
		
		if( toolTip.getParent() == null )
		{
			RootPanel.get().add(toolTip);
			
			U_Debug.ASSERT(m_activeTips.indexOf(toolTip) == -1, "prepareTip1");
			
			m_activeTips.add(toolTip);
		}
		
		toolTip.init(targetElement, config);
	}
	
	void endTip(ToolTip toolTip)
	{
		if( toolTip.getParent() != null )
		{
			toolTip.removeFromParent();
			
			toolTip.deinit();
			
			int index = m_activeTips.indexOf(toolTip);
			
			U_Debug.ASSERT(index != -1, "endTip1");
			
			if( index >= 0 )
			{
				m_activeTips.remove(index);
			}
		}
	}
	
	public void update(double timeStep)
	{
		for( int i = 0; i < m_subManagers.length; i++ )
		{
			m_subManagers[i].update(timeStep);
		}
	}
	
	public void onMouseUp(MouseUpEvent event)
	{
		for( int i = 0; i < m_subManagers.length; i++ )
		{
			m_subManagers[i].onExternalMouseUp(event);
		}
	}
	
	public void onTipMove(IsWidget widget)
	{
		for( int i = 0; i < m_activeTips.size(); i++ )
		{
			if( m_activeTips.get(i).getTargetElement() == widget.asWidget().getElement() )
			{
				showTip(m_activeTips.get(i), null);
			}
		}
	}
	
	public void addTip(IsWidget widget, ToolTipConfig config)
	{
		if( config.getType() == E_ToolTipType.MOUSE_OVER)
		{
			if( !m_isMouseOverEnabled)  return;
		}
		
		m_subManagers[config.getType().ordinal()].addTip(widget, config);
		
		for( int i = 0; i < m_activeTips.size(); i++ )
		{
			ToolTip ithTip = m_activeTips.get(i);
			
			if( ithTip.getTargetElement() == widget.asWidget().getElement() )
			{
				if( ithTip.getConfig().getType() == config.getType() )
				{
					this.prepareTip(ithTip, ithTip.getTargetElement(), config);
					
					Point mousePoint = null;
					
					if( config.getType() == E_ToolTipType.MOUSE_OVER )
					{
						mousePoint = m_mouse.getMousePoint();
					}

					this.showTip(ithTip, mousePoint);
				}
			}
		}
	}
	
	public void removeTip(IsWidget widget)
	{
		for( int i = 0; i < m_subManagers.length; i++ )
		{
			m_subManagers[i].removeTip(widget);
		}
	}
	
	public void setDefaultPadding(Double value)
	{
		m_defaultPadding = value;
	}
	
	public void setDefaultZIndex(E_ToolTipType type, int zIndex)
	{
		m_defaultZIndeces[type.ordinal()] = zIndex;
	}
}
