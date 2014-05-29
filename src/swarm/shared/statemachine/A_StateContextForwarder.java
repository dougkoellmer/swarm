package swarm.shared.statemachine;

public abstract class A_StateContextForwarder
{
	abstract StateContext getContext_internal();
	
	
	static StateArgs createArgs(Object ... userData)
	{
		return userData != null ? new StateArgs(userData) : null;
	}
	
	static StateArgs createArgs(Object userData)
	{
		return userData != null ? new StateArgs(userData) : null;
	}
	
	
	
	public boolean isPerformable(Class<? extends A_Action> T)
	{
		return this.isPerformable(T, createArgs((Object)null));
	}
	
	public boolean isPerformable(Class<? extends A_Action> T, Object userData)
	{
		return this.isPerformable(T, createArgs(userData));
	}
	
	public boolean isPerformable(Class<? extends A_Action> T, Object ... userData)
	{
		return this.isPerformable(T, createArgs(userData));
	}
	
	public boolean isPerformable(Class<? extends A_Action> T, StateArgs args)
	{
		return getContext_internal().isPerformable(T, args);
	}

	
	
	public boolean perform(Class<? extends A_Action> T)
	{
		return this.perform(T, (StateArgs)null);
	}
	
	public boolean perform(Class<? extends A_Action> T, Object userData)
	{
		return this.perform(T, createArgs(userData));
	}
	
	public boolean perform(Class<? extends A_Action> T, Object ... userData)
	{
		return this.perform(T, createArgs(userData));
	}
	
	public boolean perform(Class<? extends A_Action> T, StateArgs args)
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
}
