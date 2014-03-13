package swarm.client.view.tabs.account;

import java.util.logging.Logger;

import swarm.client.managers.ClientAccountManager;
import swarm.client.app.AppContext;
import swarm.client.input.ClickManager;
import swarm.client.input.I_ClickHandler;
import swarm.client.js.U_Native;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.StateMachine_Tabs;
import swarm.client.states.account.Action_SignInOrUp_SignUp;
import swarm.client.states.account.State_AccountStatusPending;
import swarm.client.states.account.State_SignInOrUp;
import swarm.client.view.InitialSyncScreen;
import swarm.client.view.S_UI;
import swarm.client.view.ViewContext;
import swarm.client.view.alignment.AlignmentDefinition;
import swarm.client.view.alignment.E_AlignmentPosition;
import swarm.client.view.alignment.E_AlignmentType;
import swarm.client.view.alignment.U_Alignment;
import swarm.client.view.tooltip.E_ToolTipType;
import swarm.client.view.tooltip.ToolTipConfig;
import swarm.client.view.tooltip.ToolTipManager;
import swarm.client.view.widget.HtmlWrapper;
import swarm.client.view.widget.I_TextBox;
import swarm.client.view.widget.I_TextBoxChangeListener;
import swarm.client.view.widget.PasswordTextField;
import swarm.client.view.widget.TextField;
import swarm.client.view.widget.TextBoxWrapper;
import swarm.shared.app.S_CommonApp;
import swarm.shared.account.E_SignInCredentialType;
import swarm.shared.account.E_SignInValidationError;
import swarm.shared.account.E_SignUpValidationError;
import swarm.shared.account.E_SignUpCredentialType;
import swarm.shared.account.I_SignUpCredentialValidator;
import swarm.shared.account.I_ValidationError;
import swarm.shared.account.S_Account;
import swarm.shared.account.SignUpCredentials;
import swarm.shared.account.SignUpValidationResult;
import swarm.shared.code.U_Code;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.I_StateEventListener;
import swarm.shared.statemachine.StateEvent;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SignUpPanel extends VerticalPanel implements I_StateEventListener
{
	private static final Logger s_logger = Logger.getLogger(SignUpPanel.class.getName());
	
	private final VerticalPanel m_stack = new VerticalPanel();
	
	private final FlowPanel m_panel = new FlowPanel();
	
	private final SignInOrUpErrorField m_emailErrorField = new SignInOrUpErrorField();
	private final SignInOrUpErrorField m_usernameErrorField = new SignInOrUpErrorField();
	private final SignInOrUpErrorField m_passwordErrorField = new SignInOrUpErrorField();
	private final SignInOrUpErrorField m_captchaErrorField = new SignInOrUpErrorField();
	
	private final HtmlWrapper m_captchaImageContainer;
	
	private final StaySignedInCheckbox m_checkbox;
	
	private final TextBoxWrapper m_emailInput = new TextBoxWrapper(new TextField("Email"));
	private final TextBoxWrapper m_usernameInput = new TextBoxWrapper(new TextField("Username"));
	private final TextBoxWrapper m_passwordInput = new TextBoxWrapper(new PasswordTextField("Password"));
	private final TextBoxWrapper m_captchaInput;
	
	private final FlowPanel m_captchaControlContainer = new FlowPanel();
	
	private final Anchor m_reloadCaptcha = new Anchor();
	private final Anchor m_captchaHelp = new Anchor();
	
	private final SignInOrUpButton m_button = new SignInOrUpButton();
	
	private final SignInOrUpErrorField 	m_errorFields[] = {m_emailErrorField, m_usernameErrorField, m_passwordErrorField, m_captchaErrorField};
	private final TextBoxWrapper			m_inputs[] = {m_emailInput, m_usernameInput, m_passwordInput, null};
	
	private int m_lastFocusedFieldIndex = -1;
	
	private final Action_SignInOrUp_SignUp.Args m_args_SignUp = new Action_SignInOrUp_SignUp.Args();
	
	private final ViewContext m_viewContext;
	
	SignUpPanel(ViewContext viewContext)
	{
		m_viewContext = viewContext;
		
		m_checkbox = new StaySignedInCheckbox(m_viewContext);
		
		this.addStyleName("sm_signuporin_sub_panel_wrapper");
		m_panel.addStyleName("sm_signuporin_sub_panel");
		m_stack.setWidth("100%");
		
		m_viewContext.toolTipMngr.addTip(m_button, new ToolTipConfig(E_ToolTipType.MOUSE_OVER, "Do it now!"));
		
		Element recaptchaResponseFieldElement = Document.get().getElementById("recaptcha_response_field");
		recaptchaResponseFieldElement.removeFromParent();
		recaptchaResponseFieldElement.getStyle().clearDisplay();
		
		m_captchaInput = new TextBoxWrapper(new TextField(recaptchaResponseFieldElement, "Enter The Text Below"));
		m_captchaInput.getTextBox().addStyleName("sm_recaptcha_field");
		m_inputs[3] = m_captchaInput;
		
		final Element recaptchaImageElement = Document.get().getElementById("recaptcha_image");
		recaptchaImageElement.removeFromParent();
		recaptchaImageElement.getStyle().clearDisplay();
		
		recaptchaImageElement.getStyle().clearWidth();
		recaptchaImageElement.getStyle().clearHeight();
		m_captchaImageContainer = new HtmlWrapper(recaptchaImageElement);
		m_captchaImageContainer.addStyleName("sm_captcha_container");
		m_captchaImageContainer.addStyleName("sm_signinorup_element");
		
		
		m_reloadCaptcha.getElement().getStyle().setMarginLeft(3, Unit.PX);
		m_captchaHelp.getElement().getStyle().setMarginLeft(12, Unit.PX);
		
		m_captchaHelp.setHref("javascript:Recaptcha.showhelp()");
		m_reloadCaptcha.setHref("javascript:Recaptcha.reload('t')");
		
		m_viewContext.clickMngr.addClickHandler(m_reloadCaptcha, new I_ClickHandler()
		{
			@Override
			public void onClick(int x, int y)
			{
				onRecaptchaClicked();
			}
		});
		
		m_viewContext.clickMngr.addClickHandler(m_captchaImageContainer, new I_ClickHandler()
		{
			@Override
			public void onClick(int x, int y)
			{
				m_viewContext.recaptchaWrapper.loadNewImage();
				onRecaptchaClicked();
			}
		});
		
		m_reloadCaptcha.addStyleName("sm_js_anchor");
		m_captchaHelp.addStyleName("sm_js_anchor");
		
		m_captchaControlContainer.add(m_reloadCaptcha);
		m_captchaControlContainer.add(m_captchaHelp);
		
		m_reloadCaptcha.setText("reload");
		m_captchaHelp.setText("help");
		
		m_button.addStyleName("sm_signup_button");
		
		AlignmentDefinition alignment = U_Alignment.createHorRightVerCenter(S_UI.TOOl_TIP_PADDING);
		alignment.setPadding(E_AlignmentType.MASTER_ANCHOR_VERTICAL, -1.0);
		
		ToolTipManager toolTipper = m_viewContext.toolTipMngr;
		ToolTipConfig config = null;
		
		config = new ToolTipConfig(E_ToolTipType.FOCUS, alignment, "Uniquely identifies you.  Invisible to others.");
		toolTipper.addTip(m_emailInput.getTextBox(), config);
		
		config = new ToolTipConfig(E_ToolTipType.FOCUS, alignment, "Your public username, as in b33hive.net/my_username.  Letters, numbers and underscores only, 1-"+S_Account.MAX_USERNAME_LENGTH+" characters.");
		toolTipper.addTip(m_usernameInput.getTextBox(), config);
		
		config = new ToolTipConfig(E_ToolTipType.FOCUS, alignment, "[insert advice on choosing password].  Must be "+S_Account.MIN_PASSWORD_LENGTH+" or more characters.");
		toolTipper.addTip(m_passwordInput.getTextBox(), config);
		
		config = new ToolTipConfig(E_ToolTipType.FOCUS, alignment, "This reasonably ensures that you're a biological entity.");
		toolTipper.addTip(m_captchaInput.getTextBox(), config);
		
		config = new ToolTipConfig(E_ToolTipType.MOUSE_OVER, "What is this thing?");
		toolTipper.addTip(m_captchaHelp, config);
		
		config = new ToolTipConfig(E_ToolTipType.MOUSE_OVER, "Click here to get new text.");
		toolTipper.addTip(m_reloadCaptcha, config);
		toolTipper.addTip(m_captchaImageContainer, config);
		
		m_usernameInput.getTextBox().setMaxLength(S_Account.MAX_USERNAME_LENGTH);
		m_emailInput.getTextBox().setMaxLength(S_Account.MAX_EMAIL_LENGTH);
		
		m_button.setText("Sign Up");
		
		m_stack.add(m_emailErrorField);
		m_stack.add(m_emailInput);
		m_stack.add(m_usernameErrorField);
		m_stack.add(m_usernameInput);
		m_stack.add(m_passwordErrorField);
		m_stack.add(m_passwordInput);
		m_stack.add(m_captchaErrorField);
		m_stack.add(m_captchaInput);
		m_stack.add(m_captchaImageContainer);
		m_stack.add(m_captchaControlContainer);
		
		m_stack.setCellHorizontalAlignment(m_emailErrorField, HasHorizontalAlignment.ALIGN_RIGHT);
		m_stack.setCellHorizontalAlignment(m_usernameErrorField, HasHorizontalAlignment.ALIGN_RIGHT);
		m_stack.setCellHorizontalAlignment(m_passwordErrorField, HasHorizontalAlignment.ALIGN_RIGHT);
		m_stack.setCellHorizontalAlignment(m_captchaErrorField, HasHorizontalAlignment.ALIGN_RIGHT);
		
		HorizontalPanel bottomDock = new HorizontalPanel();
		FlowPanel wrapper = new FlowPanel();
		wrapper.getElement().getStyle().setMarginLeft(0, Unit.PX);
		wrapper.getElement().getStyle().setMarginBottom(6, Unit.PX);
		bottomDock.setSize("100%", "100%");
		wrapper.add(m_checkbox);
		bottomDock.add(wrapper);
		bottomDock.add(m_button);
		bottomDock.setCellVerticalAlignment(wrapper, HasVerticalAlignment.ALIGN_BOTTOM);
		bottomDock.setCellHorizontalAlignment(m_button, HasHorizontalAlignment.ALIGN_RIGHT);
		bottomDock.setCellHorizontalAlignment(wrapper, HasHorizontalAlignment.ALIGN_LEFT);
		
		m_stack.add(bottomDock);
		
		/*this.sinkEvents(Event.KEYEVENTS);
		
		this.addDomHandler(new KeyDownHandler()
		{
			@Override
			public void onKeyDown(KeyDownEvent event)
			{
				s_logger.severe("ERERERER");
			}
		}, KeyDownEvent.getType());*/
		
		/*final Label legal = new Label();
		String termsHref = smU_Code.transformPathToJavascript("b33hive/terms");
		String privacyHref = smU_Code.transformPathToJavascript("b33hive/privacy");
		legal.getElement().setInnerHTML("By signing up you agree to our<br><a class='sm_terms_anchor' href=\""+termsHref+"\">Terms</a> and <a class='sm_terms_anchor' href=\""+privacyHref+"\">Privacy Policy</a>.");
		m_stack.add(legal);
		m_stack.setCellHorizontalAlignment(legal, HasHorizontalAlignment.ALIGN_RIGHT);*/
		
		//--- DRK > Register callbacks so we can display error messages when text in the input fields change.
		for( int i = 0; i < m_errorFields.length; i++ )
		{
			final int final_i = i;
			
			((I_TextBox)m_inputs[i].getTextBox()).setChangeListener(new I_TextBoxChangeListener()
			{
				@Override
				public void onTextChange()
				{
					E_SignUpCredentialType credential = E_SignUpCredentialType.values()[final_i];
					I_SignUpCredentialValidator validator = credential.getValidator();
					String text = m_inputs[final_i].getText();
					//s_logger.severe(text);
					m_errorFields[final_i].setError(credential, validator.validateCredential(text), null);
				}

				@Override
				public void onEnterPressed()
				{
					submit(final_i);
				}
			});
			
			m_inputs[i].getTextBox().addStyleName("sm_signinorup_field");
			
			m_inputs[i].getTextBox().addBlurHandler(new BlurHandler()
			{
				@Override
				public void onBlur(BlurEvent event)
				{
					I_ValidationError currentError = m_errorFields[final_i].getCurrentError();
					
					if( currentError == null || currentError != null && !currentError.isServerGeneratedError() )
					{
						E_SignUpCredentialType credential = E_SignUpCredentialType.values()[final_i];
						I_SignUpCredentialValidator validator = credential.getValidator();
						
						m_errorFields[final_i].setError(credential, validator.validateCredential(m_inputs[final_i].getText()), null);
					}
				}
			});
		}
		
		m_viewContext.clickMngr.addClickHandler(m_button, new I_ClickHandler()
		{
			@Override
			public void onClick(int x, int y)
			{
				submit(-1);
			}
		});
		
		m_stack.setCellHorizontalAlignment(m_button, HasHorizontalAlignment.ALIGN_RIGHT);
		
		final Label header = new Label("I don't have an account...");
		header.getElement().getStyle().setMarginLeft(4, Unit.PX);
		header.getElement().getStyle().setMarginBottom(2, Unit.PX);
		
		m_panel.add(m_stack);
		
		this.add(header);
		this.add(m_panel);
		
		this.setCellHorizontalAlignment(header, HasHorizontalAlignment.ALIGN_CENTER);
	}
	
	private void onRecaptchaClicked()
	{
		m_errorFields[E_SignUpCredentialType.CAPTCHA_RESPONSE.ordinal()].reset();
		m_captchaImageContainer.getElement().getFirstChildElement().getStyle().setVisibility(Visibility.HIDDEN);
		m_captchaImageContainer.getElement().getFirstChildElement().setAttribute("alt", "");
	}
	
	private void submit(int focusedFieldIndex)
	{
		boolean canSubmit = true;
		
		for( int i = 0; i < m_errorFields.length; i++ )
		{
			m_errorFields[i].m_blockEmptyErrors = false;
			
			E_SignUpCredentialType credential = E_SignUpCredentialType.values()[i];
			I_ValidationError currentError = m_errorFields[i].getCurrentError();
			I_ValidationError newError = null;
			
			if( currentError != null && currentError.isServerGeneratedError() )
			{
				newError = currentError;
			}
			else
			{
				I_SignUpCredentialValidator validator = E_SignUpCredentialType.values()[i].getValidator();
				newError = validator.validateCredential(m_inputs[i].getText());
				m_errorFields[i].setError(credential, newError, null);
			}
			
			if( !newError.isRetryable() )
			{
				if( newError.isError() )
				{
					canSubmit = false;
				}
			}
			else
			{
				m_errorFields[i].setError(credential, E_SignUpValidationError.NO_ERROR, null);
			}
		}
		
		if( canSubmit )
		{
			boolean rememberMe = m_checkbox.isChecked();
			String captchaResponse = m_viewContext.recaptchaWrapper.getResponse();
			String captchaChallenge = m_viewContext.recaptchaWrapper.getChallenge();
			String[] args = {m_inputs[0].getText(), m_inputs[1].getText(), m_inputs[2].getText(), captchaResponse, captchaChallenge};
			SignUpCredentials creds = new SignUpCredentials(rememberMe, args);
			
			m_args_SignUp.setCreds(creds);
			m_viewContext.stateContext.perform(Action_SignInOrUp_SignUp.class, m_args_SignUp);
			
			m_lastFocusedFieldIndex = focusedFieldIndex;
			
			if( m_lastFocusedFieldIndex >= 0 )
			{
				m_inputs[m_lastFocusedFieldIndex].getTextBox().setFocus(false);
			}
		}
		else
		{
			m_lastFocusedFieldIndex = -1;
		}
	}
	
	/**
	 * This is so parent can get rid of any focus tool tips when exiting.
	 */
	public void blur()
	{
		for( int i = 0; i < m_inputs.length; i++ )
		{
			m_inputs[i].getElement().blur();
		}
	}
	
	private void updateToolTips()
	{
		ToolTipManager toolTipper = m_viewContext.toolTipMngr;
		
		for( int i = 0; i < m_inputs.length; i++ )
		{
			toolTipper.onTipMove(m_inputs[i].getTextBox());
		}
	}
	
	public void onResizeOrScroll()
	{
		updateToolTips();
	}

	@Override
	public void onStateEvent(StateEvent event)
	{
		switch( event.getType() )
		{
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof State_SignInOrUp )
				{
					ClientAccountManager accountManager = m_viewContext.appContext.accountMngr;
					SignUpValidationResult result = accountManager.checkOutLatestBadSignUpResult();
					
					if( result != null )
					{
						for( int i = 0; i < m_errorFields.length; i++ )
						{
							E_SignUpCredentialType credential = E_SignUpCredentialType.values()[i];
							E_SignUpValidationError error = result.getError(credential);
							
							if( error != E_SignUpValidationError.CAPTCHA_INCORRECT && i == E_SignUpCredentialType.CAPTCHA_RESPONSE.ordinal() )
							{
								m_errorFields[i].m_blockEmptyErrors = true;
								m_errorFields[i].setError(credential, E_SignUpValidationError.EMPTY, null);
							}
							else
							{
								m_errorFields[i].m_blockEmptyErrors = false;								
								m_errorFields[i].setError(credential, error, null);
							}
						}
					}
					
					if( m_lastFocusedFieldIndex >= 0 )
					{
						m_inputs[m_lastFocusedFieldIndex].getTextBox().setFocus(true);
						
						m_lastFocusedFieldIndex = -1;
					}
				}
				
				break;
			}
			
			case DID_PERFORM_ACTION:
			{
				if( event.getAction() == StateMachine_Base.OnAccountManagerResponse.class )
				{
					StateMachine_Base.OnAccountManagerResponse.Args args = event.getActionArgs();
					
					ClientAccountManager.E_ResponseType responseType = args.getType();
					
					switch(responseType)
					{
						case SIGN_UP_SUCCESS:
						case SIGN_IN_SUCCESS:
						case PASSWORD_CONFIRM_SUCCESS:
						{
							for( int i = 0; i < m_inputs.length; i++ )
							{
								m_inputs[i].getTextBox().setText("");
								
								m_errorFields[i].reset();
							}
							
							m_lastFocusedFieldIndex = -1;
							
							break;
						}
					}
				}
				
				break;
			}
		}
	}
}