package swarm.client.view.tabs.account;

import swarm.client.app.AppContext;
import swarm.client.input.ClickManager;
import swarm.client.input.I_ClickHandler;
import swarm.client.managers.ClientAccountManager;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.account.Action_SignInOrUp_SetNewPassword;
import swarm.client.states.account.Action_SignInOrUp_SignIn;
import swarm.client.states.account.State_AccountStatusPending;
import swarm.client.states.account.State_SignInOrUp;
import swarm.client.view.ViewContext;
import swarm.client.view.tooltip.E_ToolTipType;
import swarm.client.view.tooltip.ToolTipConfig;
import swarm.client.view.tooltip.ToolTipManager;
import swarm.client.view.widget.I_TextBox;
import swarm.client.view.widget.I_TextBoxChangeListener;
import swarm.client.view.widget.PasswordTextField;
import swarm.client.view.widget.TextField;
import swarm.client.view.widget.TextBoxWrapper;
import swarm.shared.account.E_SignInCredentialType;
import swarm.shared.account.E_SignInValidationError;
import swarm.shared.account.E_SignUpCredentialType;
import swarm.shared.account.E_SignUpValidationError;
import swarm.shared.account.I_CredentialType;
import swarm.shared.account.I_SignInCredentialValidator;
import swarm.shared.account.I_SignUpCredentialValidator;
import swarm.shared.account.I_ValidationError;
import swarm.shared.account.SignInCredentials;
import swarm.shared.account.SignInValidationResult;
import swarm.shared.account.SignUpCredentials;
import swarm.shared.account.SignUpValidationResult;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.ActionEvent;
import swarm.shared.statemachine.I_StateEventListener;
import swarm.shared.statemachine.A_BaseStateEvent;

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

public class SignInPanel extends VerticalPanel implements I_StateEventListener
{
	enum E_SubmitType
	{
		SIGN_IN, RESET_PASSWORD;
	};
	
	private final FlowPanel m_panel = new FlowPanel();
	
	private final VerticalPanel m_stack = new VerticalPanel();
	
	private final SignInOrUpErrorField m_emailErrorField = new SignInOrUpErrorField();
	private final SignInOrUpErrorField m_passwordErrorField = new SignInOrUpErrorField();
	
	private final TextBoxWrapper m_emailInput = new TextBoxWrapper(new TextField("Email"));
	private final TextBoxWrapper m_passwordInput = new TextBoxWrapper(new PasswordTextField("Password"));
	
	private final SignInOrUpButton m_button = new SignInOrUpButton();
	private final StaySignedInCheckbox m_checkbox;
	
	private SignInOrUpErrorField 	m_errorFields[] = {m_emailErrorField, m_passwordErrorField};
	private TextBoxWrapper	m_inputs[] = {m_emailInput, m_passwordInput};
	
	private int m_lastFocusedFieldIndex = -1;
	
	private final Action_SignInOrUp_SignIn.Args m_actionArgs = new Action_SignInOrUp_SignIn.Args();
	
	private final Anchor m_changePassword = new Anchor();
	
	private final ViewContext m_viewContext;

	SignInPanel(ViewContext viewContext)
	{
		m_viewContext = viewContext;
		
		m_checkbox = new StaySignedInCheckbox(m_viewContext);
		
		this.addStyleName("sm_signuporin_sub_panel_wrapper");
		m_panel.addStyleName("sm_signuporin_sub_panel");
		m_stack.setWidth("100%");
		m_button.addStyleName("sm_signin_button");
		
		m_viewContext.toolTipMngr.addTip(m_button, new ToolTipConfig(E_ToolTipType.MOUSE_OVER, "Do it!"));
		m_viewContext.toolTipMngr.addTip(m_changePassword, new ToolTipConfig(E_ToolTipType.MOUSE_OVER,
				"Forgot your password? Enter a new one along with your e-mail or username, then click here."));
		
		FlowPanel passwordResetContainer = new FlowPanel();
		m_changePassword.setHref("javascript:void(0)");
		m_changePassword.setText("change");
		m_changePassword.addStyleName("sm_js_anchor");
		m_viewContext.clickMngr.addClickHandler(m_changePassword, new I_ClickHandler()
		{
			@Override
			public void onClick(int x, int y)
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
			
			((I_TextBox)m_inputs[i].getTextBox()).setChangeListener(new I_TextBoxChangeListener()
			{
				@Override
				public void onTextChange()
				{
					E_SignInCredentialType credential = E_SignInCredentialType.values()[final_i];
					I_SignInCredentialValidator validator = credential.getValidator();
					
					E_SignInValidationError error = validator.validateCredential(m_inputs[final_i].getText());
					
					E_SignInValidationError visibleError = filterErrorFromTextChange(error);
					
					m_errorFields[final_i].setError(credential, error, visibleError);
					
					if( final_i == E_SignInCredentialType.PASSWORD.ordinal() )
					{
						error = (E_SignInValidationError) m_errorFields[E_SignInCredentialType.EMAIL.ordinal()].getCurrentError();
						if( error == E_SignInValidationError.UNKNOWN_COMBINATION )
						{
							m_errorFields[E_SignInCredentialType.EMAIL.ordinal()].setError(credential, E_SignInValidationError.NO_ERROR, E_SignInValidationError.NO_ERROR);
						}
					}
				}

				@Override
				public void onEnterPressed()
				{
					submit(final_i, E_SubmitType.SIGN_IN);
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
						E_SignInCredentialType credential = E_SignInCredentialType.values()[final_i];
						I_SignInCredentialValidator validator = credential.getValidator();
						
						E_SignInValidationError error = validator.validateCredential(m_inputs[final_i].getText());
						
						E_SignInValidationError visibleError = filterErrorFromTextChange(error);
						
						m_errorFields[final_i].setError(credential, error, visibleError);
					}
				}
			});
		}
		
		m_viewContext.clickMngr.addClickHandler(m_button, new I_ClickHandler()
		{
			@Override
			public void onClick(int x, int y)
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
	
	private E_SignInValidationError filterErrorFromTextChange(E_SignInValidationError original)
	{
		if( original == E_SignInValidationError.PASSWORD_TOO_SHORT )
		{
			original = E_SignInValidationError.NO_ERROR;
		}
		else if( original == E_SignInValidationError.EMPTY )
		{
			original = E_SignInValidationError.NO_ERROR;
		}
		
		return original;
	}
	
	private void submit(int focusedFieldIndex, E_SubmitType type)
	{
		boolean canSubmit = true;
		
		for( int i = 0; i < m_errorFields.length; i++ )
		{
			m_errorFields[i].m_blockEmptyErrors = false;
			
			E_SignInCredentialType credential = E_SignInCredentialType.values()[i];
			I_ValidationError currentError = m_errorFields[i].getCurrentError();
			I_ValidationError newError = null;
			
			if( currentError != null )//&& currentError.isServerGeneratedError() )
			{
				if( type == E_SubmitType.RESET_PASSWORD )
				{
					if( currentError == E_SignInValidationError.UNKNOWN_COMBINATION )
					{
						newError  = E_SignInValidationError.NO_ERROR;
					}
					else if( currentError == E_SignInValidationError.NO_ERROR && i == E_SignInCredentialType.PASSWORD.ordinal() )
					{
						newError = E_SignInCredentialType.PASSWORD.getValidator().validateCredential(m_inputs[i].getText());
					}
					else
					{
						newError = currentError;
					}
				}
				else
				{
					if( currentError == E_SignInValidationError.PASSWORD_TOO_SHORT )
					{
						newError = E_SignInValidationError.NO_ERROR;
					}
					else
					{
						newError = currentError;
					}
				}
			}
			else
			{
				I_SignInCredentialValidator validator = credential.getValidator();
				newError = validator.validateCredential(m_inputs[i].getText());
				
				if( type == E_SubmitType.SIGN_IN )
				{
					if( newError == E_SignInValidationError.PASSWORD_TOO_SHORT )
					{
						newError = E_SignInValidationError.NO_ERROR;
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
				m_errorFields[i].setError(credential, E_SignInValidationError.NO_ERROR, E_SignInValidationError.NO_ERROR);
			}
		}
		
		if( canSubmit )
		{
			boolean rememberMe = m_checkbox.isChecked();
			
			SignInCredentials creds = new SignInCredentials(rememberMe, m_inputs[0].getText(), m_inputs[1].getText());
			m_actionArgs.setCreds(creds);
			
			if( type == E_SubmitType.SIGN_IN )
			{
				m_viewContext.stateContext.perform(Action_SignInOrUp_SignIn.class, m_actionArgs);
			}
			else if( type == E_SubmitType.RESET_PASSWORD )
			{
				m_viewContext.stateContext.perform(Action_SignInOrUp_SetNewPassword.class, m_actionArgs);
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
	public void onStateEvent(A_BaseStateEvent event)
	{
		switch( event.getType() )
		{
			case DID_FOREGROUND:
			{
				if( event.getState() instanceof State_SignInOrUp )
				{
					ClientAccountManager accountManager = m_viewContext.appContext.accountMngr;
					SignInValidationResult result = accountManager.checkOutLatestBadSignInResult();
					if( result != null )
					{
						for( int i = 0; i < m_errorFields.length; i++ )
						{
							m_errorFields[i].m_blockEmptyErrors = false;
							E_SignInCredentialType credential = E_SignInCredentialType.values()[i];
							E_SignInValidationError error = result.getError(credential);
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
				ActionEvent event_cast = event.cast();
				
				if( event.getTargetClass() == StateMachine_Base.OnAccountManagerResponse.class )
				{
					StateMachine_Base.OnAccountManagerResponse.Args args = event_cast.getArgsIn();
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
