package swarm.shared.statemachine;

/**
 * 
 * @author dougkoellmer
 */
public class UpdateEvent extends LifecycleEvent
{
	private double m_timestep;
	
	void init(A_State state)
	{
		super.init(state, E_Event.DID_UPDATE);
		
		m_timestep = state.getLastTimeStep();
	}
	
	@Override void clean()
	{
		super.clean();
		
		m_timestep = 0.0;
	}
	
	public double getTimestep()
	{
		return m_timestep;
	}
}
