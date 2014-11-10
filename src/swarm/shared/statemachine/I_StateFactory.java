package swarm.shared.statemachine;

/**
 * 
 * @author dougkoellmer
 */
public interface I_StateFactory
{
	A_State newState(Class<? extends A_State> stateClass);
	
	A_Action_Base newAction(Class<? extends A_Action_Base> actionClass);
}
