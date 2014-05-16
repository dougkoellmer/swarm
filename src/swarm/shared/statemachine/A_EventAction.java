package swarm.shared.statemachine;


public abstract class A_EventAction extends A_Action
{
	/**
	 * There's generally no need for subclasses to implement this method.
	 * This dummy override simple does nothing.
	 */
	@Override public void perform(StateArgs args)
	{
		// do nothing
	}

	/**
	 * By default this overrides {@link A_Action} behavior to return true.
	 * Most of the time you want event-type-actions to fire regardless of
	 * whether the action's state is foregrounded.
	 */
	@Override public boolean isPerformableInBackground()
	{
		return true;
	}
}
