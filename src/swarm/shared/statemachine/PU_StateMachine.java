package swarm.shared.statemachine;

/**
 * 
 * @author dougkoellmer
 */

class PU_StateMachine
{
	public static boolean instanceOf(Class<?> subclass, Class<?> superclass)
	{
		//return subclass == superclass || superclass.isAssignableFrom(subclass);
		
		
		//--- DRK > Dumbed down version not using isAssignableFrom for GWT JRE emulation.
		//---		TODO: Use dependency injection or something so normal java runtimes can implement this better.
		Class<?> curr = subclass;
		
		while( curr != null )
		{
			if( curr == superclass )  return true;
			
			curr = curr.getSuperclass();
		}
		
		return false;
	}
}
