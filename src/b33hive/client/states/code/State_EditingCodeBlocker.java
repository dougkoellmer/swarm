package com.b33hive.client.states.code;


import com.b33hive.shared.statemachine.bhA_Action;
import com.b33hive.shared.statemachine.bhA_EventAction;
import com.b33hive.shared.statemachine.bhA_State;
import com.b33hive.shared.statemachine.bhA_StateConstructor;
import com.b33hive.shared.structs.bhCellAddress;

public class State_EditingCodeBlocker extends bhA_State
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
	
	static class Constructor extends bhA_StateConstructor
	{
		private final Reason m_reason;
		
		public Constructor(Reason reason)
		{
			m_reason = reason;
		}
	}
	
	public static class OnReasonChange extends bhA_EventAction
	{
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return State_EditingCodeBlocker.class;
		}
	}
	
	public State_EditingCodeBlocker()
	{
		bhA_Action.register(new OnReasonChange());
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
	protected void didEnter(bhA_StateConstructor constructor)
	{
		Constructor thisCons = (Constructor) constructor;
		
		m_reason = thisCons.m_reason;
	}
}
