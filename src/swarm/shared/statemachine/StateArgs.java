package swarm.shared.statemachine;

public class StateArgs
{
	private static final Object[] DUMMY_VALUES = {};
	
	public Object[] values;
	
	public StateArgs()
	{
		values = null;
	}
	
	public StateArgs(Object ... values_in)
	{
		this.values = values_in;
	}
	
	public void set(Object ... values_in)
	{
		this.values = values_in;
	}
	
	public void set(int index, Object values_in)
	{
		if( this.values == null )
		{
			this.values = new Object[index+1];
		}
		else if( this.values.length <= index )
		{
			Object[] oldValue = this.values;
			this.values = new Object[index+1];
			
			for( int i = 0; i < oldValue.length; i++ )
			{
				this.values[i] = oldValue[i];
			}
		}
		
		this.values[index] = values_in;
	}
	
	public <T extends Object> T cast()
	{
		return (T) this;
	}
	
	public <T extends Object> T get(int index)
	{
		return (T) (values != null && values.length > index ? values[index] : null);
	}
	
	public <T extends Object> T get()
	{
		return get(0);
	}
	
	public boolean contains(Object arg)
	{
		if( values == null || arg == null )  return false;
		
		for( int i = 0; i < values.length; i++ )
		{
			if( arg.equals(values[i]) )  return true;
		}
		
		return false;
	}
	
	public boolean equals(Object ... values)
	{
		Object[] values_this = this.values != null ? this.values : DUMMY_VALUES;
		Object[] values_that = values != null ? values : DUMMY_VALUES;
		int max = values_this.length > values_that.length ? values_this.length : values_that.length;
		
		for( int i = 0; i < max; i++ )
		{
			Object value_this = i < values_this.length ? values_this[i] : null;
			Object value_that = i < values_that.length ? values_that[i] : null;
			
			if( value_this != null && value_that != null )
			{
				if( !value_this.equals(value_that) )
				{
					return false;
				}
			}
			else if( value_this != null || value_that != null )
			{
				return false;
			}
		}
		
		return true;
	}
	
	public boolean equals(StateArgs args)
	{
		if( args == null )  return false;
		
		if( args.values == null && this.values == null )  return true;

		return equals(args.values);
	}
}