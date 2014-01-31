package swarm.client.view.tabs.account;

import swarm.shared.account.E_SignUpValidationError;
import swarm.shared.account.I_CredentialType;
import swarm.shared.account.I_ValidationError;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class SignInOrUpErrorField extends Label
{
	boolean m_blockEmptyErrors = true;
	
	private I_ValidationError m_currentError = null;
	
	SignInOrUpErrorField()
	{
		this.addStyleName("sm_signinorup_error");
		
		setEmptyErrorText();
	}
	
	private void setEmptyErrorText()
	{
		this.getElement().setInnerHTML("&nbsp;");
	}
	
	public void reset()
	{
		m_currentError = null;
		m_blockEmptyErrors = true;
		setEmptyErrorText();
		this.getElement().getStyle().clearColor();
	}
	
	public I_ValidationError getCurrentError()
	{
		return m_currentError;
	}
	
	void setError(I_CredentialType credential, I_ValidationError error, I_ValidationError visibleError_nullable)
	{
		visibleError_nullable = visibleError_nullable != null ? visibleError_nullable : error;
		if( m_blockEmptyErrors )
		{
			if( error.isEmptyError() )
			{
				setEmptyErrorText();
				
				return;
			}
		}
		
		m_currentError = error;

		m_blockEmptyErrors = false;
		
		String errorText = visibleError_nullable.calcErrorText(credential);
		if( !error.isError() )
		{
			this.getElement().getStyle().setColor("green");
		}
		else
		{
			this.getElement().getStyle().setColor("red");
		}
		
		if( errorText.isEmpty() )
		{
			setEmptyErrorText();
		}
		else
		{
			this.setText(errorText);
		}
	}
}
