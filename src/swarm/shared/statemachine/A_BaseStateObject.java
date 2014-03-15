package swarm.shared.statemachine;


/**
 * This base class provides both actions and states with a unified API to safely & easily manipulate any part of the machine tree.
 * More runtime protections, mostly for debugging purposes, may be added to this class in the future.
 * 
 * @author Doug
 *
 */
public class A_BaseStateObject
{
	StateContext m_context;
	
	public StateContext getContext()
	{
		return m_context;
	}
	
	private static StateArgs createArgs(Object userData)
	{
		return userData != null ? new StateArgs() : null;
	}
	
	protected void enterState(A_State container, Class<? extends A_State> T)
	{
		this.enterState(container, T, null);
	}
	
	protected void enterState(A_State container, Class<? extends A_State> T, StateArgs constructor)
	{
		((A_StateContainer) container).enterState_internal(T, constructor);
	}
	
	protected void foregroundState(A_State container, Class<? extends A_State> T)
	{
		((A_StateContainer) container).foregroundState_internal(T);
	}
	
	protected void backgroundState(A_State container, Class<? extends A_State> T)
	{
		((A_StateContainer) container).backgroundState_internal(T);
	}
	
	protected void container_exitState(A_State container, Class<? extends A_State> T)
	{
		((A_StateContainer) container).exitState_internal(T);
	}
	
	private A_State getMachineFromThis()
	{
		if( this instanceof A_Action )
		{
			A_State state = ((A_Action)this).getState();
			
			if( state instanceof A_StateMachine )
			{
				return state;
			}
			else if( state.getParent() instanceof A_StateMachine )
			{
				return state.getParent();
			}
		}
		else if( this instanceof A_StateMachine )
		{
			return (A_State) this;
		}
		else if( this instanceof A_State )
		{
			A_State parent = ((A_State)this).getParent();
			
			if( parent instanceof A_StateMachine )
			{
				return parent;
			}
		}
		
		return null;
	}
	
	protected void pushState(Class<? extends A_State> T)
	{
		this.pushState(T, (StateArgs) null);
	}
	
	protected void pushState(Class<? extends A_State> T, Object userData)
	{
		this.pushState(T, createArgs(userData));
	}
	
	protected void pushState(Class<? extends A_State> T, StateArgs constructor_nullable)
	{
		this.pushState(this.getMachineFromThis(), T, constructor_nullable);
	}
	
	protected void pushState(A_State machine, Class<? extends A_State> T)
	{
		this.pushState(machine, T, null);
	}
	
	protected void pushState(A_State machine, Class<? extends A_State> T, Object userData)
	{
		this.pushState(machine, T, createArgs(userData));
	}
	
	protected void pushState(A_State machine, Class<? extends A_State> T, StateArgs constructor_nullable)
	{		
		((A_StateMachine) machine).pushState_internal(T, constructor_nullable);
	}
	
	
	protected void popState(Object ... args)
	{
		this.popState(this.getMachineFromThis(), args);
	}
	
	protected void popState(A_State machine, Object ... args)
	{
		((A_StateMachine) machine).popState_internal(args);
	}
	
	protected void setState(Class<? extends A_State> T)
	{
		this.setState(T, (StateArgs)null);
	}
	
	protected void setState(Class<? extends A_State> T, Object userData)
	{
		this.setState(T, createArgs(userData));
	}
	
	protected void setState(Class<? extends A_State> T, StateArgs constructor_nullable)
	{
		this.setState(this.getMachineFromThis(), T, constructor_nullable);
	}
	
	protected void setState(A_State machine, Class<? extends A_State> T)
	{
		this.setState(machine, T, null);
	}
	
	protected void setState(A_State machine, Class<? extends A_State> T, Object userData)
	{
		this.setState(machine, T, createArgs(userData));
	}
	
	protected void setState(A_State machine, Class<? extends A_State> T, StateArgs constructor_nullable)
	{
		((A_StateMachine) machine).setState_internal(T, constructor_nullable);
	}
	
	protected void beginBatchOperation()
	{
		m_context.beginBatch();
	}
	
	protected void endBatchOperation()
	{
		m_context.endBatch();
	}
}
