package swarm.client.app;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.google.gwt.logging.client.TextLogFormatter;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;

import swarm.client.code.bhClientCodeCompiler;
import swarm.client.input.bhClickManager;
import swarm.client.managers.bhCellAddressManager;
import swarm.client.managers.bhClientAccountManager;
import swarm.client.managers.bhGridManager;
import swarm.client.managers.bhUserManager;
import swarm.client.states.StateContainer_Base;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.StateMachine_Tabs;
import swarm.client.states.State_AsyncDialog;
import swarm.client.states.State_GenericDialog;
import swarm.client.states.State_Initializing;
import swarm.client.states.account.StateMachine_Account;
import swarm.client.states.account.State_AccountStatusPending;
import swarm.client.states.account.State_ManageAccount;
import swarm.client.states.account.State_SignInOrUp;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_CameraFloating;
import swarm.client.states.camera.State_CameraSnapping;
import swarm.client.states.camera.State_GettingMapping;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.states.code.StateMachine_EditingCode;
import swarm.client.states.code.State_EditingCode;
import swarm.client.states.code.State_EditingCodeBlocker;
import swarm.client.structs.bhCellCodeCache;
import swarm.client.thirdparty.captcha.bhRecaptchaWrapper;
import swarm.client.thirdparty.json.bhGwtJsonFactory;
import swarm.client.thirdparty.transaction.bhGwtRequestDispatcher;
import swarm.client.time.bhU_Time;
import swarm.client.transaction.bhClientTransactionManager;
import swarm.client.transaction.bhInlineRequestDispatcher;
import swarm.client.ui.bhE_ZIndex;
import swarm.client.ui.bhS_UI;
import swarm.client.ui.bhViewConfig;
import swarm.client.ui.bhViewController;
import swarm.client.ui.tabs.code.bhCellSandbox;
import swarm.client.ui.tooltip.bhE_ToolTipType;
import swarm.client.ui.tooltip.bhToolTipManager;
import swarm.shared.bhE_AppEnvironment;
import swarm.shared.app.sm;
import swarm.shared.app.bhA_App;
import swarm.shared.app.bhS_App;
import swarm.shared.debugging.bhI_AssertionDelegate;
import swarm.shared.debugging.bhTelemetryAssert;
import swarm.shared.debugging.bhU_Debug;
import swarm.shared.statemachine.bhA_State;
import swarm.shared.statemachine.bhA_StateMachine;
import swarm.shared.time.bhI_TimeSource;
import swarm.shared.transaction.bhE_RequestPath;
import swarm.shared.transaction.bhE_TelemetryRequestPath;
import swarm.shared.transaction.bhRequestPathManager;

public class bhA_ClientApp extends bhA_App implements bhI_TimeSource
{
	private static final Logger s_logger = Logger.getLogger(bhA_ClientApp.class.getName());	
	
	private double m_lastTime = 0;
	
	protected final bhClientAppConfig m_appConfig;
	protected final bhViewConfig m_viewConfig;
	
	protected bhA_ClientApp(bhClientAppConfig appConfig, bhViewConfig viewConfig)
	{
		super(bhE_AppEnvironment.CLIENT);
		
		m_appConfig = appConfig;
		m_viewConfig = viewConfig;
		
		sm_c.app = this;
	}
	
	protected void startUp(bhE_StartUpStage stage)
	{
		switch(stage)
		{
			case CHECK_BROWSER_SUPPORT:// this case goes async, so can't recurse immediately.
			{
				stage_browserSupportCheck();		return;
			}
			
			case CONFIGURE_LOGGING:
			{
				stage_configureLogging();			break;
			}
			
			case LOAD_SUPPORT_LIBRARIES:
			{
				stage_loadSupportLibraries();		break;
			}
			
			case START_APP_MANAGERS:
			{
				stage_startAppManagers();			break;
			}
			
			case START_VIEW_MANAGERS:
			{
				stage_startViewManagers();			break;
			}
			
			case REGISTER_STATES:
			{
				stage_registerStateMachine();		break;
			}
			
			case ESTABLISH_TIMING:
			{
				stage_establishTiming();			break;
			}
			
			case GUNSHOT_SOUND:
			{
				stage_gunshotSound();				break;
			}
		};
		
		bhE_StartUpStage nextStage = stage.getNext();
		
		if( nextStage != null )
		{
			startUp(nextStage);
		}
	}
	
	protected void stage_configureLogging()
	{
		//--- DRK > Format console logging messages.
		Logger.getLogger("b33hive.shared.statemachine").setLevel(Level.OFF);
		Handler[] handlers = Logger.getLogger("").getHandlers();
		for (Handler h : handlers)
		{
		    h.setFormatter(new TextLogFormatter(false)
		    {
		        @Override
		        public String format(LogRecord event)
		        {
		            return event.getMessage();
		        }
		    });
		}
		
		bhU_Debug.setDelegate(new bhI_AssertionDelegate()
		{
			@Override
			public void doAssert(String message)
			{
				//--- DRK > Asserts are sometimes pretty silent, so this lets you know that eclipse has at least a stack trace for you if you're lucky.
				//Window.alert("ASSERTION FAILED: " + message);
				
				s_logger.severe("ASSERTION FAILED: " + message);
				
				String platform = sm_c.platformInfo.getRawPlatform();
				bhTelemetryAssert telemetryAssert = new bhTelemetryAssert(message, platform);
				sm_c.txnMngr.makeRequest(bhE_TelemetryRequestPath.logAssert, telemetryAssert);

				//assert(false);
			}
		});
	}
	
	protected void stage_browserSupportCheck()
	{
		sm_c.cellSandbox = new bhCellSandbox(new bhCellSandbox.I_StartUpCallback()
		{
			public void onStartUpComplete(boolean success)
			{
				if( success )
				{
					startUp(bhE_StartUpStage.CHECK_BROWSER_SUPPORT.getNext());
				}
				else
				{
					//--- DRK > For some reason we have to do a short setTimeout before modifying the
					//---		DOM here, at least in debug mode, to avoid an exception. The exception
					//---		gets fired *after* the DOM is successfully changed, so we could just let it
					//---		go, but just being pedantic and avoiding it here.
					showUnsupportedBrowserError();
				}
			}
		}, m_appConfig.appId);
	}
	
	protected void stage_loadSupportLibraries()
	{
		if( m_appConfig.publicRecaptchaKey != null )
		{
			//loadRecaptcha(m_appConfig.publicRecaptchaKey);
		}
	}
	
	private native void loadRecaptcha(String publicKey)
	/*-{
		var body= $doc.getElementsByTagName('body')[0];
		
		var script= $doc.createElement('script');
		script.type= 'text/javascript';
		script.src = 'http://www.google.com/recaptcha/api/challenge?k=' + publicKey;
		body.appendChild(script);
	}-*/;
	
	protected void stage_startAppManagers()
	{
		sm_c.platformInfo = new bhPlatformInfo();
		sm.jsonFactory = new bhGwtJsonFactory(m_appConfig.verboseTransactions);
		sm.codeCompiler = new bhClientCodeCompiler();
		sm_c.addressMngr = new bhCellAddressManager(m_appConfig.addressCacheSize, m_appConfig.addressCacheExpiration_seconds, this);
		sm_c.accountMngr = new bhClientAccountManager();
		sm_c.codeCache = new bhCellCodeCache(m_appConfig.codeCacheSize, m_appConfig.codeCacheExpiration_seconds, this);
		sm_c.userMngr = new bhUserManager(sm_c.accountMngr, sm_c.codeCache, m_appConfig.user);
		sm_c.gridMngr = new bhGridManager(m_appConfig.grid);
		sm.requestPathMngr = new bhRequestPathManager(sm.jsonFactory, m_appConfig.verboseTransactions);
		sm_c.txnMngr = new bhClientTransactionManager(sm.requestPathMngr);
		
		//--- DRK > Configure transaction stuff.
		sm_c.requestPathMngr.register(bhE_RequestPath.values());
		sm_c.txnMngr.setSyncRequestDispatcher(new bhInlineRequestDispatcher(m_appConfig.appId));
		sm_c.txnMngr.setAsyncRequestDispatcher(new bhGwtRequestDispatcher());
	}
	
	protected void stage_startViewManagers()
	{
		sm_c.recaptchaWrapper = new bhRecaptchaWrapper();
		sm_c.clickMngr = new bhClickManager();
		sm_c.toolTipMngr = new bhToolTipManager(sm_c.platformInfo.getPlatform() != bhE_Platform.IOS, bhS_UI.TOOL_TIP_DELAY);
		
		//--- DRK > Set defaults for tool tips.
		for( int i = bhE_ZIndex.TOOL_TIP_1.ordinal(), j = 0; i <= bhE_ZIndex.TOOL_TIP_5.ordinal(); i++, j++ )
		{
			sm_c.toolTipMngr.setDefaultZIndex(bhE_ToolTipType.values()[j], i);
		}
		sm_c.toolTipMngr.setDefaultPadding(bhS_UI.TOOl_TIP_PADDING);
	}
	
	protected void stage_registerStateMachine()
	{
		bhA_State.register(new StateMachine_Base());
		{
			bhA_State.register(new State_Initializing());
			
			bhA_State.register(new State_GenericDialog());
			bhA_State.register(new State_AsyncDialog());
			
			bhA_State.register(new StateContainer_Base());
			{
				bhA_State.register(new StateMachine_Camera(m_appConfig.minSnapTime, m_appConfig.maxSnapTime));
				{
					bhA_State.register(new State_CameraFloating());
					bhA_State.register(new State_GettingMapping());
					bhA_State.register(new State_CameraSnapping(m_appConfig.cellHudHeight));
					bhA_State.register(new State_ViewingCell());
				}
			}
		}
	}
	
	protected void stage_establishTiming()
	{
		//--- DRK > Get timing and update loop going.
		bhU_Time.startUp();
		Timer timer = new Timer()
		{
			@Override
			public void run()
			{
				bhA_ClientApp.this.update();				
			}
		};
		timer.scheduleRepeating(m_appConfig.framerate_milliseconds);
	}
	
	protected void stage_gunshotSound()
	{
		bhA_StateMachine.root_didEnter(StateMachine_Base.class, m_viewConfig.stateEventListener);
		bhA_StateMachine.root_didForeground(StateMachine_Base.class);
		
		sm_c.txnMngr.flushSyncResponses();
	}

	protected static void registerCodeEditingStates()
	{
		bhA_State.register(new StateMachine_EditingCode());
		{
			bhA_State.register(new State_EditingCode());
			bhA_State.register(new State_EditingCodeBlocker());
		}
	}
	
	protected static void registerAccountStates()
	{
		bhA_State.register(new StateMachine_Account());
		{
			bhA_State.register(new State_ManageAccount());
			bhA_State.register(new State_AccountStatusPending());
			bhA_State.register(new State_SignInOrUp());
		}
	}
	
	protected native void showUnsupportedBrowserError()
	/*-{
			function showError()
			{
				var unsupportedHtml =	"<table class='sm_unsupported_platform_font' style='width:100%; height:100%;'><tr><td style='vertical-align:middle; text-align:center;'>" +
										"<img style='width:280px; height:255px;' src='/r.img/b33.png'/><br><br><br>" +
										"Please upgrade to the latest version of your browser in order to access b33hive." +
										"</td></tr></table>";
				$doc.body.innerHTML = unsupportedHtml;
			}
			
			setTimeout(showError, 0);
 	}-*/;
	
	public void update()
	{
		double currentTime = bhU_Time.getSeconds();
		bhA_StateMachine.root_didUpdate(StateMachine_Base.class, currentTime - m_lastTime);
		m_lastTime = currentTime;

		sm_c.txnMngr.flushSyncResponses();
	}

	@Override
	public double getTime()
	{
		return m_lastTime;
	}
}
