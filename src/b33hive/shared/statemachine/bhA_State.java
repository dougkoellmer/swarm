package b33hive.shared.statemachine;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import b33hive.shared.debugging.bhU_Debug;


/**
 * ...
 * @author 
 */
public abstract class bhA_State extends bhA_BaseStateObject
{
	private static final Logger s_logger = Logger.getLogger(bhA_State.class.getName());
	
	private static HashMap<Class<? extends bhA_State>, bhA_State> s_stateRegistry = new HashMap<Class<? extends bhA_State>, bhA_State>();
	
	bhStateTreeRoot m_root;
	
	private float m_totalTimeInState = 0.0f;
	private float m_foregroundedTimeInState = 0.0f;
	
	private boolean m_isForegrounded = false;
	private boolean m_isEntered = false;
	
	private boolean m_isEntering = false;
	private boolean m_isForegrounding = false;
	
	bhA_State m_stateBeneath = null;	
	
	private HashMap<Class<? extends bhA_Action>, Boolean> m_isPerformableOverrides = new HashMap<Class<? extends bhA_Action>, Boolean>();
	
	bhA_State m_parent = null;
	
	Class<? extends bhA_State> m_previousState = null;
	
	private Class<? extends bhA_Action> m_lastActionPerformed = null;
	
	private double m_lastTimeStep = 0;
	
	private int m_foregroundedUpdateCount = 0;
	private int m_totalUpdateCount = 0;
	
	private Object m_userData = null;
	
	protected bhA_State() 
	{
	}
	
	public static bhStateTreeRoot root_didEnter(Class<? extends bhA_State> T__extends__bhA_State, bhI_StateEventListener stateEventListener)
	{
		bhA_State state = bhA_State.getInstance(T__extends__bhA_State);
		bhStateTreeRoot root = new bhStateTreeRoot(state);
		root.addListener(stateEventListener);
		state.m_root = root;
		
		state.internal_didEnter(null);
		
		return root;
	}
	
	public static void root_didForeground(Class<? extends bhA_State> T__extends__bhA_State)
	{
		bhA_State state = bhA_State.getEnteredInstance(T__extends__bhA_State);
		state.internal_didForeground(null, null);
	}
	
	public static void root_didUpdate(Class<? extends bhA_State> T__extends__bhA_State, double timeStep)
	{
		bhA_State state = bhA_State.getEnteredInstance(T__extends__bhA_State);
		state.internal_update(timeStep);
	}
	
	public boolean isTransparent()
	{
		return false;
	}
	
	public boolean isForegrounded()
	{
		return m_isForegrounded;
	}
	
	public boolean isEntering()
	{
		return m_isEntering;
	}
	
	public boolean isForegrounding()
	{
		return m_isForegrounding;
	}
	
	public boolean isEntered()
	{
		return m_isEntered;
	}
	
	public int getUpdateCount()
	{
		return m_totalUpdateCount;
	}
	
	public int getForegroundedUpdateCount()
	{
		return m_foregroundedUpdateCount;
	}
	
	public double getLastTimeStep()
	{
		return m_lastTimeStep;
	}
	
	public Class<? extends bhA_State> getPreviousState()
	{
		return m_previousState;
	}
	
	public Class<? extends bhA_Action> getLastActionPerformed()
	{
		return m_lastActionPerformed;
	}
	
	public <T extends bhA_State> T getParent()
	{
		return (T) m_parent;
	}
	
	public bhA_State getStateBeneath()
	{
		return m_stateBeneath;
	}
	
	public float getTimeInState(bhE_StateTimeType eTimeType)
	{
		eTimeType = eTimeType == null ? bhE_StateTimeType.TOTAL : eTimeType;
		
		switch(eTimeType)
		{
			case FOREGROUNDED:	return m_foregroundedTimeInState;
			case TOTAL:			return m_totalTimeInState;
			
			// TODO: Probably not useful....might need a another member to track this in a useful manner.
			case BACKGROUNDED:	return m_totalTimeInState - m_foregroundedTimeInState;
		}
		
		return 0.0f;
	}
	
	private void clean()
	{
		m_totalTimeInState = 0.0f;
		m_foregroundedTimeInState = 0.0f;
		m_isForegrounded = false;
		m_isEntered = false;
		m_totalUpdateCount = 0;
		m_userData = null;
		m_foregroundedUpdateCount = 0;

		m_isPerformableOverrides.clear();
	}
	
	protected void setPerformableOverride(Class<? extends bhA_Action> T, boolean perfomable)
	{
		bhU_Debug.ASSERT(m_parent != null );
		bhU_Debug.ASSERT(m_isEntered);
		
		m_isPerformableOverrides.put(T, perfomable);
	}
	
	public boolean performAction(Class<? extends bhA_Action> T)
	{
		return this.performAction(T, null);
	}
	
	public boolean performAction(Class<? extends bhA_Action> T, bhA_ActionArgs args)
	{
		bhA_Action action = bhA_Action.getInstance(T);

		if ( action == null )
		{
			return false;
		}
		
		if( !action.suppressLog() )
		{
			//s_logger.log(Level.INFO, "Will perform action: " + action.getClass().getName());
		}
		
		boolean performable = this.isPerformable_private(action, args);
		
		if( !performable )
		{				
			//s_logger.log(Level.INFO, "Action not perfomable: " + action.getClass().getName());
			
			return false;
		}
		else
		{
			this.m_lastActionPerformed = T;
			
			bhStateTreeRoot root = m_root;

			action.m_state = this;
			root.queueEvent(new bhStateEvent(action, args));
			{
				bhA_Action.s_actionStack.add(action);
				
				action.m_isCancelled = false;
				
				action.prePerform();
				
				action.perform(args);
			
				bhA_Action.s_actionStack.remove(bhA_Action.s_actionStack.size()-1);
			}
			action.m_state = null;

			root.processEventQueue();
			
			return true;
		}
	}
	
	public boolean isActionPerfomable(Class<? extends bhA_Action> T, bhA_ActionArgs args)
	{
		return this.isPerformable_private(bhA_Action.getInstance(T), args);
	}
	
	private boolean isPerformable_private(bhA_Action action, bhA_ActionArgs args)
	{
		boolean perfomable = true;

		if( !this.isActionLegal(action) )
		{
			perfomable = false;
		}
		else
		{
			action.m_state = this;
			{
				perfomable = action.isPerformable(args);
			}
			action.m_state = null;
		}
		
		return perfomable;
	}
	
	private boolean isActionLegal(bhA_Action action)
	{
		if ( !this.m_isEntered )
		{
			bhU_Debug.ASSERT(false);
			return false;
		}
		
		if( !this.m_isForegrounded )
		{
			if ( !action.isPerformableInBackground() )
			{
				return false;
			}
		}
		
		Class<? extends bhA_State> actionState = action.getStateAssociation();

		//if( actionState.isInstance(this) ) // TODO: this check is kinda slow (string comparison) if implemented correctly in GWT, so ignoring for now
		{
			if ( m_isPerformableOverrides.containsKey(action.getClass()) )
			{
				return m_isPerformableOverrides.get(action.getClass());
			}
			
			return true;
		}
		
		//return false;
	}
	
	
	
	
	void internal_didEnter(bhA_StateConstructor constructor)
	{
		bhStateTreeRoot root = m_root;
		
		if( this instanceof bhI_StateEventListener )
		{
			root.addListener((bhI_StateEventListener) this);
		}
		
		this.clean();
		
		if( constructor != null )
		{
			m_userData = constructor.getUserData();
		}
		
		m_isEntered = true;
		
		//s_logger.log(Level.INFO, "Will enter state: " + this.getClass().getName());
		
		root.queueEvent(new bhStateEvent(bhE_StateEventType.DID_ENTER, this));
		
		m_isEntering = true;
		
		this.didEnter(constructor);
		
		m_isEntering = false;
		
		root.processEventQueue();
	}
	
	void internal_didForeground(Class<? extends bhA_State> revealingState, Object[] args)
	{
		m_isForegrounded = true;
		
		m_foregroundedTimeInState = 0.0f;
		m_foregroundedUpdateCount = 0;
		
		bhStateTreeRoot root = m_root;
		
		//s_logger.log(Level.INFO, "Will foreground state: " + this.getClass().getName());
		
		root.queueEvent(new bhStateEvent(bhE_StateEventType.DID_FOREGROUND, this));
		
		m_isForegrounding = true;
		
		this.didForeground(revealingState, args);
		
		m_isForegrounding = false;
		
		root.processEventQueue();
	}
	
	void internal_update(double timeStep)
	{
		bhStateTreeRoot root = m_root;
		
		bhU_Debug.ASSERT(m_isEntered, "bhA_State::update1");
		
		if( !m_isEntered )  return;
		
		if( m_isEntered )
		{
			m_totalTimeInState += timeStep;
		}
		
		if( m_isForegrounded )
		{
			m_foregroundedTimeInState += timeStep;
			m_foregroundedUpdateCount++;
		}
		
		root.queueEvent(new bhStateEvent( bhE_StateEventType.DID_UPDATE, this));
		
		m_lastTimeStep = timeStep;
		
		m_totalUpdateCount++;
		
		this.update(timeStep);
		
		root.processEventQueue();
	}
	
	void internal_willBackground(Class<? extends bhA_State> blockingState)
	{
		bhStateTreeRoot root = m_root;
		
		//s_logger.log(Level.INFO, "Will background state: " + this.getClass().getName());
		
		root.queueEvent(new bhStateEvent(bhE_StateEventType.DID_BACKGROUND, this, blockingState));
		
		this.willBackground(blockingState);
		
		m_isForegrounded = false;
		
		root.processEventQueue();
		
		m_foregroundedTimeInState = 0.0f;
		m_foregroundedUpdateCount = 0;
	}
	
	void internal_willExit()
	{
		bhStateTreeRoot root = m_root;
		
		//s_logger.log(Level.INFO, "Will exit state: " + this.getClass().getName());
		
		root.queueEvent(new bhStateEvent(bhE_StateEventType.DID_EXIT, this));

		this.willExit();

		this.clean();
		
		if( this instanceof bhI_StateEventListener )
		{
			root.removeListener((bhI_StateEventListener) this);
		}
		
		root.processEventQueue();
	}
	
	protected void didEnter(bhA_StateConstructor constructor) {}
	protected void didForeground(Class<? extends bhA_State> revealingState, Object[] argsFromRevealingState){ }
	protected void update(double timeStep) {}
	protected void willBackground(Class<? extends bhA_State> blockingState) {}
	protected void willExit() { }
	
	public static <T extends bhA_State> T getEnteredInstance(Class<? extends bhA_State> T)
	{
		bhA_State registeredState = s_stateRegistry.get(T);
		if ( registeredState != null )
		{
			if ( registeredState.m_isEntered )
			{
				return (T) registeredState;
			}
		}
		
		return null;
	}
	
	public static boolean isForegrounded(Class<? extends bhA_State> T)
	{
		return getForegroundedInstance(T) != null;
	}
	
	public static boolean isEntered(Class<? extends bhA_State> T)
	{
		return getEnteredInstance(T) != null;
	}
	
	public static void register(bhA_State state)
	{
		s_stateRegistry.put(state.getClass(), state);
	}
	
	public static <T extends bhA_State> T getForegroundedInstance(Class<? extends bhA_State> T)
	{
		bhA_State registeredState = s_stateRegistry.get(T);
		if ( registeredState != null )
		{
			if ( registeredState.isForegrounded() )
			{
				return (T) registeredState;
			}
		}
		
		return null;
	}
	
	protected static bhA_State getInstance(Class<? extends bhA_State> T)
	{
		bhA_State registeredState = s_stateRegistry.get(T);
		if ( registeredState != null )
		{
			if ( registeredState.isEntered() )
			{
				bhU_Debug.ASSERT(false, "Tried to reuse state instance.");
			}
			
			return registeredState;
		}

		bhU_Debug.ASSERT(false, "No state instance registered.");
		
		return null;
	}
}