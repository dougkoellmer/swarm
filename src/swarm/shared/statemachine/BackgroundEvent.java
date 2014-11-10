package swarm.shared.statemachine;

/**
 * 
 * @author dougkoellmer
 */
public class BackgroundEvent extends LifecycleEvent
{
	private Class<? extends A_State> m_blockingState;
	
	void init(A_State state, Class<? extends A_State> blockingState)
	{
		super.init(state, E_Event.DID_BACKGROUND);
		
		m_blockingState = blockingState;
	}
	
	@Override void clean()
	{
		super.clean();
		
		m_blockingState = null;
	}
	
	public Class<? extends A_State> getBlockingState()
	{
		return m_blockingState;
	}
}
