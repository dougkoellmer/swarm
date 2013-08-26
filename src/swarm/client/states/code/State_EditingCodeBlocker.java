package swarm.client.states.code;


import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_EventAction;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateConstructor;
import swarm.shared.structs.smCellAddress;

public class State_EditingCodeBlocker extends smA_State
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
	
	static class Constructor extends smA_StateConstructor
	{
		private final Reason m_reason;
		
		public Constructor(Reason reason)
		{
			m_reason = reason;
		}
	}
	
	public static class OnReasonChange extends smA_EventAction
	{
		@Override
		public Class<? extends smA_State> getStateAssociation()
		{
			return State_EditingCodeBlocker.class;
		}
	}
	
	public State_EditingCodeBlocker()
	{
		smA_Action.register(new OnReasonChange());
	}
	
	
	private Reason m_reason = null;
	
	void setReason(Reason reason)
	{
		m_reason = reason;
		
		this.performAction(OnReasonChange.class);
	}
	
	public Reason getReason()
	{
		return m_reason;
	}
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
		Constructor thisCons = (Constructor) constructor;
		
		m_reason = thisCons.m_reason;
	}
}
