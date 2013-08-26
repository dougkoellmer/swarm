package swarm.shared.statemachine;

import swarm.shared.debugging.smU_Debug;

/**
 * This base class provides both actions and states with a unified API to safely manipulate the machine tree.
 * More runtime protections, mostly for debugging purposes, will be added to this class in the future.
 * 
 * @author Doug
 *
 */
public class smA_BaseStateObject extends Object
{
	protected void container_enterState(smA_State thisArg, Class<? extends smA_State> T)
	{
		this.container_enterState(thisArg, T, null);
	}
	
	protected void container_enterState(smA_State thisArg, Class<? extends smA_State> T, smA_StateConstructor constructor)
	{
		bhU_Debug.ASSERT(thisArg instanceof smA_StateContainer);
		
		((smA_StateContainer) thisArg).internal_enterState(T, constructor);
	}
	
	protected void container_foregroundState(smA_State thisArg, Class<? extends smA_State> T)
	{
		bhU_Debug.ASSERT(thisArg instanceof smA_StateContainer);
		
		((smA_StateContainer) thisArg).internal_foregroundState(T);
	}
	
	protected void container_backgroundState(smA_State thisArg, Class<? extends smA_State> T)
	{
		bhU_Debug.ASSERT(thisArg instanceof smA_StateContainer);
		
		((smA_StateContainer) thisArg).internal_backgroundState(T);
	}
	
	protected void container_exitState(smA_State thisArg, Class<? extends smA_State> T)
	{
		bhU_Debug.ASSERT(thisArg instanceof smA_StateContainer);
		
		((smA_StateContainer) thisArg).internal_exitState(T);
	}
	
	protected void machine_pushState(smA_State thisArg, Class<? extends smA_State> T)
	{
		this.machine_pushState(thisArg, T, null);
	}
	
	protected void machine_pushState(smA_State thisArg, Class<? extends smA_State> T, smA_StateConstructor constructor)
	{
		//bhU_Debug.ASSERT(m_parent.checkLegalStateManipulation());
		
		bhU_Debug.ASSERT(thisArg instanceof smA_StateMachine);
		
		((smA_StateMachine) thisArg).pushState_internal(T, constructor);
	}
	
	protected void machine_popState(smA_State thisArg, Object ... args)
	{
		//bhU_Debug.ASSERT(this.checkLegalStateManipulation());
		
		bhU_Debug.ASSERT(thisArg instanceof smA_StateMachine);
		
		((smA_StateMachine) thisArg).popState_internal(args);
	}
	
	protected void machine_setState(smA_State thisArg, Class<? extends smA_State> T)
	{
		this.machine_setState(thisArg, T, null);
	}
	
	protected void machine_beginBatch(smA_State thisArg)
	{
		thisArg.m_root.beginBatch();
	}
	
	protected void machine_endBatch(smA_State thisArg)
	{
		thisArg.m_root.endBatch();
	}
	
	protected void machine_setState(smA_State thisArg, Class<? extends smA_State> T, smA_StateConstructor constructor)
	{
		//bhU_Debug.ASSERT(this.checkLegalStateManipulation());

		bhU_Debug.ASSERT(thisArg instanceof smA_StateMachine);
		
		((smA_StateMachine) thisArg).internal_setState(T, constructor);
	}
}
