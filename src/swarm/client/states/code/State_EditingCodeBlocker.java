package swarm.client.states.code;


import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_EventAction;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.structs.CellAddress;

public class State_EditingCodeBlocker extends A_State
{
	public static enum Reason
	{
		NO_CELL_SELECTED,
		LOADING,
		SNAPPING,
		NO_HTML,
		ERROR,
		SYNCING,
		PREVIEWING
	};
	
	static class Constructor extends StateArgs
	{
		private final Reason m_reason;
		
		public Constructor(Reason reason)
		{
			m_reason = reason;
		}
	}
	
	public static class OnReasonChange extends A_EventAction
	{
		@Override
		public Class<? extends A_State> getStateAssociation()
		{
			return State_EditingCodeBlocker.class;
		}
	}
	
	public State_EditingCodeBlocker()
	{
		registerAction(new OnReasonChange());
	}
	
	
	private Reason m_reason = null;
	
	void setReason(Reason reason)
	{
		m_reason = reason;
		
		this.perform(OnReasonChange.class);
	}
	
	public Reason getReason()
	{
		return m_reason;
	}
	
	@Override
	protected void didEnter(StateArgs constructor)
	{
		Constructor thisCons = (Constructor) constructor;
		
		m_reason = thisCons.m_reason;
	}
}
