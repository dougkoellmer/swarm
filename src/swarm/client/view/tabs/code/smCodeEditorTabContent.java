package swarm.client.view.tabs.code;

import java.util.logging.Logger;

import swarm.client.app.smS_Client;
import swarm.client.app.smAppContext;
import swarm.client.entities.smBufferCell;
import swarm.client.entities.smA_ClientUser;
import swarm.client.input.smClickManager;
import swarm.client.input.smI_ClickHandler;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.StateMachine_Tabs;
import swarm.client.states.camera.State_CameraSnapping;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.states.code.Action_EditingCode_Edit;
import swarm.client.states.code.Action_EditingCode_Preview;
import swarm.client.states.code.Action_EditingCode_Save;
import swarm.client.states.code.State_EditingCode;
import swarm.client.states.code.State_EditingCodeBlocker;
import swarm.client.view.smConsoleBlocker;
import swarm.client.view.smI_UIElement;
import swarm.client.view.smS_UI;
import swarm.client.view.smSplitPanel;
import swarm.client.view.smViewContext;
import swarm.client.view.cell.smVisualCellManager;
import swarm.client.view.tabs.smI_TabContent;
import swarm.client.view.tabs.smTabPanel;
import swarm.client.view.tooltip.smE_ToolTipType;
import swarm.client.view.tooltip.smToolTipConfig;
import swarm.client.view.tooltip.smToolTipManager;
import swarm.client.view.widget.smButton;
import swarm.client.view.widget.smDefaultButton;
import swarm.shared.app.smS_App;
import swarm.shared.entities.smE_CharacterQuota;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smStateEvent;
import swarm.shared.structs.smCodePrivileges;
import swarm.shared.structs.smGridCoordinate;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class smCodeEditorTabContent extends AbsolutePanel implements smI_TabContent, smI_CodeMirrorListener
{
	private static final Logger s_logger = Logger.getLogger(smCodeEditorTabContent.class.getName());
	
	private final smDefaultButton m_previewButton = new smDefaultButton();
	private final smDefaultButton m_saveButton = new smDefaultButton();
	
	private final Label m_characterCountLabel = new Label();
	
	private final FlowPanel m_buttonTrayWrapper = new FlowPanel();
	private final HorizontalPanel m_buttonTray = new HorizontalPanel();
	
	private smCodeEditor m_editor = null;
	
	private final Action_EditingCode_Edit.Args m_args_CodeChanged = new Action_EditingCode_Edit.Args();
	private smViewContext m_viewContext;
	
	public smCodeEditorTabContent(smViewContext viewContext)
	{
		m_viewContext = viewContext;
		
		m_editor = new smCodeEditor(m_viewContext.appContext.userMngr, this);
		
		this.addStyleName("html_editor_tab");
		
		m_buttonTrayWrapper.addStyleName("sm_button_tray_wrapper");
		m_buttonTray.addStyleName("sm_button_tray");
		m_buttonTray.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		smI_ClickHandler commitHandler = new smI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				onSaveClicked();
			}
		};
		
		smI_ClickHandler previewHandler = new smI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				onPreviewClicked();
			}
		};
		
		smToolTipManager tipManager = m_viewContext.toolTipMngr;
		smClickManager clickMngr = new smClickManager();
		
		clickMngr.addClickHandler(m_saveButton, commitHandler);
		m_saveButton.setText("Save");
		m_saveButton.setEnabled(false);
		tipManager.addTip(m_saveButton, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Save your cell and push it live. [Ctrl-S]"));
		
		clickMngr.addClickHandler(m_previewButton, previewHandler);
		m_previewButton.setText("Preview");
		m_previewButton.setEnabled(false);
		tipManager.addTip(m_previewButton, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Preview your cell without saving. [Ctrl-P]"));
		
		m_characterCountLabel.getElement().getStyle().setMarginLeft(smS_UI.MAGIC_UI_SPACING*2, Unit.PX);
		m_characterCountLabel.setWordWrap(false);
		
		m_buttonTray.add(m_characterCountLabel);
		//m_buttonTray.add(m_revertButton);
		m_buttonTray.add(m_previewButton);
		m_buttonTray.add(m_saveButton);
		
		m_buttonTray.setCellWidth(m_characterCountLabel, "100%");
		m_buttonTray.setCellHorizontalAlignment(m_characterCountLabel, HasHorizontalAlignment.ALIGN_LEFT);
		
		m_characterCountLabel.setText("");
		
		m_buttonTrayWrapper.add(m_buttonTray);
		this.add(m_editor);
		this.add(m_buttonTrayWrapper);
	}
	
	private void onSaveClicked()
	{
		if( !m_viewContext.stateContext.isActionPerformable(Action_EditingCode_Save.class) )
		{
			return;
		}
		
		m_viewContext.cellMngr.clearAlerts();
		
		onChange(); // one last time to make sure all changes are provided to the machine.
		
		m_viewContext.stateContext.performAction(Action_EditingCode_Save.class);
	}
	
	private void onPreviewClicked()
	{
		if( !m_viewContext.stateContext.isActionPerformable(Action_EditingCode_Preview.class) )
		{
			return;
		}
		
		m_viewContext.cellMngr.clearAlerts();
		
		onChange(); // one last time to make sure all changes are provided to the machine.
		
		m_viewContext.stateContext.performAction(Action_EditingCode_Preview.class);
	}
	
	private void updateLayout()
	{
		double tabButtonContainerHeight = m_viewContext.splitPanel.getTabPanel().getTabButtonContainerHeight();
		double editorHeight = RootPanel.get().getOffsetHeight() - smTabPanel.TAB_HEIGHT - smS_UI.MAGIC_UI_SPACING*2 - tabButtonContainerHeight;
		m_editor.setSize(m_viewContext.splitPanel.getTabPanelWidth() + "px", editorHeight + "px");
		
		m_editor.updateLayout();
	}
	
	public void onResize()
	{
		updateLayout();
	}
	
	@Override
	public void onLoad()
	{
		super.onLoad();
		
		updateLayout();
	}
	
	private void refreshButtons()
	{
		boolean canCommit = m_viewContext.stateContext.isActionPerformable(Action_EditingCode_Save.class);
		boolean canPreview = m_viewContext.stateContext.isActionPerformable(Action_EditingCode_Preview.class);
		
		m_previewButton.setEnabled(canPreview);
		m_saveButton.setEnabled(canCommit);
	}
	
	@Override
	public void onStateEvent(smStateEvent event)
	{
		m_editor.onStateEvent(event);
		
		switch(event.getType())
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof State_ViewingCell )
				{
					State_EditingCode editingHtmlState = event.getContext().getEnteredState(State_EditingCode.class);
					
					if( editingHtmlState != null )
					{
						String html = editingHtmlState.getCode().getRawCode();
						setEditorContent(html);
					}
				}
				
				break;
			}
			
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof State_ViewingCell )
				{
					refreshButtons();
				}
				else if( event.getState() instanceof State_EditingCode )
				{
					refreshButtons();
					
					if( !event.getContext().isEntered(State_ViewingCell.class) )
					{
						setEditorContent(""); // some bug in code mirror forces you to set content initially for gutter to display correctly.
					}
					else
					{
						if( event.getRevealingState() == null || event.getRevealingState() == State_EditingCodeBlocker.class )
						{
							State_EditingCode editingHtmlState = (State_EditingCode) event.getState();
							
							//--- TODO(DKR) Seemingly not causing problems for now, but these "reasons" don't really apply anymore,
							//---			So this conditional should be cleaned up.
							if( editingHtmlState.getMostRecentBlockerReason() != State_EditingCodeBlocker.Reason.SYNCING &&
								editingHtmlState.getMostRecentBlockerReason() != State_EditingCodeBlocker.Reason.PREVIEWING )
							{
								this.setEditorContent(editingHtmlState.getCode().getRawCode());
							}
						}
					}
				}
				else if( event.getState() instanceof State_EditingCodeBlocker )
				{
					smConsoleBlocker.getInstance().attachTo(this);
					
					updateConsoleBlocker();
				}
				else if( event.getState() instanceof StateMachine_Tabs )
				{
					if( event.getRevealingState() == null )
					{
						//--- DRK > This goes along with the sleight of hand we pull below for not detaching the blocker while animating out.
						//---		This just makes sure that the console blocker gets detached...it might be the case that it gets immediately
						//---		reattached.
						smConsoleBlocker.getInstance().detachFrom(this);
					}
				}
				
				break;
			}
			
			case DID_BACKGROUND:
			{
				if( event.getState() instanceof StateMachine_Tabs )
				{
					refreshButtons();
				}
				
				break;
			}
			
			case DID_EXIT:
			{
				if( event.getState() instanceof State_ViewingCell || event.getState() instanceof State_CameraSnapping )
				{
					refreshButtons();
				}
				else if( event.getState() instanceof State_EditingCodeBlocker )
				{
					//--- DRK > Pulling a little sleight of hand here so that we don't remove the blocker if it appears
					//---		that the console is being animated out.
					if( event.getContext().isForegrounded(StateMachine_Tabs.class) )
					{
						//--- DRK > This should be called in the "exit" event because background event could be called for something
						//---		like an error dialog being pushed over the topmost state in the machine.
						//---		Other tabs needing the console blocker will simply take over if they need it anyway.
						smConsoleBlocker.getInstance().detachFrom(this);
					}
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == Action_EditingCode_Save.class || event.getAction() == Action_EditingCode_Preview.class )
				{
					refreshButtons();
				}
				else if( event.getAction() == Action_EditingCode_Edit.class )
				{
					refreshButtons();
				}
				else if( event.getAction() == State_EditingCodeBlocker.OnReasonChange.class )
				{
					updateConsoleBlocker();
				}
				else if( event.getAction() == StateMachine_Base.OnUserPopulated.class || 
						 event.getAction() == StateMachine_Base.OnUserCleared.class  )
				{
					State_ViewingCell viewingState = event.getContext().getEnteredState(State_ViewingCell.class);
					State_EditingCode editingState = event.getContext().getEnteredState(State_EditingCode.class);
					
					if( viewingState != null && editingState != null )
					{
						setEditorContent(editingState.getCode().getRawCode());
					}
				}
				
				break;
			}
		}
	}
	
	private void updateConsoleBlocker()
	{
		State_EditingCodeBlocker blocker = m_viewContext.stateContext.getForegroundedState(State_EditingCodeBlocker.class);
		
		if( blocker == null )
		{
			return;
		}
		
		State_EditingCodeBlocker.Reason reason = blocker.getReason();
		
		String statusText = null;
		
		switch(reason)
		{
			case NO_CELL_SELECTED:	statusText = "No cell selected.";				break;
			case LOADING:			statusText = "Waiting on server...";			break;
			case NO_HTML:			statusText = "No html available.";				break;
			case SNAPPING:			statusText = "Snapping...";						break;
			case ERROR:				statusText = "Error retrieving html.";			break;
			case SYNCING:			statusText = "Saving...";						break;
			case PREVIEWING:		statusText = "Generating preview...";			break;
		}
		
		smConsoleBlocker.getInstance().setHtml(statusText);

		if( reason != State_EditingCodeBlocker.Reason.SYNCING && reason != State_EditingCodeBlocker.Reason.PREVIEWING)
		{
			//--- DRK > For every reason except compiling, we don't want to see any code behind the blocker.
			setEditorContent("");
		}
	}
	
	private void setEditorContent(String content)
	{
		m_editor.setContent(content);
		
		refreshCharacterCount();
	}
	
	private void refreshCharacterCount()
	{
		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
		
		if( viewingState != null && !m_viewContext.stateContext.isForegrounded(State_EditingCodeBlocker.class) )
		{
			smCodePrivileges privileges = viewingState.getCell().getCodePrivileges();
			
			int charCount = m_editor.getContent().length();
			
			int max = privileges.getCharacterQuota().getMaxCharacters();
			
			m_characterCountLabel.setText(charCount + " / " + max + " characters used.");
			
			if( charCount > max )
			{
				m_characterCountLabel.getElement().getStyle().setColor("red");
			}
			else
			{
				m_characterCountLabel.getElement().getStyle().clearColor();
			}
		}
		else
		{
			m_characterCountLabel.setText("");
		}
	}

	@Override
	public void onChange()
	{
		String sourceCode = m_editor.getContent();
		
		refreshCharacterCount();
		
		m_args_CodeChanged.init(sourceCode);
		m_viewContext.stateContext.performAction(Action_EditingCode_Edit.class, m_args_CodeChanged);
	}

	@Override
	public void onSelect()
	{
		refreshButtons();
	}

	@Override
	public void onSave()
	{
		onSaveClicked();
	}

	@Override
	public void onPreview()
	{
		onPreviewClicked();
	}
}
