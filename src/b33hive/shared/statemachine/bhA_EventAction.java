package b33hive.shared.statemachine;


public abstract class bhA_EventAction extends bhA_Action
{
	@Override
	public final void perform(bhA_ActionArgs args)
	{
		// do nothing
	}

	@Override
	public abstract Class<? extends bhA_State> getStateAssociation();
}
