package swarm.shared.statemachine;

public abstract class StateArgs
{
	public Object userData;
	
	public StateArgs()
	{
		userData = null;
	}
	
	public StateArgs(Object userDataIn)
	{
		this.userData = userDataIn;
	}
	
	public <T extends StateArgs> T cast()
	{
		return (T) this;
	}
	
	public <T extends Object> T castUserData()
	{
		return (T) userData;
	}
}