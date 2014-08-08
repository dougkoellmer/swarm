package swarm.shared.statemachine;

public class StateReflectionFactory implements I_StateFactory
{
	@Override
	public A_State newState(Class<? extends A_State> stateClass)
	{
		try
		{
			return stateClass.newInstance();
		}
		catch (InstantiationException e)
		{
//			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
//			e.printStackTrace();
		}
		
		return null;
	}
	
	private static Class<? extends A_State> tryToGetState(String actionClassName, String stateType)
	{
		actionClassName = actionClassName.replace("Action", stateType);
		
		try
		{
			return (Class<? extends A_State>) Class.forName(actionClassName);
		}
		catch (ClassNotFoundException e)
		{
//			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public A_Action newAction(Class<? extends A_Action> actionClass)
	{
		A_Action action = null;
		
		try
		{
			 action = actionClass.newInstance();
			 
			 Class<?> enclosingClass = actionClass.getEnclosingClass();
			 
			 if( enclosingClass != null && A_State.class.isAssignableFrom(enclosingClass) )
			 {
				 action.m_association = (Class<? extends A_State>) enclosingClass;
			 }
			 else
			 {
				 String actionClassName = actionClass.getName();
				 String[] actionClassName_split = actionClassName.split("_");
				 actionClassName = "";
				 for( int i = 0; i < actionClassName_split.length-1; i++ )
				 {
					 actionClassName += actionClassName_split[i] + (i < actionClassName_split.length-2 ? "_" : "");
				 }
				 
				 actionClassName = actionClassName.replace("Event", "Action");
				 
				 action.m_association = tryToGetState(actionClassName, "State");
				 
				 if( action.m_association == null )
				 {
					 action.m_association = tryToGetState(actionClassName, "StateMachine");
				 }
			 }
		}
		catch (InstantiationException e)
		{
//			e.printStackTrace();
		}
		catch( IllegalAccessException e )
		{
//			e.printStackTrace();
		}
		
		return action;
	}
}
