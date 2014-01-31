package swarm.shared.statemachine;

import swarm.shared.debugging.U_Debug;

/**
 * This base class provides both actions and states with a unified API to safely manipulate the machine tree.
 * More runtime protections, mostly for debugging purposes, may be added to this class in the future.
 * 
 * @author Doug
 *
 */
public class A_BaseStateObject extends Object
{
	StateContext m_context;
	
	public StateContext getContext()
	{
		return m_context;
	}
	
	protected void container_enterState(A_State thisArg, Class<? extends A_State> T)
	{
		this.container_enterState(thisArg, T, null);
	}
	
	protected void container_enterState(A_State thisArg, Class<? extends A_State> T, A_StateConstructor constructor)
	{
		U_Debug.ASSERT(thisArg instanceof A_StateContainer);
		
		((A_StateContainer) thisArg).internal_enterState(T, constructor);
	}
	
	protected void container_foregroundState(A_State thisArg, Class<? extends A_State> T)
	{
		U_Debug.ASSERT(thisArg instanceof A_StateContainer);
		
		((A_StateContainer) thisArg).internal_foregroundState(T);
	}
	
	protected void container_backgroundState(A_State thisArg, Class<? extends A_State> T)
	{
		U_Debug.ASSERT(thisArg instanceof A_StateContainer);
		
		((A_StateContainer) thisArg).internal_backgroundState(T);
	}
	
	protected void container_exitState(A_State thisArg, Class<? extends A_State> T)
	{
		U_Debug.ASSERT(thisArg instanceof A_StateContainer);
		
		((A_StateContainer) thisArg).internal_exitState(T);
	}
	
	protected void machine_pushState(A_State thisArg, Class<? extends A_State> T)
	{
		this.machine_pushState(thisArg, T, null);
	}
	
	protected void machine_pushState(A_State thisArg, Class<? extends A_State> T, A_StateConstructor constructor)
	{
		//smU_Debug.ASSERT(m_parent.checkLegalStateManipulation());
		
		U_Debug.ASSERT(thisArg instanceof A_StateMachine);
		
		((A_StateMachine) thisArg).pushState_internal(T, constructor);
	}
	
	protected void machine_popState(A_State thisArg, Object ... args)
	{
		//smU_Debug.ASSERT(this.checkLegalStateManipulation());
		
		U_Debug.ASSERT(thisArg instanceof A_StateMachine);
		
		((A_StateMachine) thisArg).popState_internal(args);
	}
	
	protected void machine_setState(A_State thisArg, Class<? extends A_State> T)
	{
		this.machine_setState(thisArg, T, null);
	}
	
	protected void machine_beginBatch(A_State thisArg)
	{
		thisArg.m_context.beginBatch();
	}
	
	protected void machine_endBatch(A_State thisArg)
	{
		thisArg.m_context.endBatch();
	}
	
	protected void machine_setState(A_State thisArg, Class<? extends A_State> T, A_StateConstructor constructor)
	{
		//smU_Debug.ASSERT(this.checkLegalStateManipulation());

		U_Debug.ASSERT(thisArg instanceof A_StateMachine);
		
		((A_StateMachine) thisArg).internal_setState(T, constructor);
	}
}
