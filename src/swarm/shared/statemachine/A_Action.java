package swarm.shared.statemachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import swarm.shared.debugging.U_Debug;


/**
 * ...
 * @author 
 */
public abstract class A_Action extends A_BaseStateObject
{
	private static final Logger s_logger = Logger.getLogger(A_Action.class.getName());
	
	A_State m_state = null;
	Class<? extends A_State> m_association;
	
	protected A_Action()
	{
	}

	public boolean isPerformableInBackground()
	{
		return false;
	}
	
	public boolean suppressLog()
	{
		return false;
	}
	
	public /*virtual*/ void prePerform(A_ActionArgs args)
	{
		
	}

	public abstract void perform(A_ActionArgs args);

	public Class<? extends A_State> getStateAssociation()
	{
		return m_association;
	}

	public boolean isPerformable(A_ActionArgs args)
	{
		return true;
	}

	protected <T extends A_State> T getState()
	{
		return (T) m_state;
	}
}