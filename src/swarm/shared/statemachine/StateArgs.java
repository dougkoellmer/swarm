package swarm.shared.statemachine;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author dougkoellmer
 */
public class StateArgs
{
	private static final Object[] EMPTY_ARRAY = {};
	
	public static final StateArgs DEFAULT = new StateArgs();
	
	private final Object[] m_values;
	
	public StateArgs()
	{
		m_values = EMPTY_ARRAY;
	}
	
	public StateArgs(Object value)
	{
		if( value == null )
		{
			m_values = EMPTY_ARRAY;
		}
		else
		{
			m_values = new Object[]{value};
		}
	}
	
	public StateArgs(Object ... values_in)
	{
		if( values_in == null || values_in.length == 0 )
		{
			m_values = EMPTY_ARRAY;
		}
		else
		{
			m_values = new Object[values_in.length];
			
			for( int i = 0; i < values_in.length; i++ )
			{
				m_values[i] = values_in[i];
			}
		}
	}
	
	public int count()
	{
		return m_values.length;
	}
	
	private StateArgs(Object[] values_in, boolean thisDummyParameterIsHereSoWeCanOverloadTheConstructor)
	{
		if( values_in == null || values_in.length == 0 )
		{
			m_values = EMPTY_ARRAY;
		}
		else
		{
			m_values = values_in;
		}
	}
	
	public <T extends Object> T cast()
	{
		return (T) this;
	}
	
	public <T extends Object> T get(int index)
	{
		return (T) (m_values != null && m_values.length > index ? m_values[index] : null);
	}

	public <T extends Object> T get()
	{
		return get(0);
	}
	
	public <T extends Object> T get(Class<T> paramType)
	{
		return get(paramType, 0);
	}
	
	public <T extends Object> T get(Class<T> paramType, int index)
	{
		return get_private(paramType, index, /*strictComparison=*/false);
	}
	
	private <T extends Object> T get_private(Class<T> paramType, int index, boolean strictComparison)
	{
		if( paramType == null )  return null;
		
		int count = 0;
		
		for( int i = 0; i < m_values.length; i++ )
		{
			Object ithValue = m_values[i];
			
			if( ithValue != null )
			{
				if( !strictComparison && PU_StateMachine.instanceOf(ithValue.getClass(), paramType) || strictComparison && ithValue.getClass() == paramType )
				{
					if( count == index )
					{
						return (T) ithValue;
					}
					else
					{
						count++;
					}
				}
			}
		}
		
		return null;
	}
	
	public StateArgs impose(StateArgs args)
	{
		StateArgs args_this = this;
		StateArgs args_that = args;
		
		if( args_that == null )				return this;
		if( args_that.m_values.length == 0 )  return this;
		if( args_this.m_values.length == 0 )  return args;
		
		HashMap<Class<? extends Object>, Integer> counts = null;
		ArrayList<Object> newValues = new ArrayList<Object>();
		
		for( int i = 0; i < args_this.m_values.length; i++ )
		{
			Object ithArg_this = args_this.m_values[i];
			
			if( ithArg_this == null )  continue;
			
			Class<? extends Object> ithArgType = ithArg_this.getClass();
			
			Integer index = counts == null ? 0 : counts.get(ithArgType);
			index = index != null ? index : 0;
			
			Object potentiallyOverridingArg_that = args_that.get_private(ithArgType, index, /*strictComparison=*/true);
			
			if( potentiallyOverridingArg_that != null )
			{
				counts = counts != null ? counts : new HashMap<Class<? extends Object>, Integer>();
				counts.put(ithArgType, index + 1);
				
				newValues.add(potentiallyOverridingArg_that);
			}
			else
			{
				newValues.add(ithArg_this);
			}
		}
		
		for( int i = 0; i < args_that.m_values.length; i++ )
		{
			Object ithArg_that = args_that.m_values[i];
			
			if( ithArg_that == null )  continue;
			
			if( counts != null )
			{
				Integer index = counts.get(ithArg_that.getClass());
				
				if( index != null && index > 0 )
				{
					counts.put(ithArg_that.getClass(), index-1);
					
					continue;
				}
			}
			
			newValues.add(ithArg_that);
		}
		
		boolean thisParameterIsSoWeCanOverloadTheConstructor = true;
		
		return new StateArgs(newValues.toArray(), thisParameterIsSoWeCanOverloadTheConstructor);
	}
	
	public boolean contains(Object arg)
	{
		if( m_values == null || arg == null )  return false;
		
		for( int i = 0; i < m_values.length; i++ )
		{
			if( arg.equals(m_values[i]) )  return true;
		}
		
		return false;
	}
	
	public boolean containsAny(Object ... args )
	{
		if( args == null || args.length == 0 )  return true;
		
		for( int i = 0; i < args.length; i++ )
		{
			if( contains(args[i] ) )  return true;
		}
		
		return false;
	}
	
	public boolean containsAll(Object ... args )
	{
		if( args == null || args.length == 0 )  return true;
		
		for( int i = 0; i < args.length; i++ )
		{
			if( !contains(args[i] ) )  return false;
		}
		
		return true;
	}
	
	//TODO: HAve to define the behavior of the equals methods below a little better. Fore example does order matter or is it more like a tuple?
	
//	public boolean equals(Object ... argValues)
//	{
//		Object[] values_this = this.m_values != null ? this.m_values : EMPTY_ARRAY;
//		Object[] values_that = argValues != null ? argValues : EMPTY_ARRAY;
//		int max = values_this.length > values_that.length ? values_this.length : values_that.length;
//		
//		for( int i = 0; i < max; i++ )
//		{
//			Object value_this = i < values_this.length ? values_this[i] : null;
//			Object value_that = i < values_that.length ? values_that[i] : null;
//			
//			if( value_this != null && value_that != null )
//			{
//				if( !value_this.equals(value_that) )
//				{
//					return false;
//				}
//			}
//			else if( value_this != null || value_that != null )
//			{
//				return false;
//			}
//		}
//		
//		return true;
//	}
//	
//	@Override public boolean equals(Object argValue)
//	{
//		if(m_values == null || m_values.length == 0 )
//		{
//			if( argValue == null )
//			{
//				return true;
//			}
//			else return false;
//		}
//		else
//		{
//			for( int i = 0; i < m_values.length; i++ )
//			{
//				Object ithValue = m_values[i];
//				
//				if( ithValue != null )
//				{
//					if( argValue == null )  return false;
//					
//					return ithValue.equals(argValue);
//				}
//			}
//			
//			if( argValue != null )  return false;
//			
//			return true;
//		}
//	}
//	
//	public boolean equals(StateArgs args)
//	{
//		if( args == null && m_values.length == 0 )  return true;
//		
//		args = A_StateContextForwarder.defaultArgs(args);
//		
//		if( args.m_values == null && this.m_values == null )  return true;
//
//		return equals(args.m_values);
//	}
	
	@Override public String toString()
	{
		return m_values.toString();
	}
}