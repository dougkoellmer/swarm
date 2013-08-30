package swarm.client.app;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.google.gwt.logging.client.TextLogFormatter;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;

import swarm.client.code.smClientCodeCompiler;
import swarm.client.entities.smCamera;
import swarm.client.input.smClickManager;
import swarm.client.managers.smCameraManager;
import swarm.client.managers.smCellAddressManager;
import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smGridManager;
import swarm.client.managers.smUserManager;
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
import swarm.client.structs.smCellCodeCache;
import swarm.client.thirdparty.captcha.smRecaptchaWrapper;
import swarm.client.thirdparty.json.smGwtJsonFactory;
import swarm.client.thirdparty.transaction.smGwtRequestDispatcher;
import swarm.client.time.smU_Time;
import swarm.client.transaction.smClientTransactionManager;
import swarm.client.transaction.smInlineRequestDispatcher;
import swarm.client.view.smE_ZIndex;
import swarm.client.view.smS_UI;
import swarm.client.view.smViewConfig;
import swarm.client.view.smViewController;
import swarm.client.view.tabs.code.smCellSandbox;
import swarm.client.view.tooltip.smE_ToolTipType;
import swarm.client.view.tooltip.smToolTipManager;
import swarm.shared.smE_AppEnvironment;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.app.smA_App;
import swarm.shared.app.smS_App;
import swarm.shared.debugging.smI_AssertionDelegate;
import swarm.shared.debugging.smTelemetryAssert;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateMachine;
import swarm.shared.time.smI_TimeSource;
import swarm.shared.transaction.smE_RequestPath;
import swarm.shared.transaction.smE_TelemetryRequestPath;
import swarm.shared.transaction.smRequestPathManager;

public class smA_ClientApp extends smA_App implements smI_TimeSource
{
	private static final Logger s_logger = Logger.getLogger(smA_ClientApp.class.getName());	
	
	private double m_lastTime = 0;
	
	protected final smClientAppConfig m_appConfig;
	protected final smViewConfig m_viewConfig;
	protected final smAppContext m_module;
	
	protected smA_ClientApp(smClientAppConfig appConfig, smViewConfig viewConfig)
	{
		super(smE_AppEnvironment.CLIENT);
		
		m_appConfig = appConfig;
		m_viewConfig = viewConfig;
		m_module = new smAppContext();
	}
	
	protected void startUp(smE_StartUpStage stage)
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
		
		smE_StartUpStage nextStage = stage.getNext();
		
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
		
		smU_Debug.setDelegate(new smI_AssertionDelegate()
		{
			@Override
			public void doAssert(String message)
			{
				//--- DRK > Asserts are sometimes pretty silent, so this lets you know that eclipse has at least a stack trace for you if you're lucky.
				//Window.alert("ASSERTION FAILED: " + message);
				
				s_logger.severe("ASSERTION FAILED: " + message);
				
				String platform = m_module.platformInfo.getRawPlatform();
				smTelemetryAssert telemetryAssert = new smTelemetryAssert(message, platform);
				m_module.txnMngr.makeRequest(smE_TelemetryRequestPath.logAssert, telemetryAssert);

				//assert(false);
			}
		});
	}
	
	protected void stage_browserSupportCheck()
	{
		m_module.cellSandbox = new smCellSandbox(new smCellSandbox.I_StartUpCallback()
		{
			public void onStartUpComplete(boolean success)
			{
				if( success )
				{
					startUp(smE_StartUpStage.CHECK_BROWSER_SUPPORT.getNext());
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
		m_module.platformInfo = new smPlatformInfo();
		m_module.jsonFactory = new smGwtJsonFactory(m_appConfig.verboseTransactions);
		m_module.codeCompiler = new smClientCodeCompiler();
		
		m_module.codeCache = new smCellCodeCache(m_appConfig.codeCacheSize, m_appConfig.codeCacheExpiration_seconds, this);
		m_module.userMngr = new smUserManager(m_module, m_appConfig.user);
		m_module.requestPathMngr = new smRequestPathManager(m_module.jsonFactory, m_appConfig.verboseTransactions);
		m_module.txnMngr = new smClientTransactionManager(m_module.requestPathMngr);
		m_module.gridMngr = new smGridManager(m_module.txnMngr, m_appConfig.grid);
		m_module.cameraMngr = new smCameraManager(m_module.gridMngr, new smCamera(), m_appConfig.minSnapTime, m_appConfig.maxSnapTime);
		m_module.addressMngr = new smCellAddressManager(m_module, m_appConfig.addressCacheSize, m_appConfig.addressCacheExpiration_seconds, this);
		m_module.accountMngr = new smClientAccountManager(m_module.txnMngr, m_module.jsonFactory);
		
		//--- DRK > Configure transaction stuff.
		m_module.requestPathMngr.register(smE_RequestPath.values());
		m_module.txnMngr.setSyncRequestDispatcher(new smInlineRequestDispatcher(m_appConfig.appId));
		m_module.txnMngr.setAsyncRequestDispatcher(new smGwtRequestDispatcher());
	}
	
	protected void stage_startViewManagers()
	{
		m_module.recaptchaWrapper = new smRecaptchaWrapper();
		m_module.clickMngr = new smClickManager();
		m_module.toolTipMngr = new smToolTipManager(m_module.platformInfo.getPlatform() != smE_Platform.IOS, smS_UI.TOOL_TIP_DELAY);
		
		//--- DRK > Set defaults for tool tips.
		for( int i = smE_ZIndex.TOOL_TIP_1.ordinal(), j = 0; i <= smE_ZIndex.TOOL_TIP_5.ordinal(); i++, j++ )
		{
			m_module.toolTipMngr.setDefaultZIndex(smE_ToolTipType.values()[j], i);
		}
		m_module.toolTipMngr.setDefaultPadding(smS_UI.TOOl_TIP_PADDING);
	}
	
	protected void stage_registerStateMachine()
	{
		smA_State.register(new StateMachine_Base());
		{
			smA_State.register(new State_Initializing());
			
			smA_State.register(new State_GenericDialog());
			smA_State.register(new State_AsyncDialog());
			
			smA_State.register(new StateContainer_Base());
			{
				smA_State.register(new StateMachine_Camera());
				{
					smA_State.register(new State_CameraFloating());
					smA_State.register(new State_GettingMapping());
					smA_State.register(new State_CameraSnapping(m_appConfig.cellHudHeight));
					smA_State.register(new State_ViewingCell());
				}
			}
		}
	}
	
	protected void stage_establishTiming()
	{
		//--- DRK > Get timing and update loop going.
		smU_Time.startUp();
		Timer timer = new Timer()
		{
			@Override
			public void run()
			{
				smA_ClientApp.this.update();				
			}
		};
		timer.scheduleRepeating(m_appConfig.framerate_milliseconds);
	}
	
	protected void stage_gunshotSound()
	{
		smA_StateMachine.root_didEnter(StateMachine_Base.class, m_viewConfig.stateEventListener);
		smA_StateMachine.root_didForeground(StateMachine_Base.class);
		
		m_module.txnMngr.flushSyncResponses();
	}

	protected static void registerCodeEditingStates()
	{
		smA_State.register(new StateMachine_EditingCode());
		{
			smA_State.register(new State_EditingCode());
			smA_State.register(new State_EditingCodeBlocker());
		}
	}
	
	protected static void registerAccountStates()
	{
		smA_State.register(new StateMachine_Account());
		{
			smA_State.register(new State_ManageAccount());
			smA_State.register(new State_AccountStatusPending());
			smA_State.register(new State_SignInOrUp());
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
		double currentTime = smU_Time.getSeconds();
		smA_StateMachine.root_didUpdate(StateMachine_Base.class, currentTime - m_lastTime);
		m_lastTime = currentTime;

		m_module.txnMngr.flushSyncResponses();
	}

	@Override
	public double getTime()
	{
		return m_lastTime;
	}
}
