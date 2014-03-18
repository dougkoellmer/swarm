package swarm.shared.statemachine;

public class StateReflectionFactory implements I_StateFactory
{
	@Override
	public A_State newInstance(Class<? extends A_State> stateClass)
	{
		try
		{
			return stateClass.newInstance();
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
