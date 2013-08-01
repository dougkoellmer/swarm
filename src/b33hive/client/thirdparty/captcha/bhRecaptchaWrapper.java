package com.b33hive.client.recaptcha;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;

public class bhRecaptchaWrapper
{
	private static bhRecaptchaWrapper s_instance;
	
	private bhRecaptchaWrapper()
	{
		
	}
	
	public static void startUp()
	{
		s_instance = new bhRecaptchaWrapper();
	}
	
	public static bhRecaptchaWrapper getInstance()
	{
		return s_instance;
	}
	
	public native String getResponse()
	/*-{
			return $wnd.Recaptcha.get_response();
	}-*/;
	
	public native String getChallenge()
	/*-{
			return $wnd.Recaptcha.get_challenge();
	}-*/;
	
	public native void loadNewImage()
	/*-{
			return $wnd.Recaptcha.reload("t");
	}-*/;
}
