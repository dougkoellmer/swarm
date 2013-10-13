package swarm.shared.statemachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.shared.debugging.smU_Debug;


/**
 * ...
 * @author 
 */
public abstract class smA_State extends smA_BaseStateObject
{
	private static final Logger s_logger = Logger.getLogger(smA_State.class.getName());
	
	private float m_totalTimeInState = 0.0f;
	private float m_foregroundedTimeInState = 0.0f;
	
	private boolean m_isForegrounded = false;
	private boolean m_isEntered = false;
	
	private boolean m_isEntering = false;
	private boolean m_isForegrounding = false;
	
	smA_State m_stateBeneath = null;
	
	private HashMap<Class<? extends smA_Action>, Boolean> m_isPerformableOverrides = new HashMap<Class<? extends smA_Action>, Boolean>();
	
	smA_State m_parent = null;
	
	Class<? extends smA_State> m_previousState = null;
	Class<? extends smA_State> m_blockingState = null;
	
	private Class<? extends smA_Action> m_lastActionPerformed = null;
	
	private double m_lastTimeStep = 0;
	
	private int m_foregroundedUpdateCount = 0;
	private int m_totalUpdateCount = 0;
	
	private ArrayList<smA_Action> m_queuedActionsToRegister = null;
	
	protected smA_State() 
	{
	}
	
	void onRegistered()
	{
		if( m_queuedActionsToRegister != null )
		{
			for( int i = 0; i < m_queuedActionsToRegister.size(); i++ )
			{
				registerAction_private(m_queuedActionsToRegister.get(i));
			}
			
			m_queuedActionsToRegister = null;
		}
	}
	
	protected void queueActionRegistration(smA_Action action)
	{
		m_queuedActionsToRegister = m_queuedActionsToRegister != null ? m_queuedActionsToRegister : new ArrayList<smA_Action>();
		m_queuedActionsToRegister.add(action);
	}
	
	private void registerAction_private(smA_Action action)
	{
		m_context.registerAction(this.getClass(), action);
	}
	
	protected void registerAction(smA_Action action)
	{
		if( m_context == null )
		{
			queueActionRegistration(action);
		}
		else
		{
			registerAction_private(action);
		}
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
	
	public Class<? extends smA_State> getBlockingState()
	{
		return m_blockingState;
	}
	
	public int getForegroundedUpdateCount()
	{
		return m_foregroundedUpdateCount;
	}
	
	public double getLastTimeStep()
	{
		return m_lastTimeStep;
	}
	
	public Class<? extends smA_State> getPreviousState()
	{
		return m_previousState;
	}
	
	public Class<? extends smA_Action> getLastActionPerformed()
	{
		return m_lastActionPerformed;
	}
	
	public <T extends smA_State> T getParent()
	{
		return (T) m_parent;
	}
	
	public smA_State getStateBeneath()
	{
		return m_stateBeneath;
	}
	
	public float getTimeInState(smE_StateTimeType eTimeType)
	{
		eTimeType = eTimeType == null ? smE_StateTimeType.TOTAL : eTimeType;
		
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
		m_blockingState = null;
		m_totalTimeInState = 0.0f;
		m_foregroundedTimeInState = 0.0f;
		m_isForegrounded = false;
		m_isEntered = false;
		m_totalUpdateCount = 0;
		m_foregroundedUpdateCount = 0;

		m_isPerformableOverrides.clear();
	}
	
	protected void setPerformableOverride(Class<? extends smA_Action> T, boolean perfomable)
	{
		smU_Debug.ASSERT(m_parent != null );
		smU_Debug.ASSERT(m_isEntered);
		
		m_isPerformableOverrides.put(T, perfomable);
	}
	
	public boolean performAction(Class<? extends smA_Action> T)
	{
		return this.performAction(T, null);
	}
	
	public boolean performAction(Class<? extends smA_Action> T, smA_ActionArgs args)
	{
		smA_Action action = m_context.getAction(T);

		if ( action == null )
		{
			return false;
		}
		
		if( !action.suppressLog() )
		{
			//s_logger.log(Level.INFO, "Will perform action: " + action.getClass().getName());
		}
		
		boolean performable = this.isActionPerformable_private(action, args);
		
		if( !performable )
		{				
			//s_logger.log(Level.INFO, "Action not perfomable: " + action.getClass().getName());
			
			return false;
		}
		else
		{
			this.m_lastActionPerformed = T;
			
			smStateContext context = m_context;

			action.m_state = this;
			context.queueEvent(new smStateEvent(this, action, args));
			{
				//smA_Action.s_actionStack.add(action);
				
				action.prePerform(args);
				
				action.perform(args);
			
				//smA_Action.s_actionStack.remove(smA_Action.s_actionStack.size()-1);
			}
			action.m_state = null;

			context.processEventQueue();
			
			return true;
		}
	}
	
	public boolean isActionPerfomable(Class<? extends smA_Action> T, smA_ActionArgs args)
	{
		return this.isActionPerformable_private(m_context.getAction(T), args);
	}
	
	private boolean isActionPerformable_private(smA_Action action, smA_ActionArgs args)
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
	
	private boolean isActionLegal(smA_Action action)
	{
		if ( !this.m_isEntered )
		{
			smU_Debug.ASSERT(false);
			return false;
		}
		
		if( !this.m_isForegrounded )
		{
			if ( !action.isPerformableInBackground() )
			{
				return false;
			}
		}
		
		Class<? extends smA_State> actionState = action.getStateAssociation();

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
	
	
	
	
	void didEnter_internal(smA_StateConstructor constructor)
	{
		smStateContext root = m_context;
		
		if( this instanceof smI_StateEventListener )
		{
			root.addListener((smI_StateEventListener) this);
		}
		
		this.clean();
		
		m_isEntered = true;
		
		//s_logger.log(Level.INFO, "Will enter state: " + this.getClass().getName());
		
		root.queueEvent(new smStateEvent(smE_StateEventType.DID_ENTER, this));
		
		m_isEntering = true;
		
		this.didEnter(constructor);
		
		m_isEntering = false;
		
		root.processEventQueue();
	}
	
	void didForeground_internal(Class<? extends smA_State> revealingState, Object[] args)
	{
		m_isForegrounded = true;
		
		m_foregroundedTimeInState = 0.0f;
		m_foregroundedUpdateCount = 0;
		m_blockingState = null;
		
		smStateContext context = m_context;
		
		//s_logger.log(Level.INFO, "Will foreground state: " + this.getClass().getName());
		
		context.queueEvent(new smStateEvent(smE_StateEventType.DID_FOREGROUND, this));
		
		m_isForegrounding = true;
		
		this.didForeground(revealingState, args);
		
		m_isForegrounding = false;
		
		context.processEventQueue();
	}
	
	void update_internal(double timeStep)
	{
		smStateContext context = m_context;
		
		smU_Debug.ASSERT(m_isEntered, "smA_State::update1");
		
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
		
		context.queueEvent(new smStateEvent( smE_StateEventType.DID_UPDATE, this));
		
		m_lastTimeStep = timeStep;
		
		m_totalUpdateCount++;
		
		this.update(timeStep);
		
		context.processEventQueue();
	}
	
	void willBackground_internal(Class<? extends smA_State> blockingState)
	{
		smStateContext context = m_context;
		
		//s_logger.log(Level.INFO, "Will background state: " + this.getClass().getName());
		
		context.queueEvent(new smStateEvent(smE_StateEventType.DID_BACKGROUND, this, blockingState));
		
		this.m_blockingState = blockingState;
		
		this.willBackground(blockingState);
		
		m_isForegrounded = false;
		
		context.processEventQueue();
		
		m_foregroundedTimeInState = 0.0f;
		m_foregroundedUpdateCount = 0;
	}
	
	void willExit_internal()
	{
		smStateContext root = m_context;
		
		//s_logger.log(Level.INFO, "Will exit state: " + this.getClass().getName());
		
		root.queueEvent(new smStateEvent(smE_StateEventType.DID_EXIT, this));

		this.willExit();

		this.clean();
		
		if( this instanceof smI_StateEventListener )
		{
			root.removeListener((smI_StateEventListener) this);
		}
		
		root.processEventQueue();
	}
	
	protected void didEnter(smA_StateConstructor constructor) {}
	protected void didForeground(Class<? extends smA_State> revealingState, Object[] argsFromRevealingState){ }
	protected void update(double timeStep) {}
	protected void willBackground(Class<? extends smA_State> blockingState) {}
	protected void willExit() { }
}