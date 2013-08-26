package swarm.shared.utils;

import java.util.ArrayList;
import java.util.HashMap;


import swarm.shared.statemachine.smA_State;

public class sm$
{
	private static HashMap<Class, Object> s_instances = new HashMap<Class, Object>();
	
	public static void register(Object instance)
	{
		register(instance, instance.getClass());
	}
	
	public static void register(Object instance, Class<? extends Object> T_override)
	{
		s_instances.put(T_override, instance);
	}
	
	public static <T extends Object> T get(Class<? extends Object> T)
	{
		return (T) s_instances.get(T);
	}
}
