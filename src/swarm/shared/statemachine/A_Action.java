package swarm.shared.statemachine;

/**
 * 
 * @author dougkoellmer
 */
public abstract class A_Action extends A_Action_Base
{
	public abstract void perform(StateArgs args);
}