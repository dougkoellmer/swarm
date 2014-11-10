package swarm.shared.statemachine;

/**
 * 
 * @author dougkoellmer
 */
public abstract class A_Action_Returning extends A_Action_Base
{
	public abstract StateArgs perform(StateArgs args);
}
