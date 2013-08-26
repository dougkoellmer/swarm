package swarm.client.ui.widget;

import swarm.client.app.sm_c;
import swarm.client.input.smClickManager;
import swarm.client.input.smI_ClickHandler;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class smCheckBox extends FlowPanel
{
	private final HorizontalPanel m_shelf = new HorizontalPanel();
	private final CheckBox m_checkBox = new CheckBox();
	private final Label m_label = new Label();
	private final FlowPanel m_clickCatcher = new FlowPanel();
	
	public smCheckBox()
	{
		this(null);
	}
	
	public smCheckBox(String text)
	{
		this.getElement().getStyle().setCursor(Cursor.POINTER);
		this.getElement().getStyle().setPosition(Position.RELATIVE);
		
		m_clickCatcher.getElement().getStyle().setPosition(Position.ABSOLUTE);
		m_clickCatcher.getElement().getStyle().setZIndex(1);
		m_clickCatcher.getElement().getStyle().setTop(0, Unit.PX);
		m_clickCatcher.getElement().getStyle().setLeft(0, Unit.PX);
		
		//m_label.getElement().getStyle().setColor("#0000ff");
		m_label.setWordWrap(false);
		
		if( text != null )
		{
			this.setText(text);
		}
		
		sm_c.clickMngr.addClickHandler(m_clickCatcher, new smI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				m_checkBox.setValue(!m_checkBox.getValue());
			}
			
		});
		
		m_shelf.add(m_checkBox);
		m_shelf.add(m_label);
		
		m_shelf.setCellVerticalAlignment(m_checkBox, HasVerticalAlignment.ALIGN_MIDDLE);
		m_shelf.setCellVerticalAlignment(m_label, HasVerticalAlignment.ALIGN_MIDDLE);
		
		this.add(m_shelf);
		this.add(m_clickCatcher);		
	}
	
	public void setSize(String width, String height)
	{
		super.setSize(width, height);
		
		m_clickCatcher.setSize(width, height);
	}
	
	public boolean isChecked()
	{
		return m_checkBox.getValue();
	}
	
	public void setText(String text)
	{
		m_label.setText(text);
	}
	
	public Widget getClickCatcher()
	{
		return m_clickCatcher;
	}
	
	public void check()
	{
		m_checkBox.setValue(true);
	}
}
