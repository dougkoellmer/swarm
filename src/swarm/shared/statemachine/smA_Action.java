package swarm.shared.statemachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import swarm.shared.debugging.smU_Debug;


/**
 * ...
 * @author 
 */
public abstract class smA_Action extends smA_BaseStateObject
{
	private static final Logger s_logger = Logger.getLogger(smA_Action.class.getName());
	
	smA_State m_state = null;
	Class<? extends smA_State> m_association;
	
	protected smA_Action()
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
	
	public /*virtual*/ void prePerform()
	{
		
	}

	public abstract void perform(smA_ActionArgs args);

	public Class<? extends smA_State> getStateAssociation()
	{
		return m_association;
	}

	public boolean isPerformable(smA_ActionArgs args)
	{
		return true;
	}

	protected <T extends smA_State> T getState()
	{
		return (T) m_state;
	}
}