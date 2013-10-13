package swarm.shared.statemachine;

public abstract class smA_ActionArgs
{
	public Object userData;
	
	public smA_ActionArgs()
	{
		userData = null;
	}
	
	public smA_ActionArgs(Object userData)
	{
		this.userData = userData;
	}
}