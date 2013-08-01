package com.b33hive.shared.statemachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.b33hive.shared.debugging.bhU_Debug;


/**
 * ...
 * @author 
 */
public abstract class bhA_Action extends bhA_BaseStateObject
{
	private static final Logger s_logger = Logger.getLogger(bhA_Action.class.getName());
	
	static final HashMap<Class<? extends bhA_Action>, bhA_Action> s_actionRegistry = new HashMap<Class<? extends bhA_Action>, bhA_Action>();
	
	bhA_State m_state = null;
	
	static final ArrayList<bhA_Action> s_actionStack = new ArrayList<bhA_Action>();
	
	boolean m_isCancelled = false;
	
	protected bhA_Action()
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
	
	public static void cancel(Class<? extends bhA_Action> T)
	{
		bhA_Action registeredAction = s_actionRegistry.get(T);
		
		for( int i = s_actionStack.size()-1; i >= 0; i-- )
		{
			bhA_Action action = s_actionStack.get(i);

			//--- DRK > TODO: GWT doesn't include fast dynamic type checking, so biting the bullet on a hack here...
			if( action == registeredAction )
			{
				action.cancel();
			}
		}
	}
	
	public static void register(bhA_Action action)
	{
		s_actionRegistry.put(action.getClass(), action);
	}

	private static bhA_State getEnteredState(Class<? extends bhA_Action> T)
	{
		bhA_Action action = getInstance(T);
		
		if( action != null )
		{
			Class<? extends bhA_State> state_T = action.getStateAssociation();
			
			bhA_State state = bhA_State.getEnteredInstance(state_T);
			
			return state;
		}
		
		return null;
	}
	
	public static boolean perform(Class<? extends bhA_Action> T)
	{
		return perform(T, null);
	}
	
	public static boolean perform(Class<? extends bhA_Action> T, bhA_ActionArgs args)
	{
		bhA_State state = getEnteredState(T);
		
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
	
	public static boolean isPerformable(Class<? extends bhA_Action> T)
	{
		return isPerformable(T, null);
	}
	
	public static boolean isPerformable(Class<? extends bhA_Action> T, bhA_ActionArgs args)
	{
		bhA_State state = getEnteredState(T);
		
		return isPerformable(state, T, args);
	}
	
	private static boolean isPerformable(bhA_State state, Class<? extends bhA_Action> T, bhA_ActionArgs args)
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

	public abstract void perform(bhA_ActionArgs args);

	public abstract Class<? extends bhA_State> getStateAssociation();

	public boolean isPerformable(bhA_ActionArgs args)
	{
		return true;
	}

	public <T extends bhA_State> T getState()
	{
		return (T) m_state;
	}

	static bhA_Action getInstance(Class<? extends bhA_Action> T)
	{
		bhA_Action registeredAction = s_actionRegistry.get(T);
	
		if ( registeredAction != null )
		{
			if ( registeredAction.m_state != null )
			{
				bhU_Debug.ASSERT(false, "Action reuse.");
			}
			
			return registeredAction;
		}
		
		return null;
	}
}