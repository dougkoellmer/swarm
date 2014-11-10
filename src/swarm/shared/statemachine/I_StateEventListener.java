package swarm.shared.statemachine;


/**
 * ...
 * @author dougkoellmer 
 */
public interface I_StateEventListener 
{
	public static final E_Event DID_ENTER				= E_Event.DID_ENTER;
	public static final E_Event DID_FOREGROUND			= E_Event.DID_FOREGROUND;
	public static final E_Event DID_UPDATE				= E_Event.DID_UPDATE;
	public static final E_Event DID_BACKGROUND			= E_Event.DID_BACKGROUND;
	public static final E_Event DID_EXIT				= E_Event.DID_EXIT;
	public static final E_Event DID_PERFORM_ACTION		= E_Event.DID_PERFORM_ACTION;
	
	public static final StateFilter.Scope ALL			= StateFilter.ALL;
	public static final StateFilter.Scope FIRST			= StateFilter.FIRST;
	public static final StateFilter.Scope LAST			= StateFilter.LAST;	
	public static final StateFilter.Target QUEUE		= ALL.QUEUE;
	public static final StateFilter.Target HISTORY		= ALL.HISTORY;
	
	void onStateEvent(A_BaseStateEvent e);
}