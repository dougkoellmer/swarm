package swarm.shared.statemachine;

public class StateOperationResult extends A_BaseStateObject
{
	private A_BaseStateObject m_source;
	private boolean m_succeeded;
	private boolean m_proceed;
	
	A_BaseStateObject getSource()
	{
		return m_source;
	}
	
	void init(A_BaseStateObject source, boolean succeeded)
	{
		m_source = source;
		m_context = m_source.m_context;
		m_succeeded = succeeded;
		m_proceed = true;
	}
	
	void clean()
	{
		m_source = null;
		m_context = null;
		m_proceed = true;
		m_succeeded = false;
	}
	
	@Override
	boolean isLocked()
	{
		return !m_proceed;
	}
	
	public boolean succeeded()
	{
		return m_succeeded;
	}
	
	public void succceeded(boolean value)
	{
		m_succeeded = false;
	}
	
	public StateOperationResult otherwise()
	{
		if( !succeeded() )
		{
			m_proceed = true;
		}
		else
		{
			m_proceed = false;
		}
		
		return this;
	}
}