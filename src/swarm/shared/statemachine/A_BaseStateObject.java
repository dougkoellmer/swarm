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
	
	protected static void enterState(A_State container, Class<? extends A_State> T)
	{
		enterState(container, T, null);
	}
	
	protected static void enterState(A_State container, Class<? extends A_State> T, StateArgs constructor)
	{
		((A_StateContainer) container).enterState_internal(T, constructor);
	}
	
	protected static void foregroundState(A_State container, Class<? extends A_State> T)
	{
		((A_StateContainer) container).foregroundState_internal(T);
	}
	
	protected static void backgroundState(A_State container, Class<? extends A_State> T)
	{
		((A_StateContainer) container).backgroundState_internal(T);
	}
	
	protected static void exitState(A_State container, Class<? extends A_State> T)
	{
		((A_StateContainer) container).exitState_internal(T);
	}
	
	protected <T extends A_StateMachine> T getMachine()
	{
		return (T) getClosestMachine(this);
	}
	
	private static A_State getClosestMachine(A_BaseStateObject stateObject)
	{
		if( stateObject instanceof A_Action )
		{
			A_State state = ((A_Action)stateObject).getState();
			
			if( state instanceof A_StateMachine )
			{
				return state;
			}
			else if( state.getParent() instanceof A_StateMachine )
			{
				return state.getParent();
			}
		}
		else if( stateObject instanceof A_StateMachine )
		{
			return (A_State) stateObject;
		}
		else if( stateObject instanceof A_State )
		{
			A_State parent = ((A_State)stateObject).getParent();
			
			if( parent instanceof A_StateMachine )
			{
				return parent;
			}
		}
		
		return null;
	}
	
	protected void pushVer(Class<? extends A_State> T)
	{
		this.pushVer(T, (StateArgs) null);
	}
	
	protected void pushVer(Class<? extends A_State> T, Object userData)
	{
		this.pushVer(T, createArgs(userData));
	}
	
	protected void pushVer(Class<? extends A_State> T, StateArgs constructor_nullable)
	{
		pushVer(getClosestMachine(this), T, constructor_nullable);
	}
	
	protected static void pushVer(A_State machine, Class<? extends A_State> T)
	{
		A_BaseStateObject.pushVer(machine, T, null);
	}
	
	protected static void pushVer(A_State machine, Class<? extends A_State> T, Object userData)
	{
		A_BaseStateObject.pushVer(machine, T, createArgs(userData));
	}
	
	protected static void pushVer(A_State machine, Class<? extends A_State> T, StateArgs constructor_nullable)
	{		
		((A_StateMachine) machine).pushVer_internal(T, constructor_nullable);
	}
	
	protected void popVer(Object ... args)
	{
		A_BaseStateObject.popVer(getClosestMachine(this), args);
	}
	
	protected static void popVer(A_State machine, Object ... args)
	{
		((A_StateMachine) machine).popState_internal(args);
	}
	
	
	
	
	protected static void pushHor(A_State machine)
	{
		
	}
	
	protected static void pushHor(A_State machine, int offset)
	{
		
	}
	
	protected static void pushHor(A_State machine, int offset, Object userData_nullable)
	{
		
	}
	
	protected static void pushHor(A_State machine, int offset, StateArgs constructor_nullable)
	{
		
	}
	
	protected static void pushHor(A_State machine, Class<? extends A_State> T)
	{
		A_BaseStateObject.pushHor(machine, T, null);
	}
	
	protected static void pushHor(A_State machine, Class<? extends A_State> T, Object userData)
	{
		A_BaseStateObject.pushHor(machine, T, createArgs(userData));
	}
	
	protected static void pushHor(A_State machine, Class<? extends A_State> T, StateArgs constructor_nullable)
	{		
		//((A_StateMachine) machine).pushHor_internal(T, constructor_nullable);
	}
	
	
	
	protected static void popHor(A_State machine)
	{
		
	}
	
	protected static void popHor(A_State machine, int offset)
	{
		
	}
	
	protected static void popHor(A_State machine, int offset, Object userData_nullable)
	{
		
	}
	
	protected static void popHor(A_State machine, int offset, StateArgs constructor_nullable)
	{
		
	}
	
	
	
	protected static int getHorIndex(A_State machine)
	{
		return -1;
	}
	
	protected static int getHorCount(A_State machine)
	{
		return -1;
	}
	
	protected static Class<? extends A_State> getHorState(A_State machine, int offset)
	{
		return null;
	}
	
	
	protected static void goHor(A_State machine, int offset)
	{
		
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
		A_BaseStateObject.setState(getClosestMachine(this), T, constructor_nullable);
	}
	
	
	protected static void setState(A_State machine, Class<? extends A_State> T)
	{
		A_BaseStateObject.setState(machine, T, (StateArgs) null);
	}
	
	protected static void setState(A_State machine, Class<? extends A_State> T, Object userData)
	{
		A_BaseStateObject.setState(machine, T, createArgs(userData));
	}
	
	protected static void setState(A_State machine, Class<? extends A_State> T, StateArgs constructor_nullable)
	{
		((A_StateMachine) machine).setState_internal(T, constructor_nullable);
	}
	
	
	
	
	
	protected static void peek(Class<? extends A_State> T, int offset)
	{
		
	}
	
	protected static int getQueueSize(Class<? extends A_State> T)
	{
		return -1;
	}
	
	protected static void enqueue(Class<? extends A_State> T)
	{
		
	}
	
	protected static Class<? extends A_State> dequeue()
	{
		return null;
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
