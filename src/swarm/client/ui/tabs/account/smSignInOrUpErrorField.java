package swarm.client.ui.tabs.account;

import swarm.shared.account.smE_SignUpValidationError;
import swarm.shared.account.smI_CredentialType;
import swarm.shared.account.smI_ValidationError;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class smSignInOrUpErrorField extends Label
{
	boolean m_blockEmptyErrors = true;
	
	private smI_ValidationError m_currentError = null;
	
	smSignInOrUpErrorField()
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
	
	public smI_ValidationError getCurrentError()
	{
		return m_currentError;
	}
	
	void setError(smI_CredentialType credential, smI_ValidationError error, smI_ValidationError visibleError_nullable)
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
