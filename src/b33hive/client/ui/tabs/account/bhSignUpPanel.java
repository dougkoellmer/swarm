package b33hive.client.ui.tabs.account;

import java.util.logging.Logger;

import b33hive.client.managers.bhClientAccountManager;
import b33hive.client.app.bh_c;
import b33hive.client.input.bhClickManager;
import b33hive.client.input.bhI_ClickHandler;
import b33hive.client.js.bhU_Native;
import b33hive.client.states.StateMachine_Base;
import b33hive.client.states.StateMachine_Tabs;
import b33hive.client.states.account.State_AccountStatusPending;
import b33hive.client.states.account.State_SignInOrUp;
import b33hive.client.ui.bhInitialSyncScreen;
import b33hive.client.ui.bhS_UI;
import b33hive.client.ui.alignment.bhAlignmentDefinition;
import b33hive.client.ui.alignment.bhE_AlignmentPosition;
import b33hive.client.ui.alignment.bhE_AlignmentType;
import b33hive.client.ui.alignment.bhU_Alignment;
import b33hive.client.ui.tooltip.bhE_ToolTipType;
import b33hive.client.ui.tooltip.bhToolTipConfig;
import b33hive.client.ui.tooltip.bhToolTipManager;
import b33hive.client.ui.widget.bhHtmlWrapper;
import b33hive.client.ui.widget.bhI_TextBox;
import b33hive.client.ui.widget.bhI_TextBoxChangeListener;
import b33hive.client.ui.widget.bhPasswordTextBox;
import b33hive.client.ui.widget.bhTextBox;
import b33hive.client.ui.widget.bhTextBoxWrapper;
import b33hive.shared.app.bhS_App;
import b33hive.shared.account.bhE_SignInCredentialType;
import b33hive.shared.account.bhE_SignInValidationError;
import b33hive.shared.account.bhE_SignUpValidationError;
import b33hive.shared.account.bhE_SignUpCredentialType;
import b33hive.shared.account.bhI_SignUpCredentialValidator;
import b33hive.shared.account.bhI_ValidationError;
import b33hive.shared.account.bhS_Account;
import b33hive.shared.account.bhS_Recaptcha;
import b33hive.shared.account.bhSignUpCredentials;
import b33hive.shared.account.bhSignUpValidationResult;
import b33hive.shared.code.bhU_Code;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.statemachine.bhI_StateEventListener;
import b33hive.shared.statemachine.bhStateEvent;
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

public class bhSignUpPanel extends VerticalPanel implements bhI_StateEventListener
{
	private static final Logger s_logger = Logger.getLogger(bhSignUpPanel.class.getName());
	
	private final VerticalPanel m_stack = new VerticalPanel();
	
	private final FlowPanel m_panel = new FlowPanel();
	
	private final bhSignInOrUpErrorField m_emailErrorField = new bhSignInOrUpErrorField();
	private final bhSignInOrUpErrorField m_usernameErrorField = new bhSignInOrUpErrorField();
	private final bhSignInOrUpErrorField m_passwordErrorField = new bhSignInOrUpErrorField();
	private final bhSignInOrUpErrorField m_captchaErrorField = new bhSignInOrUpErrorField();
	
	private final bhHtmlWrapper m_captchaImageContainer;
	
	private final bhStaySignedInCheckbox m_checkbox = new bhStaySignedInCheckbox();
	
	private final bhTextBoxWrapper m_emailInput = new bhTextBoxWrapper(new bhTextBox("Email"));
	private final bhTextBoxWrapper m_usernameInput = new bhTextBoxWrapper(new bhTextBox("Username"));
	private final bhTextBoxWrapper m_passwordInput = new bhTextBoxWrapper(new bhPasswordTextBox("Password"));
	private final bhTextBoxWrapper m_captchaInput;
	
	private final FlowPanel m_captchaControlContainer = new FlowPanel();
	
	private final Anchor m_reloadCaptcha = new Anchor();
	private final Anchor m_captchaHelp = new Anchor();
	
	private final bhSignInOrUpButton m_button = new bhSignInOrUpButton();
	
	private final bhSignInOrUpErrorField 	m_errorFields[] = {m_emailErrorField, m_usernameErrorField, m_passwordErrorField, m_captchaErrorField};
	private final bhTextBoxWrapper			m_inputs[] = {m_emailInput, m_usernameInput, m_passwordInput, null};
	
	private int m_lastFocusedFieldIndex = -1;
	
	private final State_SignInOrUp.SignUp.Args m_args_SignUp = new State_SignInOrUp.SignUp.Args();
	
	bhSignUpPanel()
	{
		this.addStyleName("bh_signuporin_sub_panel_wrapper");
		m_panel.addStyleName("bh_signuporin_sub_panel");
		m_stack.setWidth("100%");
		
		bh_c.toolTipMngr.addTip(m_button, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Do it now!"));
		
		Element recaptchaResponseFieldElement = Document.get().getElementById("recaptcha_response_field");
		recaptchaResponseFieldElement.removeFromParent();
		recaptchaResponseFieldElement.getStyle().clearDisplay();
		
		m_captchaInput = new bhTextBoxWrapper(new bhTextBox(recaptchaResponseFieldElement, "Enter The Words Below"));
		m_captchaInput.getTextBox().addStyleName("bh_recaptcha_field");
		m_inputs[3] = m_captchaInput;
		
		final Element recaptchaImageElement = Document.get().getElementById("recaptcha_image");
		recaptchaImageElement.removeFromParent();
		recaptchaImageElement.getStyle().clearDisplay();
		
		recaptchaImageElement.getStyle().clearWidth();
		recaptchaImageElement.getStyle().clearHeight();
		m_captchaImageContainer = new bhHtmlWrapper(recaptchaImageElement);
		m_captchaImageContainer.addStyleName("bh_captcha_container");
		m_captchaImageContainer.addStyleName("bh_signinorup_element");
		
		m_reloadCaptcha.getElement().getStyle().setMarginLeft(3, Unit.PX);
		m_captchaHelp.getElement().getStyle().setMarginLeft(12, Unit.PX);
		
		m_captchaHelp.setHref("javascript:Recaptcha.showhelp()");
		m_reloadCaptcha.setHref("javascript:Recaptcha.reload('t')");
		
		bh_c.clickMngr.addClickHandler(m_reloadCaptcha, new bhI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				m_errorFields[bhE_SignUpCredentialType.CAPTCHA_RESPONSE.ordinal()].reset();
				recaptchaImageElement.getFirstChildElement().getStyle().setVisibility(Visibility.HIDDEN);
				recaptchaImageElement.getFirstChildElement().setAttribute("alt", "");
			}
		});
		
		m_reloadCaptcha.addStyleName("bh_js_anchor");
		m_captchaHelp.addStyleName("bh_js_anchor");
		
		m_captchaControlContainer.add(m_reloadCaptcha);
		m_captchaControlContainer.add(m_captchaHelp);
		
		m_reloadCaptcha.setText("reload");
		m_captchaHelp.setText("help");
		
		m_button.addStyleName("bh_signup_button");
		
		bhAlignmentDefinition alignment = bhU_Alignment.createHorRightVerCenter(bhS_UI.TOOl_TIP_PADDING);
		alignment.setPadding(bhE_AlignmentType.MASTER_ANCHOR_VERTICAL, -1.0);
		
		bhToolTipManager toolTipper = bh_c.toolTipMngr;
		bhToolTipConfig config = null;
		
		config = new bhToolTipConfig(bhE_ToolTipType.FOCUS, alignment, "Uniquely identifies you.  Invisible to others.");
		toolTipper.addTip(m_emailInput.getTextBox(), config);
		
		config = new bhToolTipConfig(bhE_ToolTipType.FOCUS, alignment, "Your public username, as in b33hive.net/my_username.  Letters, numbers and underscores only, 1-"+bhS_Account.MAX_USERNAME_LENGTH+" characters.");
		toolTipper.addTip(m_usernameInput.getTextBox(), config);
		
		config = new bhToolTipConfig(bhE_ToolTipType.FOCUS, alignment, "[insert advice on choosing password].  Must be "+bhS_Account.MIN_PASSWORD_LENGTH+" or more characters.");
		toolTipper.addTip(m_passwordInput.getTextBox(), config);
		
		config = new bhToolTipConfig(bhE_ToolTipType.FOCUS, alignment, "This reasonably ensures that you're a biological entity.");
		toolTipper.addTip(m_captchaInput.getTextBox(), config);
		
		config = new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "What is this thing?");
		toolTipper.addTip(m_captchaHelp, config);
		
		config = new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Click here to get new words.");
		toolTipper.addTip(m_reloadCaptcha, config);
		
		m_usernameInput.getTextBox().setMaxLength(bhS_Account.MAX_USERNAME_LENGTH);
		m_emailInput.getTextBox().setMaxLength(bhS_Account.MAX_EMAIL_LENGTH);
		
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
		String termsHref = bhU_Code.transformPathToJavascript("b33hive/terms");
		String privacyHref = bhU_Code.transformPathToJavascript("b33hive/privacy");
		legal.getElement().setInnerHTML("By signing up you agree to our<br><a class='bh_terms_anchor' href=\""+termsHref+"\">Terms</a> and <a class='bh_terms_anchor' href=\""+privacyHref+"\">Privacy Policy</a>.");
		m_stack.add(legal);
		m_stack.setCellHorizontalAlignment(legal, HasHorizontalAlignment.ALIGN_RIGHT);*/
		
		//--- DRK > Register callbacks so we can display error messages when text in the input fields change.
		for( int i = 0; i < m_errorFields.length; i++ )
		{
			final int final_i = i;
			
			((bhI_TextBox)m_inputs[i].getTextBox()).setChangeListener(new bhI_TextBoxChangeListener()
			{
				@Override
				public void onTextChange()
				{
					bhE_SignUpCredentialType credential = bhE_SignUpCredentialType.values()[final_i];
					bhI_SignUpCredentialValidator validator = credential.getValidator();
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
			
			m_inputs[i].getTextBox().addStyleName("bh_signinorup_field");
			
			m_inputs[i].getTextBox().addBlurHandler(new BlurHandler()
			{
				@Override
				public void onBlur(BlurEvent event)
				{
					bhI_ValidationError currentError = m_errorFields[final_i].getCurrentError();
					
					if( currentError == null || currentError != null && !currentError.isServerGeneratedError() )
					{
						bhE_SignUpCredentialType credential = bhE_SignUpCredentialType.values()[final_i];
						bhI_SignUpCredentialValidator validator = credential.getValidator();
						
						m_errorFields[final_i].setError(credential, validator.validateCredential(m_inputs[final_i].getText()), null);
					}
				}
			});
		}
		
		bh_c.clickMngr.addClickHandler(m_button, new bhI_ClickHandler()
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
			
			bhE_SignUpCredentialType credential = bhE_SignUpCredentialType.values()[i];
			bhI_ValidationError currentError = m_errorFields[i].getCurrentError();
			bhI_ValidationError newError = null;
			
			if( currentError != null && currentError.isServerGeneratedError() )
			{
				newError = currentError;
			}
			else
			{
				bhI_SignUpCredentialValidator validator = bhE_SignUpCredentialType.values()[i].getValidator();
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
				m_errorFields[i].setError(credential, bhE_SignUpValidationError.NO_ERROR, null);
			}
		}
		
		if( canSubmit )
		{
			boolean rememberMe = m_checkbox.isChecked();
			String captchaResponse = bh_c.recaptchaWrapper.getResponse();
			String captchaChallenge = bh_c.recaptchaWrapper.getChallenge();
			String[] args = {m_inputs[0].getText(), m_inputs[1].getText(), m_inputs[2].getText(), captchaResponse, captchaChallenge};
			bhSignUpCredentials creds = new bhSignUpCredentials(rememberMe, args);
			
			m_args_SignUp.setCreds(creds);
			bhA_Action.perform(State_SignInOrUp.SignUp.class, m_args_SignUp);
			
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
		bhToolTipManager toolTipper = bh_c.toolTipMngr;
		
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
	public void onStateEvent(bhStateEvent event)
	{
		switch( event.getType() )
		{
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof State_SignInOrUp )
				{
					bhClientAccountManager accountManager = bh_c.accountMngr;
					bhSignUpValidationResult result = accountManager.checkOutLatestBadSignUpResult();
					
					if( result != null )
					{
						for( int i = 0; i < m_errorFields.length; i++ )
						{
							bhE_SignUpCredentialType credential = bhE_SignUpCredentialType.values()[i];
							bhE_SignUpValidationError error = result.getError(credential);
							
							if( error != bhE_SignUpValidationError.CAPTCHA_INCORRECT && i == bhE_SignUpCredentialType.CAPTCHA_RESPONSE.ordinal() )
							{
								m_errorFields[i].m_blockEmptyErrors = true;
								m_errorFields[i].setError(credential, bhE_SignUpValidationError.EMPTY, null);
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
					
					bhClientAccountManager.E_ResponseType responseType = args.getType();
					
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