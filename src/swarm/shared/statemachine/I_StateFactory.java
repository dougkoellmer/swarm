package swarm.shared.statemachine;

public interface I_StateFactory
{
	A_State newInstance(Class<? extends A_State> stateClass);
}
