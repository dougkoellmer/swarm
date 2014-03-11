package swarm.shared.statemachine;

import swarm.shared.debugging.U_Debug;

/**
 * This base class provides both actions and states with a unified API to safely manipulate any part of the machine tree.
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
	
	protected void enterState(A_State container, Class<? extends A_State> T)
	{
		this.enterState(container, T, null);
	}
	
	protected void enterState(A_State container, Class<? extends A_State> T, A_StateConstructor constructor)
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
	
	protected void pushState(A_State machine, Class<? extends A_State> T)
	{
		this.pushState(machine, T, null);
	}
	
	protected void pushState(A_State machine, Class<? extends A_State> T, A_StateConstructor constructor)
	{		
		((A_StateMachine) machine).pushState_internal(T, constructor);
	}
	
	protected void popState(A_State machine, Object ... args)
	{
		((A_StateMachine) machine).popState_internal(args);
	}
	
	protected void setState(A_State machine, Class<? extends A_State> T)
	{
		this.setState(machine, T, null);
	}
	
	protected void setState(A_State machine, Class<? extends A_State> T, A_StateConstructor constructor)
	{
		((A_StateMachine) machine).setState_internal(T, constructor);
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
