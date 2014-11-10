package swarm.shared.statemachine;

import java.util.ArrayList;

/**
 * 
 * @author dougkoellmer
 */
public abstract class A_State extends A_BaseStateObject implements I_StateArgForwarder
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
	private E_TransitionCause m_lastTransitionCause;

	//--- DRK > Tree/family relationships.
	A_State m_parent = null;
	A_State m_stateBeneath = null;
	private StateArgs m_args = null;
	Class<? extends A_State> m_previousState = null;
	Class<? extends A_State> m_blockingState = null;
	private Class<? extends A_Action_Base> m_lastActionPerformed = null;
	private ArrayList<A_Action_Base> m_queuedActionsToRegister = null;

	protected A_State()
	{
	}

	private void clean_common()
	{
		m_totalUpdateCount = 0;
		m_foregroundedUpdateCount = 0;
		m_blockingState = null;
		m_totalTimeInState = 0.0;
		m_foregroundedTimeInState = 0.0;
		m_isForegrounded = false;
		m_isEntered = false;
		m_args = StateArgs.DEFAULT;
	}

	private void clean_onExit()
	{
		m_parent = null;
		m_stateBeneath = null;
		m_previousState = null;
	}

	@Override public <T extends StateArgs> T getArgs()
	{
		return (T) m_args;
	}

	void onRegistered()
	{
		if(m_queuedActionsToRegister != null)
		{
			for(int i = 0; i < m_queuedActionsToRegister.size(); i++)
			{
				register_private(m_queuedActionsToRegister.get(i));
			}

			m_queuedActionsToRegister = null;
		}
	}

	private void queueActionRegistration(A_Action_Base action)
	{
		m_queuedActionsToRegister = m_queuedActionsToRegister != null ? m_queuedActionsToRegister : new ArrayList<A_Action_Base>();
		m_queuedActionsToRegister.add(action);
	}

	private void register_private(A_Action_Base action)
	{
		m_context.register(action, this.getClass());
	}

	protected void register(A_Action_Base action)
	{
		if(m_context == null)
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

	public E_TransitionCause getLastTransitionCause()
	{
		return m_lastTransitionCause;
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

	public Class<? extends A_Action_Base> getLastActionPerformed()
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

	boolean performAction(Class<? extends A_Action_Base> T, StateArgs args)
	{
		A_Action_Base action = m_context.getActionInstance(T);

		if(action == null)
		{
			return false;
		}

		args = defaultArgs(args);
		boolean performable = this.isPerformable_private(action, args);

		if(!performable)
		{
			return false;
		}
		else
		{
			m_lastActionPerformed = T;

			StateContext context = m_context;

			action.m_state = this;
			ActionEvent event = getContext().getEventPool().checkOutActionEvent(this, action);
			context.queueEvent(event);
			{
				args = action.prePerform(args);
				args = defaultArgs(args);
				event.setArgsIn(args);

				if(action instanceof A_Action)
				{
					((A_Action) action).perform(args);
				}
				else if(action instanceof A_Action_Returning)
				{
					StateArgs returnedArgs = ((A_Action_Returning) action).perform(args);
					returnedArgs = defaultArgs(returnedArgs);

					event.setReturnedArgs(returnedArgs);
				}
			}
			action.m_state = null;

			context.processEventQueue();

			return true;
		}
	}

	public boolean isPerformable(Class<? extends A_Action_Base> T, StateArgs args)
	{
		return this.isPerformable_private(m_context.getActionInstance(T), args);
	}

	private boolean isPerformable_private(A_Action_Base action, StateArgs args)
	{
		if(!action.isAssociatedWithState(this.getClass()))
			return false;

		if(!this.isEntered())
		{
			return false;
		}

		if(!m_isForegrounded)
		{
			if(!action.isPerformableInBackground())
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

	void didEnter_internal(StateArgs args, E_TransitionCause cause)
	{
		StateContext root = m_context;

		if(this instanceof I_StateEventListener)
		{
			root.addListener((I_StateEventListener) this);
		}

		this.clean_common();

		m_isEntered = true;

		root.queueEvent(getContext().getEventPool().checkOutEnterEvent(this, E_Event.DID_ENTER, cause));

		m_args = defaultArgs(args);
		m_lastTransitionCause = cause;

		this.didEnter();

		root.processEventQueue();
	}

	void didForeground_internal(Class<? extends A_State> revealingState, StateArgs args)
	{
		m_isForegrounded = true;

		m_foregroundedTimeInState = 0.0;
		m_foregroundedUpdateCount = 0;
		m_blockingState = null;

		StateContext context = m_context;

		context.queueEvent(getContext().getEventPool().checkOutForegroundEvent(this, revealingState, args));

		this.didForeground(revealingState, args);

		context.processEventQueue();
	}

	void update_internal(double timeStep)
	{
		StateContext context = m_context;

		if(!m_isEntered)
			return;

		m_totalTimeInState += timeStep;

		if(m_isForegrounded)
		{
			m_foregroundedTimeInState += timeStep;
			m_foregroundedUpdateCount++;
		}

		m_lastTimeStep = timeStep;

		m_totalUpdateCount++;

		context.queueEvent(getContext().getEventPool().checkOutUpdateEvent(this));

		this.update(timeStep);

		context.processEventQueue();
	}

	void willBackground_internal(Class<? extends A_State> blockingState)
	{
		StateContext context = m_context;

		context.queueEvent(getContext().getEventPool().checkOutBackgroundEvent(this, blockingState));

		m_blockingState = blockingState;

		this.willBackground(blockingState);

		m_isForegrounded = false;

		context.processEventQueue();

		m_foregroundedTimeInState = 0.0f;
		m_foregroundedUpdateCount = 0;
	}

	void willExit_internal(E_TransitionCause cause)
	{
		StateContext root = m_context;

		root.queueEvent(getContext().getEventPool().checkOutExitEvent(this, cause));

		m_lastTransitionCause = cause;

		this.willExit();

		this.clean_common();
		this.clean_onExit();

		if(this instanceof I_StateEventListener)
		{
			root.removeListener((I_StateEventListener) this);
		}

		root.processEventQueue();
	}

	//--- DRK > Bunch of event callbacks that you can override.
	protected void didEnter()
	{
	}

	protected void didForeground(Class<? extends A_State> revealingState_nullable, StateArgs argsFromRevealingState)
	{
	}

	protected void update(double timeStep)
	{
	}

	protected void willBackground(Class<? extends A_State> blockingState_nullable)
	{
	}

	protected void willExit()
	{
	}

	@Override public <T extends Object> T getArg(int index)
	{
		return getArgs().get(index);
	}

	@Override public <T extends Object> T getArg()
	{
		return getArgs().get();
	}

	@Override public <T extends Object> T getArg(Class<T> paramType)
	{
		return getArgs().get(paramType);
	}

	@Override public <T extends Object> T getArg(Class<T> paramType, int index)
	{
		return getArgs().get(paramType, index);
	}
}