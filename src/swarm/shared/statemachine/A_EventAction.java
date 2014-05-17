package swarm.shared.statemachine;


public abstract class A_EventAction extends A_Action
{
	/**
	 * There's generally no need for subclasses to implement this method.
	 * This dummy override does nothing.
	 */
	@Override public void perform(StateArgs args)
	{
		// do nothing
	}

	/**
	 * This overrides the default {@link A_Action} behavior to return true instead of false
	 * Most of the time you want event-type-actions to fire regardless of
	 * whether the action's state is foregrounded.
	 */
	@Override public boolean isPerformableInBackground()
	{
		return true;
	}
}
