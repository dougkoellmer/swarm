package swarm.client.states;

import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_ActionArgs;
import swarm.shared.statemachine.A_StateConstructor;

import swarm.shared.statemachine.A_State;


/**
 * ...
 * @author 
 */
public class State_GenericDialog extends A_State
{
	public static final class Constructor extends A_StateConstructor
	{
		private final String m_title;
		private final String m_body;
		
		public Constructor(String title, String body)
		{
			m_title = title;
			m_body = body;
		}
	}
	
	
	public static class Ok extends A_Action
	{
		@Override
		public void perform(A_ActionArgs args)
		{
			machine_popState(getContext().getForegroundedState(StateMachine_Base.class));
		}
	}
	
	public State_GenericDialog()
	{
		registerAction(new State_GenericDialog.Ok());
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
	protected void didEnter(A_StateConstructor constructor)
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