package b33hive.shared.statemachine;

import b33hive.shared.debugging.bhU_Debug;

/**
 * This base class provides both actions and states with a unified API to safely manipulate the machine tree.
 * More runtime protections, mostly for debugging purposes, will be added to this class in the future.
 * 
 * @author Doug
 *
 */
public class bhA_BaseStateObject extends Object
{
	protected void container_enterState(bhA_State thisArg, Class<? extends bhA_State> T)
	{
		this.container_enterState(thisArg, T, null);
	}
	
	protected void container_enterState(bhA_State thisArg, Class<? extends bhA_State> T, bhA_StateConstructor constructor)
	{
		bhU_Debug.ASSERT(thisArg instanceof bhA_StateContainer);
		
		((bhA_StateContainer) thisArg).internal_enterState(T, constructor);
	}
	
	protected void container_foregroundState(bhA_State thisArg, Class<? extends bhA_State> T)
	{
		bhU_Debug.ASSERT(thisArg instanceof bhA_StateContainer);
		
		((bhA_StateContainer) thisArg).internal_foregroundState(T);
	}
	
	protected void container_backgroundState(bhA_State thisArg, Class<? extends bhA_State> T)
	{
		bhU_Debug.ASSERT(thisArg instanceof bhA_StateContainer);
		
		((bhA_StateContainer) thisArg).internal_backgroundState(T);
	}
	
	protected void container_exitState(bhA_State thisArg, Class<? extends bhA_State> T)
	{
		bhU_Debug.ASSERT(thisArg instanceof bhA_StateContainer);
		
		((bhA_StateContainer) thisArg).internal_exitState(T);
	}
	
	protected void machine_pushState(bhA_State thisArg, Class<? extends bhA_State> T)
	{
		this.machine_pushState(thisArg, T, null);
	}
	
	protected void machine_pushState(bhA_State thisArg, Class<? extends bhA_State> T, bhA_StateConstructor constructor)
	{
		//bhU_Debug.ASSERT(m_parent.checkLegalStateManipulation());
		
		bhU_Debug.ASSERT(thisArg instanceof bhA_StateMachine);
		
		((bhA_StateMachine) thisArg).internal_pushState(T, constructor);
	}
	
	protected void machine_popState(bhA_State thisArg, Object ... args)
	{
		//bhU_Debug.ASSERT(this.checkLegalStateManipulation());
		
		bhU_Debug.ASSERT(thisArg instanceof bhA_StateMachine);
		
		((bhA_StateMachine) thisArg).internal_popState(args);
	}
	
	protected void machine_setState(bhA_State thisArg, Class<? extends bhA_State> T)
	{
		this.machine_setState(thisArg, T, null);
	}
	
	protected void machine_beginBatch(bhA_State thisArg)
	{
		thisArg.m_root.beginBatch();
	}
	
	protected void machine_endBatch(bhA_State thisArg)
	{
		thisArg.m_root.endBatch();
	}
	
	protected void machine_setState(bhA_State thisArg, Class<? extends bhA_State> T, bhA_StateConstructor constructor)
	{
		//bhU_Debug.ASSERT(this.checkLegalStateManipulation());

		bhU_Debug.ASSERT(thisArg instanceof bhA_StateMachine);
		
		((bhA_StateMachine) thisArg).internal_setState(T, constructor);
	}
}
