package b33hive.client.ui.tabs.code;

import java.util.logging.Logger;

import b33hive.client.app.bhS_Client;
import b33hive.client.app.bh_c;
import b33hive.client.entities.bhBufferCell;
import b33hive.client.entities.bhA_ClientUser;
import b33hive.client.input.bhClickManager;
import b33hive.client.input.bhI_ClickHandler;
import b33hive.client.states.StateMachine_Base;
import b33hive.client.states.StateMachine_Tabs;
import b33hive.client.states.camera.State_CameraSnapping;
import b33hive.client.states.camera.State_ViewingCell;
import b33hive.client.states.code.State_EditingCode;
import b33hive.client.states.code.State_EditingCodeBlocker;
import b33hive.client.ui.bhConsoleBlocker;
import b33hive.client.ui.bhI_UIElement;
import b33hive.client.ui.bhS_UI;
import b33hive.client.ui.bhSplitPanel;
import b33hive.client.ui.bh_view;
import b33hive.client.ui.cell.bhVisualCellManager;
import b33hive.client.ui.tabs.bhI_TabContent;
import b33hive.client.ui.tabs.bhTabPanel;
import b33hive.client.ui.tooltip.bhE_ToolTipType;
import b33hive.client.ui.tooltip.bhToolTipConfig;
import b33hive.client.ui.tooltip.bhToolTipManager;
import b33hive.client.ui.widget.bhButton;
import b33hive.client.ui.widget.bhDefaultButton;
import b33hive.shared.app.bhS_App;
import b33hive.shared.entities.bhE_CharacterQuota;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.statemachine.bhStateEvent;
import b33hive.shared.structs.bhCodePrivileges;
import b33hive.shared.structs.bhGridCoordinate;
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

public class bhCodeEditorTab extends AbsolutePanel implements bhI_TabContent, bhI_CodeMirrorListener
{
	private static final Logger s_logger = Logger.getLogger(bhCodeEditorTab.class.getName());
	
	private final bhDefaultButton m_previewButton = new bhDefaultButton();
	private final bhDefaultButton m_saveButton = new bhDefaultButton();
	
	private final Label m_characterCountLabel = new Label();
	
	private final FlowPanel m_buttonTrayWrapper = new FlowPanel();
	private final HorizontalPanel m_buttonTray = new HorizontalPanel();
	
	private bhCodeEditor m_editor = null;
	
	private final State_EditingCode.CodeChanged.Args m_args_CodeChanged = new State_EditingCode.CodeChanged.Args();
	
	public bhCodeEditorTab()
	{
		m_editor = new bhCodeEditor(this);
		
		this.addStyleName("html_editor_tab");
		
		m_buttonTrayWrapper.addStyleName("bh_button_tray_wrapper");
		m_buttonTray.addStyleName("bh_button_tray");
		m_buttonTray.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		bhI_ClickHandler commitHandler = new bhI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				onSaveClicked();
			}
		};
		
		bhI_ClickHandler previewHandler = new bhI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				onPreviewClicked();
			}
		};
		
		bhToolTipManager tipManager = bh_c.toolTipMngr;
		
		bh_c.clickMngr.addClickHandler(m_saveButton, commitHandler);
		m_saveButton.setText("Save");
		m_saveButton.setEnabled(false);
		tipManager.addTip(m_saveButton, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Save your cell and push it live. [Ctrl-S]"));
		
		bh_c.clickMngr.addClickHandler(m_previewButton, previewHandler);
		m_previewButton.setText("Preview");
		m_previewButton.setEnabled(false);
		tipManager.addTip(m_previewButton, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Preview your cell without saving. [Ctrl-P]"));
		
		m_characterCountLabel.getElement().getStyle().setMarginLeft(bhS_UI.MAGIC_UI_SPACING*2, Unit.PX);
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
		if( !bhA_Action.isPerformable(State_EditingCode.Save.class) )
		{
			return;
		}
		
		bh_view.cellMngr.clearAlerts();
		
		onChange(); // one last time to make sure all changes are provided to the machine.
		
		bhA_Action.perform(State_EditingCode.Save.class);
	}
	
	private void onPreviewClicked()
	{
		if( !bhA_Action.isPerformable(State_EditingCode.Preview.class) )
		{
			return;
		}
		
		bh_view.cellMngr.clearAlerts();
		
		onChange(); // one last time to make sure all changes are provided to the machine.
		
		bhA_Action.perform(State_EditingCode.Preview.class);
	}
	
	private void updateLayout()
	{
		double editorHeight = RootPanel.get().getOffsetHeight() - bhTabPanel.TAB_HEIGHT*2 - bhS_UI.MAGIC_UI_SPACING*2;
		m_editor.setSize(bh_view.splitPanel.getTabPanelWidth() + "px", editorHeight + "px");
		
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
		boolean canCommit = bhA_Action.isPerformable(State_EditingCode.Save.class);
		boolean canPreview = bhA_Action.isPerformable(State_EditingCode.Preview.class);
		
		m_previewButton.setEnabled(canPreview);
		m_saveButton.setEnabled(canCommit);
	}
	
	@Override
	public void onStateEvent(bhStateEvent event)
	{
		m_editor.onStateEvent(event);
		
		switch(event.getType())
		{
			case DID_ENTER:
			{
				if( event.getState() instanceof State_ViewingCell )
				{
					State_EditingCode editingHtmlState = bhA_State.getEnteredInstance(State_EditingCode.class);
					
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
					
					if( !bhA_State.isEntered(State_ViewingCell.class) )
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
					bhConsoleBlocker.getInstance().attachTo(this);
					
					updateConsoleBlocker();
				}
				else if( event.getState() instanceof StateMachine_Tabs )
				{
					if( event.getRevealingState() == null )
					{
						//--- DRK > This goes along with the sleight of hand we pull below for not detaching the blocker while animating out.
						//---		This just makes sure that the console blocker gets detached...it might be the case that it gets immediately
						//---		reattached.
						bhConsoleBlocker.getInstance().detachFrom(this);
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
					if( bhA_State.isForegrounded(StateMachine_Tabs.class) )
					{
						//--- DRK > This should be called in the "exit" event because background event could be called for something
						//---		like an error dialog being pushed over the topmost state in the machine.
						//---		Other tabs needing the console blocker will simply take over if they need it anyway.
						bhConsoleBlocker.getInstance().detachFrom(this);
					}
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == State_EditingCode.Save.class || event.getAction() == State_EditingCode.Preview.class )
				{
					refreshButtons();
				}
				else if( event.getAction() == State_EditingCode.CodeChanged.class )
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
					State_ViewingCell viewingState = bhA_State.getEnteredInstance(State_ViewingCell.class);
					State_EditingCode editingState = bhA_State.getEnteredInstance(State_EditingCode.class);
					
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
		State_EditingCodeBlocker blocker = bhA_State.getForegroundedInstance(State_EditingCodeBlocker.class);
		
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
		
		bhConsoleBlocker.getInstance().setHtml(statusText);

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
		State_ViewingCell viewingState = bhA_State.getEnteredInstance(State_ViewingCell.class);
		
		if( viewingState != null && !bhA_State.isForegrounded(State_EditingCodeBlocker.class) )
		{
			bhCodePrivileges privileges = viewingState.getCell().getCodePrivileges();
			
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
		
		m_args_CodeChanged.setChangedCode(sourceCode);
		bhA_Action.perform(State_EditingCode.CodeChanged.class, m_args_CodeChanged);
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
