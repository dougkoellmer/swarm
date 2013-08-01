package com.b33hive.client.ui.tabs.account;

import com.b33hive.shared.account.bhE_SignUpValidationError;
import com.b33hive.shared.account.bhI_CredentialType;
import com.b33hive.shared.account.bhI_ValidationError;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class bhSignInOrUpErrorField extends Label
{
	boolean m_blockEmptyErrors = true;
	
	private bhI_ValidationError m_currentError = null;
	
	bhSignInOrUpErrorField()
	{
		this.addStyleName("bh_signinorup_error");
		
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
	
	public bhI_ValidationError getCurrentError()
	{
		return m_currentError;
	}
	
	void setError(bhI_CredentialType credential, bhI_ValidationError error, bhI_ValidationError visibleError_nullable)
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
