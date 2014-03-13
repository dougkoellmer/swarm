package swarm.shared.statemachine;

public abstract class A_StateConstructor
{
	private final Object userData;
	
	public A_StateConstructor()
	{
		userData = null;
	}
	
	public A_StateConstructor(Object inputUserData)
	{
		this.userData = inputUserData;
	}
}
