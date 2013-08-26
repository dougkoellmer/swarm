package swarm.client.states;

import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_ActionArgs;
import swarm.shared.statemachine.smA_StateConstructor;

import swarm.shared.statemachine.smA_State;


/**
 * ...
 * @author 
 */
public class State_GenericDialog extends smA_State
{
	public static final class Constructor extends smA_StateConstructor
	{
		private final String m_title;
		private final String m_body;
		
		public Constructor(String title, String body)
		{
			m_title = title;
			m_body = body;
		}
	}
	
	
	public static class Ok extends smA_Action
	{
		@Override
		public void perform(smA_ActionArgs args)
		{
			machine_popState(smA_State.getForegroundedInstance(StateMachine_Base.class));
		}

		@Override
		public Class<? extends smA_State> getStateAssociation()
		{
			return State_GenericDialog.class;
		}
	}
	
	public State_GenericDialog()
	{
		smA_Action.register(new State_GenericDialog.Ok());
	}
	
	
	private String m_title = null;
	private String m_body = null;
	
	public String getTitle()
	{
		return m_title;
	}
	
	public String getBody()
	{
		return m_body;
	}
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
		Constructor cons = (Constructor) constructor;
		
		m_title = cons.m_title;
		m_body = cons.m_body;
	}
	
	@Override
	protected void willExit()
	{
		m_title = null;
		m_body = null;
	}
}