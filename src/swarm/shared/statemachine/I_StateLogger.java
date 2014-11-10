package swarm.shared.statemachine;

public interface I_StateLogger
{
	void log(Class<? extends A_BaseStateObject> stateClass, E_Event type, String message);
}
