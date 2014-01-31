package swarm.shared.statemachine;

public abstract class A_ActionArgs
{
	public Object userData;
	
	public A_ActionArgs()
	{
		userData = null;
	}
	
	public A_ActionArgs(Object userData)
	{
		this.userData = userData;
	}
}