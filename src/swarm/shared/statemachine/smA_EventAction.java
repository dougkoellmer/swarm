package swarm.shared.statemachine;


public abstract class smA_EventAction extends smA_Action
{
	@Override
	public final void perform(smA_ActionArgs args)
	{
		// do nothing
	}

	@Override
	public abstract Class<? extends smA_State> getStateAssociation();
}
