package swarm.shared.statemachine;


/**
 * This base class provides actions and states with unified API to safely & easily manipulate any part of the machine tree.
 * More runtime protections, mostly for debugging purposes, may be added to this class in the future.
 * 
 * @author dougkoellmer
 */
public abstract class A_BaseStateObject extends A_StateContextForwarder
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
		return (T) getClosestMachine(this);
	}
	
//	private static A_StateMachine getClosestMachine(A_BaseStateObject stateObject, StateFilter.Target target)
//	{
//		return getClosestMachine(stateObject, target, false);
//	}
	
	private static A_StateMachine getClosestMachine(A_BaseStateObject stateObject)
	{
		if( stateObject == null )  return null;
		
		if( stateObject instanceof A_Action_Base )
		{
			return getClosestMachine(((A_Action_Base) stateObject).getState());
		}
		else if( stateObject instanceof A_StateMachine )
		{
			A_StateMachine stateObject_cast = (A_StateMachine) stateObject;
			
			return stateObject_cast;
		}
		else
		{
			return getClosestMachine(((A_State)stateObject).getParent());
		}
	}
	
	
//	private static A_StateMachine getClosestMachine(A_BaseStateObject stateObject, StateFilter.Target target, boolean forPopV)
//	{
//		if( stateObject == null )  return null;
//		
//		if( stateObject instanceof A_Action_Base )
//		{
//			return getClosestMachine(((A_Action_Base) stateObject).getState(), target, forPopV);
//		}
//		else if( stateObject instanceof A_StateMachine )
//		{
//			A_StateMachine stateObject_cast = (A_StateMachine) stateObject;
//			
//			if( forPopV && (stateObject_cast.getCurrentState() == null || stateObject_cast.getCurrentState().getStateBeneath() == null) )
//			{
//				return getClosestMachine(((A_StateMachine) stateObject).getParent(), target, forPopV);
//			}
//			else if( target != null && !stateObject_cast.has(stateObject_cast, target))
//			{
//				return getClosestMachine(((A_StateMachine) stateObject).getParent(), target, forPopV);
//			}
//			else
//			{
//				return stateObject_cast;
//			}
//		}
//		else
//		{
//			return getClosestMachine(((A_State)stateObject).getParent(), target, forPopV);
//		}
//	}
	
	
	
	
	protected boolean pushV(Class<? extends A_State> stateClass)
	{
		return pushV(stateClass, defaultArgs((Object)null));
	}
	protected boolean pushV(Class<? extends A_State> stateClass, Object userData)
	{
		return pushV(stateClass, defaultArgs(userData));
	}
	protected boolean pushV(Class<? extends A_State> stateClass, Object ... userData)
	{
		return pushV(stateClass, defaultArgs(userData));
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
		return pushV((A_StateMachine)m_context.getEntered(machine), stateClass, defaultArgs(userData));
	}
	protected boolean pushV(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass, Object ... userData)
	{
		return pushV((A_StateMachine)m_context.getEntered(machine), stateClass, defaultArgs(userData));
	}
	protected boolean pushV(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return pushV((A_StateMachine)m_context.getEntered(machine), stateClass, constructor_nullable);
	}
	
	protected boolean pushV(A_StateMachine machine, Class<? extends A_State> T)
	{
		return pushV(machine, T, defaultArgs((Object)null));
	}
	protected boolean pushV(A_StateMachine machine, Class<? extends A_State> T, Object userData)
	{
		return pushV(machine, T, defaultArgs(userData));
	}
	protected boolean pushV(A_StateMachine machine, Class<? extends A_State> T, Object ... userData)
	{
		return pushV(machine, T, defaultArgs(userData));
	}
	protected boolean pushV(A_StateMachine machine, Class<? extends A_State> T, StateArgs constructor_nullable)
	{
		if( isLocked() )  return false;
		if( machine == null )  return false;
		
		return machine.pushV_internal(T, constructor_nullable);
	}
	
	
	
	
	
	protected boolean popV()
	{
		return popV(getClosestMachine(this));
	}
	protected boolean popV(Class<? extends A_StateMachine> machineClass)
	{
		return popV((A_StateMachine)m_context.getEntered(machineClass));
	}
	protected boolean popV(A_StateMachine machine)
	{
		return popV(machine, (StateArgs) null);
	}
	
	protected boolean popV(Object arg)
	{
		return popV(getClosestMachine(this), arg);
	}
	protected boolean popV(Class<? extends A_StateMachine> machineClass, Object arg)
	{
		return popV((A_StateMachine)m_context.getEntered(machineClass), arg);
	}
	protected boolean popV(A_StateMachine machine, Object arg)
	{
		return popV(machine, defaultArgs(arg));
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
		return popV(machine, defaultArgs(args));
	}
	
	
	protected boolean popV(StateArgs args)
	{
		return popV(getClosestMachine(this), args);
	}
	protected boolean popV(Class<? extends A_StateMachine> machineClass, StateArgs args)
	{
		return popV((A_StateMachine)m_context.getEntered(machineClass), args);
	}
	protected boolean popV(A_StateMachine machine, StateArgs args)
	{
		if( isLocked() )  return false;
		if( machine == null )  return false;
		
		return machine.popV_internal(args);
	}
	
	
	
	
	
	
	protected boolean push(Class<? extends A_State> stateClass)
	{
		return push(stateClass, defaultArgs((Object)null));
	}
	protected boolean push(Class<? extends A_State> stateClass, Object userData)
	{
		return push(stateClass, defaultArgs(userData));
	}
	protected boolean push(Class<? extends A_State> stateClass, Object ... userData)
	{
		return push(stateClass, defaultArgs(userData));
	}
	protected boolean push(Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return push(getClosestMachine(this), stateClass, constructor_nullable);
	}
	
	protected boolean push(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass)
	{
		return push((A_StateMachine)m_context.getEntered(machine), stateClass, defaultArgs((Object)null));
	}
	protected boolean push(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass, Object userData)
	{
		return push((A_StateMachine)m_context.getEntered(machine), stateClass, defaultArgs(userData));
	}
	protected boolean push(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass, Object ... userData)
	{
		return push((A_StateMachine)m_context.getEntered(machine), stateClass, defaultArgs(userData));
	}
	protected boolean push(Class<? extends A_StateMachine> machine, Class<? extends A_State> stateClass, StateArgs args_nullable)
	{
		return push((A_StateMachine)m_context.getEntered(machine), stateClass, args_nullable);
	}
	
	protected boolean push(A_StateMachine machine, Class<? extends A_State> stateClass)
	{
		return push(machine, stateClass, defaultArgs((Object)null));
	}
	protected boolean push(A_StateMachine machine, Class<? extends A_State> stateClass, Object userData)
	{
		return push(machine, stateClass, defaultArgs(userData));
	}
	protected boolean push(A_StateMachine machine, Class<? extends A_State> stateClass, Object ... userData)
	{
		return push(machine, stateClass, defaultArgs(userData));
	}
	protected boolean push(A_StateMachine machine, Class<? extends A_State> stateClass, StateArgs args_nullable)
	{
		if( isLocked() )  return false;
		if( machine == null )  return false;
		
		return machine.push_internal(stateClass, args_nullable);
	}
	
	
	
	
	
	protected boolean pop()
	{
		return pop(getClosestMachine(this));
	}
	protected boolean pop(Class<? extends A_StateMachine> machineClass)
	{
		return pop((A_StateMachine)m_context.getEntered(machineClass));
	}
	protected boolean pop(A_StateMachine machine)
	{
		return pop(machine, (StateArgs)null);
	}
	

	protected boolean pop(Object arg)
	{
		return pop(getClosestMachine(this), arg);
	}
	protected boolean pop(Class<? extends A_StateMachine> machineClass, Object arg)
	{
		return pop((A_StateMachine)m_context.getEntered(machineClass), arg);
	}
	protected boolean pop(A_StateMachine machine, Object arg)
	{
		return pop(machine, defaultArgs(arg));
	}
	
	
	protected boolean pop(Object ... args)
	{
		return pop(getClosestMachine(this), args);
	}
	protected boolean pop(Class<? extends A_StateMachine> machineClass, Object ... args)
	{
		return pop((A_StateMachine)m_context.getEntered(machineClass), args);
	}
	protected boolean pop(A_StateMachine machine, Object ... args)
	{
		return pop(machine, defaultArgs(args));
	}
	
	
	protected boolean pop(StateArgs args_nullable)
	{
		return pop(getClosestMachine(this), args_nullable);
	}
	protected boolean pop(Class<? extends A_StateMachine> machineClass, StateArgs args_nullable)
	{
		return pop((A_StateMachine)m_context.getEntered(machineClass), args_nullable);
	}
	protected boolean pop(A_StateMachine machine, StateArgs args_nullable)
	{
		if( isLocked() )  return false;
		if( machine == null )  return false;
		
		return machine.pop_internal(args_nullable);
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
		return go(machine, offset, (StateArgs) null);
	}
	
	protected boolean go(int offset, Object arg)
	{
		return go(getClosestMachine(this), offset, arg);
	}
	protected boolean go(Class<? extends A_StateMachine> machineClass, int offset, Object arg)
	{
		return go((A_StateMachine)m_context.getEntered(machineClass), offset, arg);
	}
	protected boolean go(A_StateMachine machine, int offset, Object arg)
	{
		return go(machine, offset, defaultArgs(arg));
	}
	
	protected boolean go(int offset, Object ... args)
	{
		return go(getClosestMachine(this), offset, args);
	}
	protected boolean go(Class<? extends A_StateMachine> machineClass, int offset, Object ... args)
	{
		return go((A_StateMachine)m_context.getEntered(machineClass), offset, args);
	}
	protected boolean go(A_StateMachine machine, int offset, Object ... args)
	{
		return go(machine, offset, defaultArgs(args));
	}
	
	
	protected boolean go(int offset, StateArgs args)
	{
		return go(getClosestMachine(this), offset, args);
	}
	protected boolean go(Class<? extends A_StateMachine> machineClass, int offset, StateArgs args)
	{
		return go((A_StateMachine)m_context.getEntered(machineClass), offset, args);
	}
	protected boolean go(A_StateMachine machine, int offset, StateArgs args)
	{
		if( isLocked() )  return false;
		if( machine == null )  return false;
		
		return machine.go_internal(offset, args);
	}
	
	
	
	protected boolean set(Class<? extends A_State> stateClass)
	{
		return set(stateClass, defaultArgs((Object)null));
	}
	protected boolean set(Class<? extends A_State> stateClass, Object userData)
	{
		return set(stateClass, defaultArgs(userData));
	}
	protected boolean set(Class<? extends A_State> stateClass, Object ... userData)
	{
		return set(stateClass, defaultArgs(userData));
	}
	protected boolean set(Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return set(getClosestMachine(this), stateClass, constructor_nullable);
	}
	
	protected boolean set(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass)
	{
		return set(machineClass, stateClass, defaultArgs((Object)null));
	}
	protected boolean set(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass, Object userData)
	{
		return set(machineClass, stateClass, defaultArgs(userData));
	}
	protected boolean set(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass, Object ... userData)
	{
		return set(machineClass, stateClass, defaultArgs(userData));
	}
	protected boolean set(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		return set((A_StateMachine)m_context.getEntered(machineClass), stateClass, constructor_nullable);
	}
	
	protected boolean set(A_StateMachine machine, Class<? extends A_State> stateClass)
	{
		return set(machine, stateClass, defaultArgs((Object)null));
	}
	protected boolean set(A_StateMachine machine, Class<? extends A_State> stateClass, Object userData)
	{
		return set(machine, stateClass, defaultArgs(userData));
	}
	protected boolean set(A_StateMachine machine, Class<? extends A_State> stateClass, Object ... userData)
	{
		return set(machine, stateClass, defaultArgs(userData));
	}
	protected boolean set(A_StateMachine machine, Class<? extends A_State> stateClass, StateArgs constructor_nullable)
	{
		if( isLocked() )  return false;
		if( machine == null )  return false;
		
		return machine.set_internal(stateClass, constructor_nullable);
	}
	
	


	
	
	protected boolean queue(Class<? extends A_State> stateClass)
	{
		return queue(stateClass, (StateArgs)null);
	}
	protected boolean queue(Class<? extends A_State> stateClass, Object userData)
	{
		return queue(stateClass, defaultArgs(userData));
	}
	protected boolean queue(Class<? extends A_State> stateClass, Object ... userData)
	{
		return queue(stateClass, defaultArgs(userData));
	}
	protected boolean queue(Class<? extends A_State> stateClass, StateArgs args_nullable)
	{
		return queue(getClosestMachine(this), stateClass, defaultArgs(args_nullable));
	}
	
	protected boolean queue(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass)
	{
		return queue(machineClass, stateClass, (StateArgs) null);
	}
	protected boolean queue(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass, Object userData)
	{
		return queue(machineClass, stateClass, defaultArgs(userData));
	}
	protected boolean queue(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass, Object ... userData)
	{
		return queue(machineClass, stateClass, defaultArgs(userData));
	}
	protected boolean queue(Class<? extends A_StateMachine> machineClass, Class<? extends A_State> stateClass, StateArgs args_nullable)
	{
		return queue((A_StateMachine)get(machineClass), stateClass, defaultArgs(args_nullable));
	}
	
	protected boolean queue(A_StateMachine machine, Class<? extends A_State> stateClass)
	{
		return queue(machine, stateClass, (StateArgs) null);
	}
	protected boolean queue(A_StateMachine machine, Class<? extends A_State> stateClass, Object userData)
	{
		return queue(machine, stateClass, defaultArgs(userData));
	}
	protected boolean queue(A_StateMachine machine, Class<? extends A_State> stateClass, Object ... userData)
	{
		return queue(machine, stateClass, defaultArgs(userData));
	}
	protected boolean queue(A_StateMachine machine, Class<? extends A_State> stateClass, StateArgs args_nullable)
	{
		if( isLocked() )  return false;
		if( machine == null )  return false;
		
		return machine.queue_internal(stateClass, defaultArgs(args_nullable));
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
		if( machine == null )  return false;
		
		return machine.dequeue_internal();
	}
	
	
	
	
	
	protected boolean remove(StateFilter.Match match, Object ... argValues)
	{
		return remove(match, null, argValues);
	}
	protected boolean remove(StateFilter.Match match, Class<? extends Object> stateClass, Object ... argValues)
	{
		return remove(getClosestMachine(this), match, stateClass, argValues);
	}
	
	protected boolean remove(Class<? extends A_StateMachine> machineClass, StateFilter.Match match, Object ... argValues)
	{
		return remove((A_StateMachine)get(machineClass), match, null, argValues);
	}
	protected boolean remove(Class<? extends A_StateMachine> machineClass, StateFilter.Match match, Class<? extends Object> stateClass, Object ... argValues)
	{
		return remove((A_StateMachine)get(machineClass), match, stateClass, argValues);
	}
	
	protected boolean remove(A_StateMachine machine, StateFilter.Match match, Object ... argValues)
	{
		return remove(machine, match, null, argValues);
	}
	protected boolean remove(A_StateMachine machine, StateFilter.Match match, Class<? extends Object> stateClass, Object ... argValues)
	{
		if( isLocked() )  return false;
		if( machine == null )  return false;
		
		return machine.remove_internal(/*justChecking=*/false, match, stateClass, argValues);
	}
	
	
	
	protected boolean remove(StateFilter.Target target)
	{
		return remove(getClosestMachine(this), target);
	}
	protected boolean remove(Class<? extends A_StateMachine> machineClass, StateFilter.Target target)
	{
		return remove((A_StateMachine)get(machineClass), target);
	}
	protected boolean remove(A_StateMachine machine, StateFilter.Target target)
	{
		return remove(machine, target, null);
	}
	
	protected boolean remove(StateFilter.Target target, Class<? extends Object> stateClass)
	{
		return remove(getClosestMachine(this), target, stateClass);
	}
	protected boolean remove(Class<? extends A_StateMachine> machineClass, StateFilter.Target target, Class<? extends Object> stateClass)
	{
		return remove((A_StateMachine)get(machineClass), target, stateClass);
	}
	protected boolean remove(A_StateMachine machine, StateFilter.Target target, Class<? extends Object> stateClass)
	{
		return remove(machine, target, stateClass, (StateArgs)null);
	}
	
	protected boolean remove(StateFilter.Target target, Class<? extends Object> stateClass, Object ... argValues)
	{
		return remove(getClosestMachine(this), target, stateClass, defaultArgs(argValues));
	}
	protected boolean remove(Class<? extends A_StateMachine> machineClass, StateFilter.Target target, Class<? extends Object> stateClass, Object ... argValues)
	{
		return remove((A_StateMachine)get(machineClass), target, stateClass, defaultArgs(argValues));
	}
	protected boolean remove(A_StateMachine machine, StateFilter.Target target, Class<? extends Object> stateClass, Object ... argValues)
	{
		return remove(machine, target, stateClass, defaultArgs(argValues));
	}
	
	
	protected boolean remove(StateFilter.Target target, Class<? extends Object> stateClass, StateArgs args)
	{
		return remove(getClosestMachine(this), target, stateClass, args);
	}
	protected boolean remove(Class<? extends A_StateMachine> machineClass, StateFilter.Target target, Class<? extends Object> stateClass, StateArgs args)
	{
		return remove((A_StateMachine)get(machineClass), target, stateClass, args);
	}
	protected boolean remove(A_StateMachine machine, StateFilter.Target target, Class<? extends Object> stateClass, StateArgs args)
	{
		if( isLocked() )  return false;
		if( machine == null )  return false;
		
		return machine.remove_internal(/*justChecking=*/false, target, stateClass, args);
	}
	
	
	
	
	
	
	protected boolean has(StateFilter.Match match, Object ... argValues)
	{
		return has(match, null, argValues);
	}
	protected boolean has(StateFilter.Match match, Class<? extends Object> stateClass, Object ... argValues)
	{
		return has(getClosestMachine(this), match, stateClass, argValues);
	}
	
	protected boolean has(Class<? extends A_StateMachine> machineClass, StateFilter.Match match, Object ... argValues)
	{
		return has((A_StateMachine)get(machineClass), match, null, argValues);
	}
	protected boolean has(Class<? extends A_StateMachine> machineClass, StateFilter.Match match, Class<? extends Object> stateClass, Object ... argValues)
	{
		return has((A_StateMachine)get(machineClass), match, stateClass, argValues);
	}
	
	protected boolean has(A_StateMachine machine, StateFilter.Match match, Object ... argValues)
	{
		return has(machine, match, null, argValues);
	}
	protected boolean has(A_StateMachine machine, StateFilter.Match match, Class<? extends Object> stateClass, Object ... argValues)
	{
		if( isLocked() )  return false;
		if( machine == null )  return false;
		
		return machine.remove_internal(/*justChecking=*/true, match, stateClass, argValues);
	}
	
	
	
	protected boolean has(StateFilter.Target target)
	{
		return has(getClosestMachine(this), target);
	}
	protected boolean has(Class<? extends A_StateMachine> machineClass, StateFilter.Target target)
	{
		return has((A_StateMachine)get(machineClass), target);
	}
	protected boolean has(A_StateMachine machine, StateFilter.Target target)
	{
		return has(machine, target, null);
	}
	
	protected boolean has(StateFilter.Target target, Class<? extends Object> stateClass)
	{
		return has(getClosestMachine(this), target, stateClass);
	}
	protected boolean has(Class<? extends A_StateMachine> machineClass, StateFilter.Target target, Class<? extends Object> stateClass)
	{
		return has((A_StateMachine)get(machineClass), target, stateClass);
	}
	protected boolean has(A_StateMachine machine, StateFilter.Target target, Class<? extends Object> stateClass)
	{
		return has(machine, target, stateClass, null);
	}
	
	
	protected boolean has(StateFilter.Target target, Class<? extends Object> stateClass, StateArgs args)
	{
		return has(getClosestMachine(this), target, stateClass, args);
	}
	protected boolean has(Class<? extends A_StateMachine> machineClass, StateFilter.Target target, Class<? extends Object> stateClass, StateArgs args)
	{
		return has((A_StateMachine)get(machineClass), target, stateClass, args);
	}
	protected boolean has(A_StateMachine machine, StateFilter.Target target, Class<? extends Object> stateClass, StateArgs args)
	{
		if( isLocked() )  return false;
		if( machine == null )  return false;
		
		return machine.remove_internal(/*justChecking=*/true, target, stateClass, args);
	}
	
	
	
	
	
	public Class<? extends A_State> get(StateFilter.Target target, int offset)
	{
		return get(getClosestMachine(this), target, offset);
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
