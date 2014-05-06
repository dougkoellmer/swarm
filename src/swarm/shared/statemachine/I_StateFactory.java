package swarm.shared.statemachine;

public interface I_StateFactory
{
	A_State newState(Class<? extends A_State> stateClass);
	
	A_Action newAction(Class<? extends A_Action> actionClass);
}
