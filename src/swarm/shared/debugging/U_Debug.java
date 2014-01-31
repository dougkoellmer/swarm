package swarm.shared.debugging;


public final class U_Debug
{
	private static I_AssertionDelegate s_delegate;
	
	private U_Debug()
	{
		
	}
	
	public static void setDelegate(I_AssertionDelegate delegate)
	{
		s_delegate = delegate;
	}
	
	/**
	 * This method exists because the built-in java assert is fucking useless in GWT...they're pretty silent in dev mode,
	 * and really fickle in compiled mode (sometimes they throw, sometimes not)...and either way, you never break into the call stack.
	 * With MY assert, you get an alert in the browser, which will give you a chance to place a breakpoint here.
	 * You are highly encouraged to use a unique message so that in compiled mode you have some chance in hell of knowing
	 *	which assert was triggered...otherwise, you probably won't be able to figure it out.
	 */
	public static void ASSERT(boolean condition, String message)
	{
		if( !condition )
		{
			s_delegate.doAssert(message);
		}
	}
	
	public static void ASSERT(boolean condition)
	{
		U_Debug.ASSERT(condition, "");
	}
}