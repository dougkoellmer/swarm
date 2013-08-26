package swarm.client.thirdparty.captcha;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;

public class smRecaptchaWrapper
{
	public smRecaptchaWrapper()
	{
		
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
