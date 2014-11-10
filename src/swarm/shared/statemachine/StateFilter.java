package swarm.shared.statemachine;


/**
 * 
 * @author dougkoellmer
 */
public class StateFilter
{
	public static class Match
	{
		private final Target m_target;
		
		Match(Target target)
		{
			m_target = target;
		}
		
		Target getTarget()
		{
			return m_target;
		}
	}
	
	public static class Target
	{
		private final Scope m_scope;
		
		public final Match MATCHING 				= new Match(this);
		public final Match WITH_ANY 				= new Match(this);
		public final Match WITH_ALL 				= new Match(this);
			
		Target(Scope scope)
		{
			m_scope = scope;
		}
		
		Scope getScope()
		{
			return m_scope;
		}
	}
	
	public static class Scope
	{
		public final Target QUEUE					= new Target(this);
		public final Target HISTORY					= new Target(this);
	}
	
	public static final Scope ALL					= new Scope();
	public static final Scope FIRST					= new Scope();
	public static final Scope LAST					= new Scope();
	
	
	public static final Target QUEUE				= ALL.QUEUE;
	public static final Target HISTORY				= ALL.HISTORY;
}
