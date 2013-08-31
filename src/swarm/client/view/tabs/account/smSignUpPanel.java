package swarm.client.view.tabs.account;

import java.util.logging.Logger;

import swarm.client.managers.smClientAccountManager;
import swarm.client.app.smAppContext;
import swarm.client.input.smClickManager;
import swarm.client.input.smI_ClickHandler;
import swarm.client.js.smU_Native;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.StateMachine_Tabs;
import swarm.client.states.account.Action_SignInOrUp_SignUp;
import swarm.client.states.account.State_AccountStatusPending;
import swarm.client.states.account.State_SignInOrUp;
import swarm.client.view.smInitialSyncScreen;
import swarm.client.view.smS_UI;
import swarm.client.view.alignment.smAlignmentDefinition;
import swarm.client.view.alignment.smE_AlignmentPosition;
import swarm.client.view.alignment.smE_AlignmentType;
import swarm.client.view.alignment.smU_Alignment;
import swarm.client.view.tooltip.smE_ToolTipType;
import swarm.client.view.tooltip.smToolTipConfig;
import swarm.client.view.tooltip.smToolTipManager;
import swarm.client.view.widget.smHtmlWrapper;
import swarm.client.view.widget.smI_TextBox;
import swarm.client.view.widget.smI_TextBoxChangeListener;
import swarm.client.view.widget.smPasswordTextBox;
import swarm.client.view.widget.smTextBox;
import swarm.client.view.widget.smTextBoxWrapper;
import swarm.shared.app.smS_App;
import swarm.shared.account.smE_SignInCredentialType;
import swarm.shared.account.smE_SignInValidationError;
import swarm.shared.account.smE_SignUpValidationError;
import swarm.shared.account.smE_SignUpCredentialType;
import swarm.shared.account.smI_SignUpCredentialValidator;
import swarm.shared.account.smI_ValidationError;
import swarm.shared.account.smS_Account;
import swarm.shared.account.smSignUpCredentials;
import swarm.shared.account.smSignUpValidationResult;
import swarm.shared.code.smU_Code;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smI_StateEventListener;
import swarm.shared.statemachine.smStateEvent;
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
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class smSignUpPanel extends VerticalPanel implements smI_StateEventListener
{
	private static final Logger s_logger = Logger.getLogger(smSignUpPanel.class.getName());
	
	private final VerticalPanel m_stack = new VerticalPanel();
	
	private final FlowPanel m_panel = new FlowPanel();
	
	private final smSignInOrUpErrorField m_emailErrorField = new smSignInOrUpErrorField();
	private final smSignInOrUpErrorField m_usernameErrorField = new smSignInOrUpErrorField();
	private final smSignInOrUpErrorField m_passwordErrorField = new smSignInOrUpErrorField();
	private final smSignInOrUpErrorField m_captchaErrorField = new smSignInOrUpErrorField();
	
	private final smHtmlWrapper m_captchaImageContainer;
	
	private final smStaySignedInCheckbox m_checkbox = new smStaySignedInCheckbox();
	
	private final smTextBoxWrapper m_emailInput = new smTextBoxWrapper(new smTextBox("Email"));
	private final smTextBoxWrapper m_usernameInput = new smTextBoxWrapper(new smTextBox("Username"));
	private final smTextBoxWrapper m_passwordInput = new smTextBoxWrapper(new smPasswordTextBox("Password"));
	private final smTextBoxWrapper m_captchaInput;
	
	private final FlowPanel m_captchaControlContainer = new FlowPanel();
	
	private final Anchor m_reloadCaptcha = new Anchor();
	private final Anchor m_captchaHelp = new Anchor();
	
	private final smSignInOrUpButton m_button = new smSignInOrUpButton();
	
	private final smSignInOrUpErrorField 	m_errorFields[] = {m_emailErrorField, m_usernameErrorField, m_passwordErrorField, m_captchaErrorField};
	private final smTextBoxWrapper			m_inputs[] = {m_emailInput, m_usernameInput, m_passwordInput, null};
	
	private int m_lastFocusedFieldIndex = -1;
	
	private final Action_SignInOrUp_SignUp.Args m_args_SignUp = new Action_SignInOrUp_SignUp.Args();
	
	smSignUpPanel()
	{
		this.addStyleName("sm_signuporin_sub_panel_wrapper");
		m_panel.addStyleName("sm_signuporin_sub_panel");
		m_stack.setWidth("100%");
		
		smAppContext.toolTipMngr.addTip(m_button, new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Do it now!"));
		
		Element recaptchaResponseFieldElement = Document.get().getElementById("recaptcha_response_field");
		recaptchaResponseFieldElement.removeFromParent();
		recaptchaResponseFieldElement.getStyle().clearDisplay();
		
		m_captchaInput = new smTextBoxWrapper(new smTextBox(recaptchaResponseFieldElement, "Enter The Words Below"));
		m_captchaInput.getTextBox().addStyleName("sm_recaptcha_field");
		m_inputs[3] = m_captchaInput;
		
		final Element recaptchaImageElement = Document.get().getElementById("recaptcha_image");
		recaptchaImageElement.removeFromParent();
		recaptchaImageElement.getStyle().clearDisplay();
		
		recaptchaImageElement.getStyle().clearWidth();
		recaptchaImageElement.getStyle().clearHeight();
		m_captchaImageContainer = new smHtmlWrapper(recaptchaImageElement);
		m_captchaImageContainer.addStyleName("sm_captcha_container");
		m_captchaImageContainer.addStyleName("sm_signinorup_element");
		
		m_reloadCaptcha.getElement().getStyle().setMarginLeft(3, Unit.PX);
		m_captchaHelp.getElement().getStyle().setMarginLeft(12, Unit.PX);
		
		m_captchaHelp.setHref("javascript:Recaptcha.showhelp()");
		m_reloadCaptcha.setHref("javascript:Recaptcha.reload('t')");
		
		smAppContext.clickMngr.addClickHandler(m_reloadCaptcha, new smI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				m_errorFields[smE_SignUpCredentialType.CAPTCHA_RESPONSE.ordinal()].reset();
				recaptchaImageElement.getFirstChildElement().getStyle().setVisibility(Visibility.HIDDEN);
				recaptchaImageElement.getFirstChildElement().setAttribute("alt", "");
			}
		});
		
		m_reloadCaptcha.addStyleName("sm_js_anchor");
		m_captchaHelp.addStyleName("sm_js_anchor");
		
		m_captchaControlContainer.add(m_reloadCaptcha);
		m_captchaControlContainer.add(m_captchaHelp);
		
		m_reloadCaptcha.setText("reload");
		m_captchaHelp.setText("help");
		
		m_button.addStyleName("sm_signup_button");
		
		smAlignmentDefinition alignment = smU_Alignment.createHorRightVerCenter(smS_UI.TOOl_TIP_PADDING);
		alignment.setPadding(smE_AlignmentType.MASTER_ANCHOR_VERTICAL, -1.0);
		
		smToolTipManager toolTipper = smAppContext.toolTipMngr;
		smToolTipConfig config = null;
		
		config = new smToolTipConfig(smE_ToolTipType.FOCUS, alignment, "Uniquely identifies you.  Invisible to others.");
		toolTipper.addTip(m_emailInput.getTextBox(), config);
		
		config = new smToolTipConfig(smE_ToolTipType.FOCUS, alignment, "Your public username, as in b33hive.net/my_username.  Letters, numbers and underscores only, 1-"+smS_Account.MAX_USERNAME_LENGTH+" characters.");
		toolTipper.addTip(m_usernameInput.getTextBox(), config);
		
		config = new smToolTipConfig(smE_ToolTipType.FOCUS, alignment, "[insert advice on choosing password].  Must be "+smS_Account.MIN_PASSWORD_LENGTH+" or more characters.");
		toolTipper.addTip(m_passwordInput.getTextBox(), config);
		
		config = new smToolTipConfig(smE_ToolTipType.FOCUS, alignment, "This reasonably ensures that you're a biological entity.");
		toolTipper.addTip(m_captchaInput.getTextBox(), config);
		
		config = new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "What is this thing?");
		toolTipper.addTip(m_captchaHelp, config);
		
		config = new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Click here to get new words.");
		toolTipper.addTip(m_reloadCaptcha, config);
		
		m_usernameInput.getTextBox().setMaxLength(smS_Account.MAX_USERNAME_LENGTH);
		m_emailInput.getTextBox().setMaxLength(smS_Account.MAX_EMAIL_LENGTH);
		
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
			
			((smI_TextBox)m_inputs[i].getTextBox()).setChangeListener(new smI_TextBoxChangeListener()
			{
				@Override
				public void onTextChange()
				{
					smE_SignUpCredentialType credential = smE_SignUpCredentialType.values()[final_i];
					smI_SignUpCredentialValidator validator = credential.getValidator();
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
					smI_ValidationError currentError = m_errorFields[final_i].getCurrentError();
					
					if( currentError == null || currentError != null && !currentError.isServerGeneratedError() )
					{
						smE_SignUpCredentialType credential = smE_SignUpCredentialType.values()[final_i];
						smI_SignUpCredentialValidator validator = credential.getValidator();
						
						m_errorFields[final_i].setError(credential, validator.validateCredential(m_inputs[final_i].getText()), null);
					}
				}
			});
		}
		
		smAppContext.clickMngr.addClickHandler(m_button, new smI_ClickHandler()
		{
			@Override
			public void onClick()
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
	
	private void submit(int focusedFieldIndex)
	{
		boolean canSubmit = true;
		
		for( int i = 0; i < m_errorFields.length; i++ )
		{
			m_errorFields[i].m_blockEmptyErrors = false;
			
			smE_SignUpCredentialType credential = smE_SignUpCredentialType.values()[i];
			smI_ValidationError currentError = m_errorFields[i].getCurrentError();
			smI_ValidationError newError = null;
			
			if( currentError != null && currentError.isServerGeneratedError() )
			{
				newError = currentError;
			}
			else
			{
				smI_SignUpCredentialValidator validator = smE_SignUpCredentialType.values()[i].getValidator();
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
				m_errorFields[i].setError(credential, smE_SignUpValidationError.NO_ERROR, null);
			}
		}
		
		if( canSubmit )
		{
			boolean rememberMe = m_checkbox.isChecked();
			String captchaResponse = smAppContext.recaptchaWrapper.getResponse();
			String captchaChallenge = smAppContext.recaptchaWrapper.getChallenge();
			String[] args = {m_inputs[0].getText(), m_inputs[1].getText(), m_inputs[2].getText(), captchaResponse, captchaChallenge};
			smSignUpCredentials creds = new smSignUpCredentials(rememberMe, args);
			
			m_args_SignUp.setCreds(creds);
			smA_Action.perform(Action_SignInOrUp_SignUp.class, m_args_SignUp);
			
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
		smToolTipManager toolTipper = smAppContext.toolTipMngr;
		
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
	public void onStateEvent(smStateEvent event)
	{
		switch( event.getType() )
		{
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof State_SignInOrUp )
				{
					smClientAccountManager accountManager = smAppContext.accountMngr;
					smSignUpValidationResult result = accountManager.checkOutLatestBadSignUpResult();
					
					if( result != null )
					{
						for( int i = 0; i < m_errorFields.length; i++ )
						{
							smE_SignUpCredentialType credential = smE_SignUpCredentialType.values()[i];
							smE_SignUpValidationError error = result.getError(credential);
							
							if( error != smE_SignUpValidationError.CAPTCHA_INCORRECT && i == smE_SignUpCredentialType.CAPTCHA_RESPONSE.ordinal() )
							{
								m_errorFields[i].m_blockEmptyErrors = true;
								m_errorFields[i].setError(credential, smE_SignUpValidationError.EMPTY, null);
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
					
					smClientAccountManager.E_ResponseType responseType = args.getType();
					
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