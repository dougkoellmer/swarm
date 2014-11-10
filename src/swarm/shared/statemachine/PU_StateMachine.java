package swarm.shared.statemachine;

/**
 * 
 * @author dougkoellmer
 */
class PU_StateMachine
{
	public static boolean instanceOf(Class<?> subclass, Class<?> superclass)
	{
		return subclass == superclass || superclass.isAssignableFrom(subclass);
	}
}
