package swarm.shared.statemachine;


/**
 * This base class provides actions, states and results a unified API to safely & easily manipulate any part of the machine tree.
 * More runtime protections, mostly for debugging purposes, may be added to this class in the future.
 * 
 * @author Doug
 *
 */
public class A_BaseStateObject extends A_StateContextProxy
{
	StateContext m_context;
	
	public StateContext getContext()
	{
		return m_context;
	}
	
	@Override StateContext getContext_internal()
	{
		return m_context;
	}
	
	public <T extends Object> T cast()
	{
		return (T) this;
	}
	
	boolean isLocked()
	{
		return false;
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
		return getClosestMachine(this);
	}
	
	private static <T extends A_StateMachine> T getClosestMachine(A_BaseStateObject stateObject)
	{
		A_BaseStateObject machine = null;
		
		if( stateObject instanceof A_Action )
		{
			A_State state = ((A_Action)stateObject).getState();
			
			if( state instanceof A_StateMachine )
			{
				machine = state;
			}
			else if( state.getParent() instanceof A_StateMachine )
			{
				machine = state.getParent();
			}
		}
		else if( stateObject instanceof A_StateMachine )
		{
			machine = stateObject;
		}
		else if( stateObject instanceof A_State )
		{
			A_State parent = ((A_State)stateObject).getParent();
			
			if( parent instanceof A_StateMachine )
			{
				machine = parent;
			}
		}
		
		return (T) machine;
	}
	
	
	
	
	
	protected boolean pushV(Class<? extends A_State> stateClass)
	{
		return pushV(stateClass, createArgs((Object)null));
	}
	protected boolean pushV(Class<? extends A_State> stateClass, Object userData)
	{
		return pushV(stateClass, createArgs(userData));
	}
	protected boolean pushV(Class<? extends A_State> stateClass, Object ... userData)
	{
		return pushV(stateClass, createArgs(userData));
	}
	protected boolean pushV(Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return pushV(getClosestMachine(this), stateClass, constructor_nullable);
	}
	
	protected boolean pushV(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass)
	{
		return pushV((A_StateMachine)m_context.getEntered(machine), stateClass, (StateArgs) null);
	}
	protected boolean pushV(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass, Object userData)
	{
		return pushV((A_StateMachine)m_context.getEntered(machine), stateClass, createArgs(userData));
	}
	protected boolean pushV(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass, Object ... userData)
	{
		return pushV((A_StateMachine)m_context.getEntered(machine), stateClass, createArgs(userData));
	}
	protected boolean pushV(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return pushV((A_StateMachine)m_context.getEntered(machine), stateClass, constructor_nullable);
	}
	
	protected boolean pushV(A_StateMachine machine, Class<? extends A_State> T)
	{
		return pushV(machine, T, createArgs((Object)null));
	}
	protected boolean pushV(A_StateMachine machine, Class<? extends A_State> T, Object userData)
	{
		return pushV(machine, T, createArgs(userData));
	}
	protected boolean pushV(A_StateMachine machine, Class<? extends A_State> T, Object ... userData)
	{
		return pushV(machine, T, createArgs(userData));
	}
	protected boolean pushV(A_StateMachine machine, Class<? extends A_State> T, StateArgs constructor_nullable)
	{
		if( isLocked() )  return false;
		
		return ((A_StateMachine) machine).pushV_internal(T, constructor_nullable);
	}
	
	
	
	protected boolean popV(Object ... args)
	{
		return popV(getClosestMachine(this), args);
	}
	protected boolean popV(Class<? extends A_StateMachine> machineClass, Object ... args)
	{
		return popV((A_StateMachine)m_context.getEntered(machineClass), args);
	}
	protected boolean popV(A_StateMachine machine, Object ... args)
	{
		if( isLocked() )  return false;
		
		return ((A_StateMachine) machine).popV_internal(args);
	}
	
	
	
	
	protected boolean push(Class<? extends A_State> stateClass)
	{
		return push(stateClass, createArgs((Object)null));
	}
	protected boolean push(Class<? extends A_State> stateClass, Object userData)
	{
		return push(stateClass, createArgs(userData));
	}
	protected boolean push(Class<? extends A_State> stateClass, Object ... userData)
	{
		return push(stateClass, createArgs(userData));
	}
	protected boolean push(Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return push(getClosestMachine(this), stateClass, constructor_nullable);
	}
	
	protected boolean push(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass)
	{
		return push((A_StateMachine)m_context.getEntered(machine), stateClass, createArgs((Object)null));
	}
	protected boolean push(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass, Object userData)
	{
		return push((A_StateMachine)m_context.getEntered(machine), stateClass, createArgs(userData));
	}
	protected boolean push(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass, Object ... userData)
	{
		return push((A_StateMachine)m_context.getEntered(machine), stateClass, createArgs(userData));
	}
	protected boolean push(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass, StateArgs args_nullable)
	{
		return push((A_StateMachine)m_context.getEntered(machine), stateClass, args_nullable);
	}
	
	protected boolean push(A_StateMachine machine, Class<? extends A_State> stateClass)
	{
		return push(machine, stateClass, createArgs((Object)null));
	}
	protected boolean push(A_StateMachine machine, Class<? extends A_State> stateClass, Object userData)
	{
		return push(machine, stateClass, createArgs(userData));
	}
	protected boolean push(A_StateMachine machine, Class<? extends A_State> stateClass, Object ... userData)
	{
		return push(machine, stateClass, createArgs(userData));
	}
	protected boolean push(A_StateMachine machine, Class<? extends A_State> stateClass, StateArgs args_nullable)
	{
		if( isLocked() )  return false;
		
		return ((A_StateMachine) machine).push_internal(stateClass, args_nullable);
	}
	
	
	
	protected boolean pop()
	{
		return pop(getClosestMachine(this));
	}
	protected boolean pop(Class<? extends A_StateMachine> machineClass)
	{
		return pop(m_context.getEntered(machineClass));
	}
	protected boolean pop(A_State machine)
	{
		if( isLocked() )  return false;
		
		return ((A_StateMachine) machine).pop_internal();
	}
	
	
	
	protected boolean go(int offset)
	{
		return go(getClosestMachine(this), offset);
	}
	protected boolean go(Class<? extends A_StateMachine> machineClass, int offset)
	{
		return go((A_StateMachine)m_context.getEntered(machineClass), offset);
	}
	protected boolean go(A_StateMachine machine, int offset)
	{
		if( isLocked() )  return false;
		
		return ((A_StateMachine) machine).go_internal(offset);
	}
	
	

	protected boolean clearHistory()
	{
		return clearHistory(getClosestMachine(this));
	}
	protected boolean clearHistory(Class<? extends A_StateMachine> machineClass)
	{
		return clearHistory((A_StateMachine)m_context.getEntered(machineClass));
	}
	protected boolean clearHistory(A_StateMachine machine)
	{
		if( isLocked() )  return false;
		
		return ((A_StateMachine) machine).clearHistory_internal();
	}
	
	
	
	protected boolean set(Class<? extends A_State> stateClass)
	{
		return set(stateClass, createArgs((Object)null));
	}
	protected boolean set(Class<? extends A_State> stateClass, Object userData)
	{
		return set(stateClass, createArgs(userData));
	}
	protected boolean set(Class<? extends A_State> stateClass, Object ... userData)
	{
		return set(stateClass, createArgs(userData));
	}
	protected boolean set(Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return set(getClosestMachine(this), stateClass, constructor_nullable);
	}
	
	protected boolean set(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass)
	{
		return set(machineClass, stateClass, createArgs((Object)null));
	}
	protected boolean set(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass, Object userData)
	{
		return set(machineClass, stateClass, createArgs(userData));
	}
	protected boolean set(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass, Object ... userData)
	{
		return set(machineClass, stateClass, createArgs(userData));
	}
	protected boolean set(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return set((A_StateMachine)m_context.getEntered(machineClass), stateClass, constructor_nullable);
	}
	
	protected boolean set(A_StateMachine machine, Class<? extends A_State> stateClass)
	{
		return set(machine, stateClass, createArgs((Object)null));
	}
	protected boolean set(A_StateMachine machine, Class<? extends A_State> stateClass, Object userData)
	{
		return set(machine, stateClass, createArgs(userData));
	}
	protected boolean set(A_StateMachine machine, Class<? extends A_State> stateClass, Object ... userData)
	{
		return set(machine, stateClass, createArgs(userData));
	}
	protected boolean set(A_StateMachine machine, Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		if( isLocked() )  return false;
		
		return machine.set_internal(stateClass, constructor_nullable);
	}
	
	


	
	
	protected boolean queue(Class<? extends A_State> stateClass)
	{
		return queue(stateClass, (StateArgs)null);
	}
	protected boolean queue(Class<? extends A_State> stateClass, Object userData)
	{
		return queue(stateClass, createArgs(userData));
	}
	protected boolean queue(Class<? extends A_State> stateClass, Object ... userData)
	{
		return queue(stateClass, createArgs(userData));
	}
	protected boolean queue(Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return queue(getClosestMachine(this), stateClass, constructor_nullable);
	}
	
	protected boolean queue(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass)
	{
		return queue(machineClass, stateClass, (StateArgs) null);
	}
	protected boolean queue(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass, Object userData)
	{
		return set(machineClass, stateClass, createArgs(userData));
	}
	protected boolean queue(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass, Object ... userData)
	{
		return set(machineClass, stateClass, createArgs(userData));
	}
	protected boolean queue(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return queue((A_StateMachine)m_context.getEntered(machineClass), stateClass, constructor_nullable);
	}
	
	protected boolean queue(A_StateMachine machine, Class<? extends A_State> stateClass)
	{
		return queue(machine, stateClass, (StateArgs) null);
	}
	protected boolean queue(A_StateMachine machine, Class<? extends A_State> stateClass, Object userData)
	{
		return queue(machine, stateClass, createArgs(userData));
	}
	protected boolean queue(A_StateMachine machine, Class<? extends A_State> stateClass, Object ... userData)
	{
		return queue(machine, stateClass, createArgs(userData));
	}
	protected boolean queue(A_StateMachine machine, Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		if( isLocked() )  return false;
		
		return machine.queue_internal(stateClass, constructor_nullable);
	}
	
	
	
	
	
	protected boolean dequeue()
	{
		return dequeue(getClosestMachine(this));
	}
	protected boolean dequeue(Class<? extends A_StateMachine> machineClass)
	{
		return dequeue((A_StateMachine)m_context.getEntered(machineClass));
	}
	protected boolean dequeue(A_StateMachine machine)
	{
		if( isLocked() )  return false;
		
		return ((A_StateMachine) machine).dequeue_internal();
	}
	
	
	
//	
//	protected boolean removeFromQueue(Class<? extends A_State> stateClass)
//	{
//		return removeFromQueue(getClosestMachine(this), stateClass);
//	}
//	protected boolean removeFromQueue(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass)
//	{
//		return removeFromQueue((A_StateMachine)m_context.getEntered(machineClass), stateClass);
//	}
//	protected boolean removeFromQueue(A_StateMachine machine, Class<? extends A_State> stateClass)
//	{
//		if( isLocked() )  return false;
//		
//		return ((A_StateMachine) machine).removeFromQueue_internal(stateClass);
//	}
	
	
	
	
//	protected static int getIndexH(A_State machine)
//	{
//		return -1;
//	}
//	
//	protected static int getCountH(A_State machine)
//	{
//		return -1;
//	}
//	
//	protected static Class<? extends A_State> getHorState(A_State machine, int offset)
//	{
//		return null;
//	}
//	
//	protected void peek(Class<? extends A_State> T, int offset)
//	{
//		
//	}
//	
//	protected int getQueueSize(Class<? extends A_State> T)
//	{
//		return -1;
//	}
	
	
	
	
	
	
	
	
	protected void beginBatchOperation()
	{
		m_context.beginBatch();
	}
	
	protected void endBatchOperation()
	{
		m_context.endBatch();
	}
}
