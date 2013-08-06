package b33hive.client.ui.tabs.account;

import b33hive.client.app.bh_c;
import b33hive.client.input.bhClickManager;
import b33hive.client.input.bhI_ClickHandler;
import b33hive.client.managers.bhClientAccountManager;
import b33hive.client.states.StateMachine_Base;
import b33hive.client.states.account.State_AccountStatusPending;
import b33hive.client.states.account.State_SignInOrUp;
import b33hive.client.ui.tooltip.bhE_ToolTipType;
import b33hive.client.ui.tooltip.bhToolTipConfig;
import b33hive.client.ui.tooltip.bhToolTipManager;
import b33hive.client.ui.widget.bhI_TextBox;
import b33hive.client.ui.widget.bhI_TextBoxChangeListener;
import b33hive.client.ui.widget.bhPasswordTextBox;
import b33hive.client.ui.widget.bhTextBox;
import b33hive.client.ui.widget.bhTextBoxWrapper;
import b33hive.shared.account.bhE_SignInCredentialType;
import b33hive.shared.account.bhE_SignInValidationError;
import b33hive.shared.account.bhE_SignUpCredentialType;
import b33hive.shared.account.bhE_SignUpValidationError;
import b33hive.shared.account.bhI_CredentialType;
import b33hive.shared.account.bhI_SignInCredentialValidator;
import b33hive.shared.account.bhI_SignUpCredentialValidator;
import b33hive.shared.account.bhI_ValidationError;
import b33hive.shared.account.bhSignInCredentials;
import b33hive.shared.account.bhSignInValidationResult;
import b33hive.shared.account.bhSignUpCredentials;
import b33hive.shared.account.bhSignUpValidationResult;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhI_StateEventListener;
import b33hive.shared.statemachine.bhStateEvent;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class bhSignInPanel extends VerticalPanel implements bhI_StateEventListener
{
	enum E_SubmitType
	{
		SIGN_IN, RESET_PASSWORD;
	};
	
	private final FlowPanel m_panel = new FlowPanel();
	
	private final VerticalPanel m_stack = new VerticalPanel();
	
	private final bhSignInOrUpErrorField m_emailErrorField = new bhSignInOrUpErrorField();
	private final bhSignInOrUpErrorField m_passwordErrorField = new bhSignInOrUpErrorField();
	
	private final bhTextBoxWrapper m_emailInput = new bhTextBoxWrapper(new bhTextBox("Email"));
	private final bhTextBoxWrapper m_passwordInput = new bhTextBoxWrapper(new bhPasswordTextBox("Password"));
	
	private final bhSignInOrUpButton m_button = new bhSignInOrUpButton();
	private final bhStaySignedInCheckbox m_checkbox = new bhStaySignedInCheckbox();
	
	private bhSignInOrUpErrorField 	m_errorFields[] = {m_emailErrorField, m_passwordErrorField};
	private bhTextBoxWrapper	m_inputs[] = {m_emailInput, m_passwordInput};
	
	private int m_lastFocusedFieldIndex = -1;
	
	private final State_SignInOrUp.SignIn.Args m_actionArgs = new State_SignInOrUp.SignIn.Args();
	
	private final Anchor m_changePassword = new Anchor();

	bhSignInPanel()
	{
		this.addStyleName("bh_signuporin_sub_panel_wrapper");
		m_panel.addStyleName("bh_signuporin_sub_panel");
		m_stack.setWidth("100%");
		m_button.addStyleName("bh_signin_button");
		
		bh_c.toolTipMngr.addTip(m_button, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Do it!"));
		bh_c.toolTipMngr.addTip(m_changePassword, new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER,
				"If you forgot your password, enter a new one along with your e-mail, then click here."));
		
		FlowPanel passwordResetContainer = new FlowPanel();
		m_changePassword.setHref("javascript:void(0)");
		m_changePassword.setText("change");
		m_changePassword.addStyleName("bh_js_anchor");
		bh_c.clickMngr.addClickHandler(m_changePassword, new bhI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				submit(-1, E_SubmitType.RESET_PASSWORD);
			}
		});
		passwordResetContainer.add(m_changePassword);
		m_changePassword.getElement().getStyle().setMarginLeft(3, Unit.PX);
		
		m_passwordInput.getTextBox().getElement().getStyle().setMarginBottom(0, Unit.PX);
		
		m_button.setText("Sign In");
		
		//--- DRK > Register callbacks so we can display error messages when text in the input fields change.
		for( int i = 0; i < m_errorFields.length; i++ )
		{
			final int final_i = i;
			
			((bhI_TextBox)m_inputs[i].getTextBox()).setChangeListener(new bhI_TextBoxChangeListener()
			{
				@Override
				public void onTextChange()
				{
					bhE_SignInCredentialType credential = bhE_SignInCredentialType.values()[final_i];
					bhI_SignInCredentialValidator validator = credential.getValidator();
					
					bhE_SignInValidationError error = validator.validateCredential(m_inputs[final_i].getText());
					
					bhE_SignInValidationError visibleError = filterErrorFromTextChange(error);
					
					m_errorFields[final_i].setError(credential, error, visibleError);
					
					if( final_i == bhE_SignInCredentialType.PASSWORD.ordinal() )
					{
						error = (bhE_SignInValidationError) m_errorFields[bhE_SignInCredentialType.EMAIL.ordinal()].getCurrentError();
						if( error == bhE_SignInValidationError.UNKNOWN_COMBINATION )
						{
							m_errorFields[bhE_SignInCredentialType.EMAIL.ordinal()].setError(credential, bhE_SignInValidationError.NO_ERROR, bhE_SignInValidationError.NO_ERROR);
						}
					}
				}

				@Override
				public void onEnterPressed()
				{
					submit(final_i, E_SubmitType.SIGN_IN);
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
						bhE_SignInCredentialType credential = bhE_SignInCredentialType.values()[final_i];
						bhI_SignInCredentialValidator validator = credential.getValidator();
						
						bhE_SignInValidationError error = validator.validateCredential(m_inputs[final_i].getText());
						
						bhE_SignInValidationError visibleError = filterErrorFromTextChange(error);
						
						m_errorFields[final_i].setError(credential, error, visibleError);
					}
				}
			});
		}
		
		bh_c.clickMngr.addClickHandler(m_button, new bhI_ClickHandler()
		{
			@Override
			public void onClick()
			{
				submit(-1, E_SubmitType.SIGN_IN);
			}
		});
		
		m_stack.add(m_emailErrorField);
		m_stack.add(m_emailInput);
		m_stack.add(m_passwordErrorField);
		m_stack.add(m_passwordInput);
		m_stack.add(passwordResetContainer);
		
		m_stack.setCellHorizontalAlignment(m_emailErrorField, HasHorizontalAlignment.ALIGN_RIGHT);
		m_stack.setCellHorizontalAlignment(m_passwordErrorField, HasHorizontalAlignment.ALIGN_RIGHT);
		
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

		final Label header = new Label("I have an account...");
		header.getElement().getStyle().setMarginLeft(4, Unit.PX);
		header.getElement().getStyle().setMarginBottom(2, Unit.PX);
		
		m_panel.add(m_stack);
		
		this.add(header);
		this.add(m_panel);
		
		this.setCellHorizontalAlignment(header, HasHorizontalAlignment.ALIGN_CENTER);
	}
	
	private bhE_SignInValidationError filterErrorFromTextChange(bhE_SignInValidationError original)
	{
		if( original == bhE_SignInValidationError.PASSWORD_TOO_SHORT )
		{
			original = bhE_SignInValidationError.NO_ERROR;
		}
		else if( original == bhE_SignInValidationError.EMPTY )
		{
			original = bhE_SignInValidationError.NO_ERROR;
		}
		
		return original;
	}
	
	private void submit(int focusedFieldIndex, E_SubmitType type)
	{
		boolean canSubmit = true;
		
		for( int i = 0; i < m_errorFields.length; i++ )
		{
			m_errorFields[i].m_blockEmptyErrors = false;
			
			bhE_SignInCredentialType credential = bhE_SignInCredentialType.values()[i];
			bhI_ValidationError currentError = m_errorFields[i].getCurrentError();
			bhI_ValidationError newError = null;
			
			if( currentError != null )//&& currentError.isServerGeneratedError() )
			{
				if( type == E_SubmitType.RESET_PASSWORD )
				{
					if( currentError == bhE_SignInValidationError.UNKNOWN_COMBINATION )
					{
						newError  = bhE_SignInValidationError.NO_ERROR;
					}
					else if( currentError == bhE_SignInValidationError.NO_ERROR && i == bhE_SignInCredentialType.PASSWORD.ordinal() )
					{
						newError = bhE_SignInCredentialType.PASSWORD.getValidator().validateCredential(m_inputs[i].getText());
					}
					else
					{
						newError = currentError;
					}
				}
				else
				{
					if( currentError == bhE_SignInValidationError.PASSWORD_TOO_SHORT )
					{
						newError = bhE_SignInValidationError.NO_ERROR;
					}
					else
					{
						newError = currentError;
					}
				}
			}
			else
			{
				bhI_SignInCredentialValidator validator = credential.getValidator();
				newError = validator.validateCredential(m_inputs[i].getText());
				
				if( type == E_SubmitType.SIGN_IN )
				{
					if( newError == bhE_SignInValidationError.PASSWORD_TOO_SHORT )
					{
						newError = bhE_SignInValidationError.NO_ERROR;
					}
				}
			}
			
			if( !newError.isRetryable() )
			{
				if( newError.isError() )
				{
					canSubmit = false;
				}
				
				m_errorFields[i].setError(credential, newError, newError);
			}
			else
			{
				m_errorFields[i].setError(credential, bhE_SignInValidationError.NO_ERROR, bhE_SignInValidationError.NO_ERROR);
			}
		}
		
		if( canSubmit )
		{
			boolean rememberMe = m_checkbox.isChecked();
			
			bhSignInCredentials creds = new bhSignInCredentials(rememberMe, m_inputs[0].getText(), m_inputs[1].getText());
			m_actionArgs.setCreds(creds);
			
			if( type == E_SubmitType.SIGN_IN )
			{
				bhA_Action.perform(State_SignInOrUp.SignIn.class, m_actionArgs);
			}
			else if( type == E_SubmitType.RESET_PASSWORD )
			{
				bhA_Action.perform(State_SignInOrUp.SetNewPassword.class, m_actionArgs);
			}
			
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
					bhSignInValidationResult result = accountManager.checkOutLatestBadSignInResult();
					if( result != null )
					{
						for( int i = 0; i < m_errorFields.length; i++ )
						{
							m_errorFields[i].m_blockEmptyErrors = false;
							bhE_SignInCredentialType credential = bhE_SignInCredentialType.values()[i];
							bhE_SignInValidationError error = result.getError(credential);
							m_errorFields[i].setError(credential, error, error);
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
