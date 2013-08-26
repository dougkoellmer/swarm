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
	
	static final HashMap<Class<? extends smA_Action>, smA_Action> s_actionRegistry = new HashMap<Class<? extends smA_Action>, smA_Action>();
	
	smA_State m_state = null;
	
	static final ArrayList<smA_Action> s_actionStack = new ArrayList<smA_Action>();
	
	boolean m_isCancelled = false;
	
	protected smA_Action()
	{
	}
	
	private void cancel()
	{
		m_isCancelled = true;
	}

	public boolean isPerformableInBackground()
	{
		return false;
	}
	
	public boolean isCancelled()
	{
		return m_isCancelled;
	}
	
	public boolean suppressLog()
	{
		return false;
	}
	
	public static void cancel(Class<? extends smA_Action> T)
	{
		smA_Action registeredAction = s_actionRegistry.get(T);
		
		for( int i = s_actionStack.size()-1; i >= 0; i-- )
		{
			smA_Action action = s_actionStack.get(i);

			//--- DRK > TODO: GWT doesn't include fast dynamic type checking, so biting the bullet on a hack here...
			if( action == registeredAction )
			{
				action.cancel();
			}
		}
	}
	
	public static void register(smA_Action action)
	{
		s_actionRegistry.put(action.getClass(), action);
	}

	private static smA_State getEnteredState(Class<? extends smA_Action> T)
	{
		smA_Action action = getInstance(T);
		
		if( action != null )
		{
			Class<? extends smA_State> state_T = action.getStateAssociation();
			
			smA_State state = smA_State.getEnteredInstance(state_T);
			
			return state;
		}
		
		return null;
	}
	
	public static boolean perform(Class<? extends smA_Action> T)
	{
		return perform(T, null);
	}
	
	public static boolean perform(Class<? extends smA_Action> T, smA_ActionArgs args)
	{
		smA_State state = getEnteredState(T);
		
		if( !isPerformable(state, T, args) )
		{
			//s_logger.info("Can't perform action "+ T.getName() +" statically.");

			return false;
		}
		else
		{
			return state.performAction(T, args);
		}
	}
	
	public static boolean isPerformable(Class<? extends smA_Action> T)
	{
		return isPerformable(T, null);
	}
	
	public static boolean isPerformable(Class<? extends smA_Action> T, smA_ActionArgs args)
	{
		smA_State state = getEnteredState(T);
		
		return isPerformable(state, T, args);
	}
	
	private static boolean isPerformable(smA_State state, Class<? extends smA_Action> T, smA_ActionArgs args)
	{
		if( state == null )
		{
			return false;
		}
		else
		{
			return state.isActionPerfomable(T, args);
		}
	}
	
	public void prePerform()
	{
		
	}

	public abstract void perform(smA_ActionArgs args);

	public abstract Class<? extends smA_State> getStateAssociation();

	public boolean isPerformable(smA_ActionArgs args)
	{
		return true;
	}

	public <T extends smA_State> T getState()
	{
		return (T) m_state;
	}

	static smA_Action getInstance(Class<? extends smA_Action> T)
	{
		smA_Action registeredAction = s_actionRegistry.get(T);
	
		if ( registeredAction != null )
		{
			if ( registeredAction.m_state != null )
			{
				smU_Debug.ASSERT(false, "Action reuse.");
			}
			
			return registeredAction;
		}
		
		return null;
	}
}