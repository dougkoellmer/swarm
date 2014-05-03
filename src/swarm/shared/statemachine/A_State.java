package swarm.shared.statemachine;

import java.util.ArrayList;


/**
 * ...
 * @author 
 */
public abstract class A_State extends A_BaseStateObject
{
	//--- DRK > Time and update count trackers.
	private int m_foregroundedUpdateCount = 0;
	private int m_totalUpdateCount = 0;
	private double m_totalTimeInState = 0.0;
	private double m_foregroundedTimeInState = 0.0;
	private double m_lastTimeStep = 0;
	
	//--- DRK > State lifecycle trackers.
	private boolean m_isForegrounded = false;
	private boolean m_isEntered = false;
	
	//--- DRK > Tree/family relationships.
	A_State m_parent = null;
	A_State m_stateBeneath = null;
	Class<? extends A_State> m_previousState = null;
	Class<? extends A_State> m_blockingState = null;
	private Class<? extends A_Action> m_lastActionPerformed = null;
	private ArrayList<A_Action> m_queuedActionsToRegister = null;
	
	protected A_State() 
	{
	}
	
	private void clean()
	{
		m_totalUpdateCount = 0;
		m_foregroundedUpdateCount = 0;
		m_blockingState = null;
		m_totalTimeInState = 0.0;
		m_foregroundedTimeInState = 0.0;
		m_isForegrounded = false;
		m_isEntered = false;
	}
	
	void onRegistered()
	{
		if( m_queuedActionsToRegister != null )
		{
			for( int i = 0; i < m_queuedActionsToRegister.size(); i++ )
			{
				register_private(m_queuedActionsToRegister.get(i));
			}
			
			m_queuedActionsToRegister = null;
		}
	}
	
	private void queueActionRegistration(A_Action action)
	{
		m_queuedActionsToRegister = m_queuedActionsToRegister != null ? m_queuedActionsToRegister : new ArrayList<A_Action>();
		m_queuedActionsToRegister.add(action);
	}
	
	private void register_private(A_Action action)
	{
		m_context.register(action, this.getClass());
	}
	
	protected void register(A_Action action)
	{
		if( m_context == null )
		{
			queueActionRegistration(action);
		}
		else
		{
			register_private(action);
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
	
	public boolean isEntered()
	{
		return m_isEntered;
	}
	
	public int getForegroundedUpdateCount()
	{
		return m_foregroundedUpdateCount;
	}
	
	public int getUpdateCount()
	{
		return m_totalUpdateCount;
	}
	
	public double getTotalTimeInState()
	{
		return m_totalTimeInState;
	}
	
	public double getForegroundedTimeInState()
	{
		return m_foregroundedTimeInState;
	}
	
	public double getBackgroundedTimeInState()
	{
		return m_totalTimeInState - m_foregroundedTimeInState;
	}
	
	public Class<? extends A_State> getBlockingState()
	{
		return m_blockingState;
	}
	
	public double getLastTimeStep()
	{
		return m_lastTimeStep;
	}
	
	public Class<? extends A_Action> getLastActionPerformed()
	{
		return m_lastActionPerformed;
	}
	
	public <T extends A_State> T getParent()
	{
		return (T) m_parent;
	}
	
	public A_State getStateBeneath()
	{
		return m_stateBeneath;
	}
	
	public Class<? extends A_State> getPreviousState()
	{
		return m_previousState;
	}
	
	boolean performAction(Class<? extends A_Action> T, StateArgs args)
	{
		A_Action action = m_context.getAction(T);

		if ( action == null )
		{
			return false;
		}
		
		boolean performable = this.isPerformable_private(action, args);
		
		if( !performable )
		{
			return false;
		}
		else
		{
			m_lastActionPerformed = T;
			
			StateContext context = m_context;

			action.m_state = this;
			context.queueEvent(new StateEvent(this, action, args));
			{
				//A_Action.s_actionStack.add(action);
				
				action.prePerform(args);
				
				action.perform(args);
			
				//A_Action.s_actionStack.remove(smA_Action.s_actionStack.size()-1);
			}
			action.m_state = null;

			context.processEventQueue();
			
			return true;
		}
	}
	
	public boolean isPerformable(Class<? extends A_Action> T, StateArgs args)
	{
		return this.isPerformable_private(m_context.getAction(T), args);
	}
	
	private boolean isPerformable_private(A_Action action, StateArgs args)
	{
		if( !action.isAssociatedWithState(this.getClass()) )  return false;
		
		if ( !this.isEntered() )
		{
			return false;
		}
		
		if( !m_isForegrounded )
		{
			if ( !action.isPerformableInBackground() )
			{
				return false;
			}
		}
		
		boolean isPerformable;
		action.m_state = this;
		{
			isPerformable = action.isPerformable(args);
		}
		action.m_state = null;
		
		return isPerformable;
	}
	
	void didEnter_internal(StateArgs constructor)
	{
		StateContext root = m_context;
		
		if( this instanceof I_StateEventListener )
		{
			root.addListener((I_StateEventListener) this);
		}
		
		this.clean();
		
		m_isEntered = true;
		
		root.queueEvent(new StateEvent(E_StateEventType.DID_ENTER, this));

		this.didEnter(constructor);
		
		root.processEventQueue();
	}
	
	void didForeground_internal(Class<? extends A_State> revealingState, Object[] args)
	{
		m_isForegrounded = true;
		
		m_foregroundedTimeInState = 0.0;
		m_foregroundedUpdateCount = 0;
		m_blockingState = null;
		
		StateContext context = m_context;
		
		context.queueEvent(new StateEvent(E_StateEventType.DID_FOREGROUND, this));
		
		this.didForeground(revealingState, args);
		
		context.processEventQueue();
	}
	
	void update_internal(double timeStep)
	{
		StateContext context = m_context;
		
		if( !m_isEntered )  return;
		
		m_totalTimeInState += timeStep;
		
		if( m_isForegrounded )
		{
			m_foregroundedTimeInState += timeStep;
			m_foregroundedUpdateCount++;
		}
		
		context.queueEvent(new StateEvent(E_StateEventType.DID_UPDATE, this));
		
		m_lastTimeStep = timeStep;
		
		m_totalUpdateCount++;
		
		this.update(timeStep);
		
		context.processEventQueue();
	}
	
	void willBackground_internal(Class<? extends A_State> blockingState)
	{
		StateContext context = m_context;
		
		context.queueEvent(new StateEvent(E_StateEventType.DID_BACKGROUND, this, blockingState));
		
		m_blockingState = blockingState;
		
		this.willBackground(blockingState);
		
		m_isForegrounded = false;
		
		context.processEventQueue();
		
		m_foregroundedTimeInState = 0.0f;
		m_foregroundedUpdateCount = 0;
	}
	
	void willExit_internal()
	{
		StateContext root = m_context;
		
		root.queueEvent(new StateEvent(E_StateEventType.DID_EXIT, this));

		this.willExit();

		this.clean();
		
		if( this instanceof I_StateEventListener )
		{
			root.removeListener((I_StateEventListener) this);
		}
		
		root.processEventQueue();
	}
	
	//--- DRK > Bunch of event callbacks that you can override.
	protected void didEnter(StateArgs constructor) {}
	protected void didForeground(Class<? extends A_State> revealingState, Object[] argsFromRevealingState){ }
	protected void update(double timeStep) {}
	protected void willBackground(Class<? extends A_State> blockingState) {}
	protected void willExit() { }
}