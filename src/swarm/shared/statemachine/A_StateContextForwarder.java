package swarm.shared.statemachine;

/**
 * 
 * @author dougkoellmer
 */
public abstract class A_StateContextForwarder
{
	protected static final StateFilter.Scope ALL			= StateFilter.ALL;
	protected static final StateFilter.Scope FIRST			= StateFilter.FIRST;
	protected static final StateFilter.Scope LAST			= StateFilter.LAST;	
	protected static final StateFilter.Target QUEUE			= ALL.QUEUE;
	protected static final StateFilter.Target HISTORY		= ALL.HISTORY;
	
	abstract StateContext getContext_internal();	
	
	static StateArgs defaultArgs(Object ... userData)
	{
		return userData != null && userData.length > 0 ? new StateArgs(userData) : StateArgs.DEFAULT;
	}
	
	static StateArgs defaultArgs(Object userData)
	{
		return userData != null ? new StateArgs(userData) : StateArgs.DEFAULT;
	}
	
	static StateArgs defaultArgs(StateArgs args_nullable)
	{
		return args_nullable != null ? args_nullable : StateArgs.DEFAULT;
	}
	
	
	
	public boolean isPerformable(Class<? extends A_Action_Base> T)
	{
		return this.isPerformable(T, defaultArgs((Object)null));
	}
	
	public boolean isPerformable(Class<? extends A_Action_Base> T, Object userData)
	{
		return this.isPerformable(T, defaultArgs(userData));
	}
	
	public boolean isPerformable(Class<? extends A_Action_Base> T, Object ... userData)
	{
		return this.isPerformable(T, defaultArgs(userData));
	}
	
	public boolean isPerformable(Class<? extends A_Action_Base> T, StateArgs args)
	{
		return getContext_internal().isPerformable(T, args);
	}

	
	
	public boolean perform(Class<? extends A_Action_Base> T)
	{
		return this.perform(T, (StateArgs)null);
	}
	
	public boolean perform(Class<? extends A_Action_Base> T, Object userData)
	{
		return this.perform(T, defaultArgs(userData));
	}
	
	public boolean perform(Class<? extends A_Action_Base> T, Object ... userData)
	{
		return this.perform(T, defaultArgs(userData));
	}
	
	public boolean perform(Class<? extends A_Action_Base> T, StateArgs args)
	{
		return getContext_internal().perform(T, args);
	}
	
	
	
	
	
	
	
	public <T extends A_State> T getForegrounded(Class<? extends A_State> T)
	{
		return getContext_internal().getForegrounded(T);
	}
	
	public <T extends A_State> T getForegrounded(Class<? extends A_State> ... T)
	{
		for( int i = 0; i < T.length; i++ )
		{
			A_State state = getForegrounded(T[i]);
			
			if( state != null )  return (T) state;
		}
		
		return null;
	}
	
	public boolean isForegrounded(Class<? extends A_State> T)
	{
		return this.getForegrounded(T) != null;
	}
	
	public boolean isForegrounded_any(Class<? extends A_State> ... stateClasses)
	{
		for( int i = 0; i < stateClasses.length; i++ )
		{
			if( isForegrounded(stateClasses[i]) )  return true;
		}
		
		return false;
	}
	
	public boolean isForegrounded_all(Class<? extends A_State> ... stateClasses)
	{
		if( stateClasses.length == 0 )  return false;
		
		for( int i = 0; i < stateClasses.length; i++ )
		{
			if( !isForegrounded(stateClasses[i]) )  return false;
		}
		
		return true;
	}
	
	
	public <T extends A_State> Class<? extends A_State> prev(Class<? extends A_StateMachine> machineClass)
	{
		A_StateMachine machine = get(machineClass);
		
		return machine.getPreviousState();
	}
	
	public <T extends A_State> T curr(Class<? extends A_StateMachine> machineClass)
	{
		A_StateMachine machine = get(machineClass);
		
		return machine.getCurrentState();
	}
	

	public <T extends A_State> T get(Class<? extends A_State> T)
	{
		return this.getEntered(T);
	}
	
	public <T extends A_State> T get(Class<? extends A_State> ... T)
	{
		return this.getEntered(T);
	}
	
	public <T extends A_State> T getEntered(Class<? extends A_State> ... T)
	{
		for( int i = 0; i < T.length; i++ )
		{
			T state = getEntered(T[i]);
			
			if( state != null )  return state;
		}
		
		return null;
	}
	
	public <T extends A_State> T getEntered(Class<? extends A_State> T)
	{
		return getContext_internal().getEntered(T);
	}
	
	public boolean isEntered(Class<? extends A_State> T)
	{
		return this.getEntered(T) != null;
	}
	
	public boolean isEntered_any(Class<? extends A_State> ... stateClasses)
	{
		for( int i = 0; i < stateClasses.length; i++ )
		{
			if( this.isEntered(stateClasses[i]) )  return true;
		}
		
		return false;
	}
	
	public boolean isEntered_all(Class<? extends A_State> ... stateClasses)
	{
		if( stateClasses.length == 0 )  return false;
		
		for( int i = 0; i < stateClasses.length; i++ )
		{
			if( !this.isEntered(stateClasses[i]) )  return false;
		}
		
		return true;
	}
	
	public Class<? extends A_State> get(Class<? extends A_StateMachine> machineClass, StateFilter.Target target, int offset)
	{
		return get((A_StateMachine)get(machineClass), target, offset);
	}
	public Class<? extends A_State> get(A_StateMachine machine, StateFilter.Target target, int offset)
	{
		if( machine == null )  return null;
		
		return machine.get(target, offset);
	}
}
