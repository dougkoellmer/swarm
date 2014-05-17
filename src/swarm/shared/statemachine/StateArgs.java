package swarm.shared.statemachine;

public class StateArgs
{
	public Object[] userData;
	
	public StateArgs()
	{
		userData = null;
	}
	
	public StateArgs(Object ... userData_in)
	{
		this.userData = userData_in;
	}
	
	public void set(Object ... userData_in)
	{
		this.userData = userData_in;
	}
	
	public void set(int index, Object userData_in)
	{
		if( this.userData == null )
		{
			this.userData = new Object[index+1];
		}
		else if( this.userData.length <= index )
		{
			Object[] oldUserData = this.userData;
			this.userData = new Object[index+1];
			
			for( int i = 0; i < oldUserData.length; i++ )
			{
				this.userData[i] = oldUserData[i];
			}
		}
		
		this.userData[index] = userData_in;
	}
	
	public <T extends Object> T cast()
	{
		return (T) this;
	}
	
	public <T extends Object> T get(int index)
	{
		return (T) (userData != null && userData.length > index ? userData[index] : null);
	}
	
	public <T extends Object> T get()
	{
		return get(0);
	}
	
	public boolean contains(Object arg)
	{
		if( userData == null || arg == null )  return false;
		
		for( int i = 0; i < userData.length; i++ )
		{
			if( arg.equals(userData[i]) )  return true;
		}
		
		return false;
	}
}