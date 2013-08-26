package swarm.client.ui.tooltip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import swarm.client.input.smSimpleMouse;
import swarm.client.ui.smE_ZIndex;
import swarm.client.ui.smS_UI;
import swarm.client.ui.alignment.smAlignmentDefinition;
import swarm.client.ui.alignment.smE_AlignmentPosition;
import swarm.client.ui.alignment.smE_AlignmentType;
import swarm.client.ui.alignment.smU_Alignment;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.structs.smPoint;
import com.gargoylesoftware.htmlunit.javascript.host.Event;
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

public class smToolTipManager
{
	private static final Logger s_logger = Logger.getLogger(smToolTipManager.class.getName());
	
	private final smI_ToolTipSubManager[] m_subManagers;
	
	private final ArrayList<smToolTip> m_activeTips = new ArrayList<smToolTip>();
	
	private final smSimpleMouse m_mouse = new smSimpleMouse(RootPanel.get());
	
	private final Integer[] m_defaultZIndeces = new Integer[smE_ToolTipType.values().length];
	
	private Double m_defaultPadding = null;
	
	private final smAlignmentDefinition m_reusedAlignment = new smAlignmentDefinition();
	
	private final boolean m_isMouseOverEnabled;
	
	public smToolTipManager(boolean enabled, int mouseOverDelayMilliseconds)
	{
		m_isMouseOverEnabled = enabled;
		
		m_subManagers = new smI_ToolTipSubManager[smE_ToolTipType.values().length];
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
		
		m_subManagers[smE_ToolTipType.FOCUS.ordinal()]			= new smToolTipSubManager_Focus(this);
		m_subManagers[smE_ToolTipType.MOUSE_OVER.ordinal()]		= new smToolTipSubManager_MouseOver(this, mouseOverDelayMilliseconds);
		m_subManagers[smE_ToolTipType.NOTIFICATION.ordinal()]	= new smToolTipSubManager_Notification(this);
		m_subManagers[smE_ToolTipType.TUTORIAL.ordinal()]		= new smToolTipSubManager_Tutorial(this);
		m_subManagers[smE_ToolTipType.STATUS.ordinal()]			= new smToolTipSubManager_Status(this);
	}
	
	private void setAlignmentDefaults(smToolTip toolTip, smAlignmentDefinition alignment, smPoint mousePoint)
	{
		//--- DRK > Set up defaults for alignment.
		if( mousePoint != null )
		{
			smE_AlignmentPosition position = alignment.getPosition(smE_AlignmentType.MASTER_ANCHOR_HORIZONTAL);
			
			if( position == null || position == smE_AlignmentPosition.DEFINED )
			{
				int toolTipWidth = toolTip.getElement().getClientWidth();
				int toolTipHeight = toolTip.getElement().getClientHeight();
				int screenHeight = RootPanel.get().getOffsetHeight();
				int screenWidth = RootPanel.get().getOffsetWidth();
				
				int positionY = (int) (mousePoint.getY() + smS_UI.CURSOR_HEIGHT + m_defaultPadding);
				
				if( positionY + toolTipHeight + m_defaultPadding > screenHeight )
				{
					m_reusedAlignment.setPosition(smE_AlignmentType.MASTER_ANCHOR_VERTICAL, smE_AlignmentPosition.LEFT_OR_TOP);
					m_reusedAlignment.setPosition(smE_AlignmentType.SLAVE_ANCHOR_VERTICAL, smE_AlignmentPosition.RIGHT_OR_BOTTOM);
				}
				else
				{
					m_reusedAlignment.setDefinedPosition(smE_AlignmentType.MASTER_ANCHOR_VERTICAL, (double) positionY);
					m_reusedAlignment.setPosition(smE_AlignmentType.SLAVE_ANCHOR_VERTICAL, smE_AlignmentPosition.LEFT_OR_TOP);
				}
				
				if( mousePoint.getX() + toolTipWidth + m_defaultPadding > screenWidth )
				{
					m_reusedAlignment.setDefinedPosition(smE_AlignmentType.MASTER_ANCHOR_HORIZONTAL, (double)screenWidth - m_defaultPadding);
					m_reusedAlignment.setPosition(smE_AlignmentType.SLAVE_ANCHOR_HORIZONTAL, smE_AlignmentPosition.RIGHT_OR_BOTTOM);
				}
				else
				{
					alignment.setDefinedPosition(smE_AlignmentType.MASTER_ANCHOR_HORIZONTAL, mousePoint.getX());
				}
			}
		}
		
		if( alignment.getPosition(smE_AlignmentType.MASTER_ANCHOR_HORIZONTAL) == null )
		{
			alignment.setPosition(smE_AlignmentType.MASTER_ANCHOR_HORIZONTAL, smE_AlignmentPosition.LEFT_OR_TOP);
		}
		if( alignment.getPosition(smE_AlignmentType.MASTER_ANCHOR_VERTICAL) == null )
		{
			alignment.setPosition(smE_AlignmentType.MASTER_ANCHOR_VERTICAL, smE_AlignmentPosition.RIGHT_OR_BOTTOM);
		}
		
		
		if( alignment.getPosition(smE_AlignmentType.SLAVE_ANCHOR_HORIZONTAL) == null )
		{
			alignment.setPosition(smE_AlignmentType.SLAVE_ANCHOR_HORIZONTAL, smE_AlignmentPosition.LEFT_OR_TOP);
		}
		if( alignment.getPosition(smE_AlignmentType.SLAVE_ANCHOR_VERTICAL) == null )
		{
			alignment.setPosition(smE_AlignmentType.SLAVE_ANCHOR_VERTICAL, smE_AlignmentPosition.LEFT_OR_TOP);
			alignment.setPadding(smE_AlignmentType.SLAVE_ANCHOR_VERTICAL, m_defaultPadding);
		}
	}
	
	void showTip(smToolTip toolTip, smPoint mousePoint)
	{
		Element targetElement = toolTip.getTargetElement();
		bhToolTipConfig config = toolTip.getConfig();
		bhAlignmentDefinition alignment = config.getAlignmentDefinition();
		
		if( alignment != null )
		{
			m_reusedAlignment.copy(alignment);
		}
		else
		{
			m_reusedAlignment.clear();
		}
	
		setAlignmentDefaults(toolTip, m_reusedAlignment, mousePoint);
		
		bhU_Alignment.performAlignment(targetElement, toolTip.getElement(), m_reusedAlignment);
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
	
	void prepareTip(smToolTip toolTip, Element targetElement, smToolTipConfig config)
	{
		toolTip.getElement().getStyle().setVisibility(Visibility.HIDDEN);
		
		smE_ZIndex.TOOL_TIP_1.assignTo(toolTip, config.getType());
		
		if( toolTip.getParent() == null )
		{
			RootPanel.get().add(toolTip);
			
			bhU_Debug.ASSERT(m_activeTips.indexOf(toolTip) == -1, "prepareTip1");
			
			m_activeTips.add(toolTip);
		}
		
		toolTip.init(targetElement, config);
	}
	
	void endTip(smToolTip toolTip)
	{
		if( toolTip.getParent() != null )
		{
			toolTip.removeFromParent();
			
			toolTip.deinit();
			
			int index = m_activeTips.indexOf(toolTip);
			
			bhU_Debug.ASSERT(index != -1, "endTip1");
			
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
	
	public void addTip(IsWidget widget, smToolTipConfig config)
	{
		if( config.getType() == smE_ToolTipType.MOUSE_OVER)
		{
			if( !m_isMouseOverEnabled)  return;
		}
		
		m_subManagers[config.getType().ordinal()].addTip(widget, config);
		
		for( int i = 0; i < m_activeTips.size(); i++ )
		{
			bhToolTip ithTip = m_activeTips.get(i);
			
			if( ithTip.getTargetElement() == widget.asWidget().getElement() )
			{
				if( ithTip.getConfig().getType() == config.getType() )
				{
					this.prepareTip(ithTip, ithTip.getTargetElement(), config);
					
					bhPoint mousePoint = null;
					
					if( config.getType() == smE_ToolTipType.MOUSE_OVER )
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
	
	public void setDefaultZIndex(smE_ToolTipType type, int zIndex)
	{
		m_defaultZIndeces[type.ordinal()] = zIndex;
	}
}
