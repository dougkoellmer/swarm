package swarm.shared.statemachine;


/**
 * This base class provides actions, states and results a unified API to safely & easily manipulate any part of the machine tree.
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
	
	boolean isLocked()
	{
		return false;
	}
	
	private StateOperationResult checkLock()
	{
		StateOperationResult result = null;
		
		if( isLocked() )
		{
			if( this instanceof StateOperationResult )
			{
				result = (StateOperationResult) this;
				result.succceeded(false);
			}
			else
			{
				result = m_context.checkOutResult(this, false);
			}
		}
		
		return result;
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
	

	
	
	
	static StateArgs createArgs(Object userData)
	{
		return userData != null ? new StateArgs(userData) : null;
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
		else if( stateObject instanceof StateOperationResult )
		{
			machine = getClosestMachine(((StateOperationResult)stateObject).getSource());
		}
		
		return (T) machine;
	}
	
	
	
	
	
	protected StateOperationResult pushV(Class<? extends A_State> stateClass)
	{
		return pushV(stateClass, (StateArgs) null);
	}
	protected StateOperationResult pushV(Class<? extends A_State> stateClass, Object userData)
	{
		return pushV(stateClass, createArgs(userData));
	}
	protected StateOperationResult pushV(Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return pushV(getClosestMachine(this), stateClass, constructor_nullable);
	}
	
	protected StateOperationResult pushV(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass)
	{
		return pushV((A_StateMachine)m_context.getEntered(machine), stateClass, (StateArgs) null);
	}
	protected StateOperationResult pushV(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass, Object userData)
	{
		return pushV((A_StateMachine)m_context.getEntered(machine), stateClass, createArgs(userData));
	}
	protected StateOperationResult pushV(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return pushV((A_StateMachine)m_context.getEntered(machine), stateClass, constructor_nullable);
	}
	
	protected StateOperationResult pushV(A_StateMachine machine, Class<? extends A_State> T)
	{
		return pushV(machine, T, (StateArgs) null);
	}
	protected StateOperationResult pushV(A_StateMachine machine, Class<? extends A_State> T, Object userData)
	{
		return pushV(machine, T, createArgs(userData));
	}
	protected StateOperationResult pushV(A_StateMachine machine, Class<? extends A_State> T, StateArgs constructor_nullable)
	{
		StateOperationResult result = checkLock();
		
		if( result != null )  return result;
		
		return ((A_StateMachine) machine).pushV_internal(T, constructor_nullable);
	}
	
	
	
	protected StateOperationResult popV(Object ... args)
	{
		return popV(getClosestMachine(this), args);
	}
	protected StateOperationResult popV(Class<? extends A_StateMachine> machineClass, Object ... args)
	{
		return popV((A_StateMachine)m_context.getEntered(machineClass), args);
	}
	protected StateOperationResult popV(A_StateMachine machine, Object ... args)
	{
		StateOperationResult result = checkLock();
		
		if( result != null )  return result;
		
		return ((A_StateMachine) machine).popV_internal(args);
	}
	
	
	
	
	protected StateOperationResult push(Class<? extends A_State> stateClass)
	{
		return push(stateClass, (StateArgs) null);
	}
	protected StateOperationResult push(Class<? extends A_State> stateClass, Object userData)
	{
		return push(stateClass, createArgs(userData));
	}
	protected StateOperationResult push(Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return push(getClosestMachine(this), stateClass, constructor_nullable);
	}
	
	protected StateOperationResult push(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass)
	{
		return push((A_StateMachine)m_context.getEntered(machine), stateClass, null);
	}
	protected StateOperationResult push(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass, Object userData)
	{
		return push((A_StateMachine)m_context.getEntered(machine), stateClass, createArgs(userData));
	}
	protected StateOperationResult push(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass, StateArgs args_nullable)
	{
		return push((A_StateMachine)m_context.getEntered(machine), stateClass, args_nullable);
	}
	
	protected StateOperationResult push(A_StateMachine machine, Class<? extends A_State> stateClass)
	{
		return push(machine, stateClass, null);
	}
	protected StateOperationResult push(A_StateMachine machine, Class<? extends A_State> stateClass, Object userData)
	{
		return push(machine, stateClass, createArgs(userData));
	}
	protected StateOperationResult push(A_StateMachine machine, Class<? extends A_State> stateClass, StateArgs args_nullable)
	{
		StateOperationResult result = checkLock();
		
		if( result != null )  return result;
		
		return ((A_StateMachine) machine).push_internal(stateClass, args_nullable);
	}
	
	
	
	protected StateOperationResult pop()
	{
		return pop(getClosestMachine(this));
	}
	protected StateOperationResult pop(Class<? extends A_StateMachine> machineClass)
	{
		return pop(m_context.getEntered(machineClass));
	}
	protected StateOperationResult pop(A_State machine)
	{
		StateOperationResult result = checkLock();
		
		if( result != null )  return result;
		
		return ((A_StateMachine) machine).pop_internal();
	}
	
	
	
	protected StateOperationResult go(int offset)
	{
		return go(getClosestMachine(this), offset);
	}
	protected StateOperationResult go(Class<? extends A_StateMachine> machineClass, int offset)
	{
		return go((A_StateMachine)m_context.getEntered(machineClass), offset);
	}
	protected StateOperationResult go(A_StateMachine machine, int offset)
	{
		StateOperationResult result = checkLock();
		
		if( result != null )  return result;
		
		return ((A_StateMachine) machine).go_internal(offset);
	}
	
	

	protected StateOperationResult clearHistory()
	{
		return clearHistory(getClosestMachine(this));
	}
	protected StateOperationResult clearHistory(Class<? extends A_StateMachine> machineClass)
	{
		return clearHistory((A_StateMachine)m_context.getEntered(machineClass));
	}
	protected StateOperationResult clearHistory(A_StateMachine machine)
	{
		StateOperationResult result = checkLock();
		
		if( result != null )  return result;
		
		return ((A_StateMachine) machine).clearHistory_internal();
	}
	
	
	
	protected StateOperationResult set(Class<? extends A_State> stateClass)
	{
		return set(stateClass, (StateArgs)null);
	}
	protected StateOperationResult set(Class<? extends A_State> stateClass, Object userData)
	{
		return set(stateClass, createArgs(userData));
	}
	protected StateOperationResult set(Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return set(getClosestMachine(this), stateClass, constructor_nullable);
	}
	
	protected StateOperationResult set(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass)
	{
		return set(machineClass, stateClass, (StateArgs) null);
	}
	protected StateOperationResult set(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass, Object userData)
	{
		return set(machineClass, stateClass, createArgs(userData));
	}
	protected StateOperationResult set(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return set((A_StateMachine)m_context.getEntered(machineClass), stateClass, constructor_nullable);
	}
	
	protected StateOperationResult set(A_StateMachine machine, Class<? extends A_State> stateClass)
	{
		return set(machine, stateClass, (StateArgs) null);
	}
	protected StateOperationResult set(A_StateMachine machine, Class<? extends A_State> stateClass, Object userData)
	{
		return set(machine, stateClass, createArgs(userData));
	}
	protected StateOperationResult set(A_StateMachine machine, Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		StateOperationResult result = checkLock();
		
		if( result != null )  return result;
		
		return machine.set_internal(stateClass, constructor_nullable);
	}
	
	


	
	
	protected StateOperationResult queue(Class<? extends A_State> stateClass)
	{
		return queue(stateClass, (StateArgs)null);
	}
	protected StateOperationResult queue(Class<? extends A_State> stateClass, Object userData)
	{
		return queue(stateClass, createArgs(userData));
	}
	protected StateOperationResult queue(Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return queue(getClosestMachine(this), stateClass, constructor_nullable);
	}
	
	protected StateOperationResult queue(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass)
	{
		return queue(machineClass, stateClass, (StateArgs) null);
	}
	protected StateOperationResult queue(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass, Object userData)
	{
		return set(machineClass, stateClass, createArgs(userData));
	}
	protected StateOperationResult queue(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return queue((A_StateMachine)m_context.getEntered(machineClass), stateClass, constructor_nullable);
	}
	
	protected StateOperationResult queue(A_StateMachine machine, Class<? extends A_State> stateClass)
	{
		return queue(machine, stateClass, (StateArgs) null);
	}
	protected StateOperationResult queue(A_StateMachine machine, Class<? extends A_State> stateClass, Object userData)
	{
		return queue(machine, stateClass, createArgs(userData));
	}
	protected StateOperationResult queue(A_StateMachine machine, Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		StateOperationResult result = checkLock();
		
		if( result != null )  return result;
		
		return machine.queue_internal(stateClass, constructor_nullable);
	}
	
	
	
	
	
	protected StateOperationResult dequeue()
	{
		return dequeue(getClosestMachine(this));
	}
	protected StateOperationResult dequeue(Class<? extends A_StateMachine> machineClass)
	{
		return dequeue((A_StateMachine)m_context.getEntered(machineClass));
	}
	protected StateOperationResult dequeue(A_StateMachine machine)
	{
		StateOperationResult result = checkLock();
		
		if( result != null )  return result;
		
		return ((A_StateMachine) machine).dequeue_internal();
	}
	
	
	
	
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

	
	protected StateOperationResult perform(Class<? extends A_Action> T)
	{
		return perform(T, (StateArgs)null);
	}
	
	protected StateOperationResult perform(Class<? extends A_Action> T, Object userData)
	{
		return perform(T, createArgs(userData));
	}
	
	protected StateOperationResult perform(Class<? extends A_Action> T, StateArgs args)
	{
		return m_context.checkOutResult(this, m_context.perform(T, args));
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
