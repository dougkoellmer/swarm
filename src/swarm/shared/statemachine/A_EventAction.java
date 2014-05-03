package swarm.shared.statemachine;


public abstract class A_EventAction extends A_Action
{
	@Override
	public final void perform(StateArgs args)
	{
		// do nothing
	}
	
	@Override
	public boolean isPerformableInBackground()
	{
		return true;
	}
}
