package swarm.client.ui.dialog;

import swarm.client.app.sm_c;
import swarm.client.input.bhClickManager;
import swarm.client.input.bhI_ClickHandler;
import swarm.client.states.State_AsyncDialog;
import swarm.client.states.State_GenericDialog;
import swarm.client.ui.widget.bhButton;
import swarm.client.ui.widget.bhDefaultButton;
import swarm.shared.statemachine.bhA_Action;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class bhDialog extends AbsolutePanel
{
	public interface I_Delegate
	{
		void onOkPressed();
	}
	
	private final bhDefaultButton m_okButton = new bhDefaultButton();
	
	private final Label m_title = new Label();
	private final FlowPanel m_body = new FlowPanel();
	
	private final VerticalPanel m_header = new VerticalPanel();
	private final VerticalPanel m_outerStack = new VerticalPanel();
	private final VerticalPanel m_innerStack = new VerticalPanel();
	private final HorizontalPanel m_buttonDock = new HorizontalPanel();
	
	private final I_Delegate m_delegate;
	
	public bhDialog(int width, int height, I_Delegate delegate)
	{
		m_delegate = delegate;
		
		this.addStyleName("sm_dialog");
		//m_outerStack.addStyleName("sm_dialog_stack");
		this.getElement().getStyle().setOverflow(Overflow.VISIBLE);
		
		this.setSize(width + "px", height + "px");
		
		m_outerStack.setSize("100%", "100%");
		///m_innerStack.getElement().getStyle().setDisplay(Display.BLOCK);
		m_innerStack.setSize("100%", "100%");
		m_innerStack.addStyleName("sm_dialog_body_stack");
		m_okButton.setText("OK");
		
		m_body.addStyleName("sm_dialog_body");
		m_body.setWidth((width-8) + "px");
		
		m_header.addStyleName("sm_dialog_header");
		
		sm_c.clickMngr.addClickHandler(m_okButton, new bhI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				onOkPressed();
			}
		});

		m_header.add(m_title);
		m_header.setCellHorizontalAlignment(m_title, HasHorizontalAlignment.ALIGN_CENTER);
		m_header.setCellVerticalAlignment(m_title, HasVerticalAlignment.ALIGN_MIDDLE);
		
		m_buttonDock.add(m_okButton);
		
		m_innerStack.add(m_body);
		m_innerStack.add(m_buttonDock);
		
		//m_body.getElement().getParentElement().getStyle().setOverflowY(Overflow.AUTO);
		
		m_innerStack.setCellHeight(m_body, "100%");
		m_innerStack.setCellHorizontalAlignment(m_body, HasHorizontalAlignment.ALIGN_CENTER);
		m_innerStack.setCellVerticalAlignment(m_body, HasVerticalAlignment.ALIGN_MIDDLE);
		m_innerStack.setCellHorizontalAlignment(m_buttonDock, HasHorizontalAlignment.ALIGN_CENTER);
		
		m_outerStack.add(m_header);
		m_outerStack.add(m_innerStack);
		
		m_outerStack.setCellHeight(m_innerStack, "100%");
		
		this.add(m_outerStack);
	}
	
	@Override
	public void onLoad()
	{
		super.onLoad();
		
		scheduleLayoutUpdate(this);
	}
	
	private native void scheduleLayoutUpdate(bhDialog thisArg)
	/*-{
		setTimeout(function()
		{
			thisArg.@swarm.client.ui.dialog.bhDialog::updateLayout()();
		}, 0);
	}-*/;
	
	private void updateLayout()
	{
		String maxHeight = m_body.getElement().getParentElement().getClientHeight()+"px";
		m_body.getElement().getStyle().setProperty("maxHeight", maxHeight);
	}
	
	public void setTitle(String title)
	{
		m_title.setText(title);
	}
	
	public void setBodySafeHtml(SafeHtml safeHtml)
	{
		m_body.getElement().setInnerSafeHtml(safeHtml);
	}
	
	public void setBodyHtml(String html)
	{
		//m_body.getElement().getStyle().setProperty("maxHeight", m_body.getParent().getElement().getClientHeight() + "px");
		m_body.getElement().setInnerHTML(html);
	}
	
	private void onOkPressed()
	{
		m_delegate.onOkPressed();
	}
	
	void onKeyPressed(int keyCode)
	{
		switch( keyCode )
		{
			case KeyCodes.KEY_ENTER:
			{
				onOkPressed();
				
				break;
			}
		}
	}
}
