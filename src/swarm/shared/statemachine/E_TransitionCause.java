package swarm.shared.statemachine;

/**
 * 
 * @author dougkoellmer
 */
public enum E_TransitionCause
{
	PUSH,
	GO,
	POP,
	DEQUEUE,
	PUSH_V,
	POP_V,
	SET,
	EXTERNAL;
}
