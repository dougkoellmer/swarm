package swarm.client.app;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.google.gwt.logging.client.TextLogFormatter;
import com.google.gwt.user.client.Timer;

import swarm.client.code.smClientCodeCompiler;
import swarm.client.entities.smCamera;
import swarm.client.input.smClickManager;
import swarm.client.managers.smCameraManager;
import swarm.client.managers.smCellAddressManager;
import swarm.client.managers.smCellBufferManager;
import swarm.client.managers.smCellCodeManager;
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
import swarm.client.view.smViewContext;
import swarm.client.view.smViewController;
import swarm.client.view.sandbox.smSandboxManager;
import swarm.client.view.tooltip.smE_ToolTipType;
import swarm.client.view.tooltip.smToolTipManager;
import swarm.shared.smE_AppEnvironment;
import swarm.shared.account.smSignInValidator;
import swarm.shared.account.smSignUpValidator;
import swarm.shared.app.smSharedAppContext;
import swarm.shared.app.smA_App;
import swarm.shared.app.smS_App;
import swarm.shared.debugging.smI_AssertionDelegate;
import swarm.shared.debugging.smTelemetryAssert;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.statemachine.smA_State;
import swarm.shared.statemachine.smA_StateMachine;
import swarm.shared.statemachine.smI_StateEventListener;
import swarm.shared.statemachine.smStateContext;
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
	protected final smAppContext m_appContext;
	protected final smViewContext m_viewContext;
	protected smStateContext m_stateContext;
	
	protected smA_ClientApp(smClientAppConfig appConfig, smViewConfig viewConfig)
	{
		super(smE_AppEnvironment.CLIENT);
		
		m_appConfig = appConfig;
		m_viewConfig = viewConfig;
		m_appContext = new smAppContext();
		m_viewContext = new smViewContext();
		m_viewContext.appConfig = m_appConfig;
		
		m_viewContext.appContext = m_appContext;
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
				stage_registerStateMachine(null);	break;
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
		Logger.getLogger("swarm.shared.statemachine").setLevel(Level.OFF);
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
				
				String platform = m_appContext.platformInfo.getRawPlatform();
				smTelemetryAssert telemetryAssert = new smTelemetryAssert(message, platform);
				m_appContext.txnMngr.makeRequest(smE_TelemetryRequestPath.logAssert, telemetryAssert);

				//assert(false);
			}
		});
	}
	
	protected void stage_browserSupportCheck()
	{
		m_appContext.cellSandbox = new smSandboxManager(m_viewContext, new smSandboxManager.I_StartUpCallback()
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
		}, m_appConfig.appId, m_appConfig.useVirtualSandbox);
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
		boolean clientSide = true;
		smSignInValidator signInValidator = new smSignInValidator(clientSide);
		smSignUpValidator signUpValidator = new smSignUpValidator(clientSide);
		
		m_appContext.platformInfo = new smPlatformInfo();
		m_appContext.jsonFactory = new smGwtJsonFactory(m_appConfig.verboseTransactions);
		m_appContext.codeCompiler = new smClientCodeCompiler();
		
		m_appContext.codeCache = new smCellCodeCache(m_appConfig.codeCacheSize, m_appConfig.codeCacheExpiration_seconds, this);
		m_appContext.userMngr = new smUserManager(m_appContext, m_appConfig.user);
		m_appContext.requestPathMngr = new smRequestPathManager(m_appContext.jsonFactory, m_appConfig.verboseTransactions);
		m_appContext.txnMngr = new smClientTransactionManager(m_appContext.requestPathMngr, m_appContext.jsonFactory);
		m_appContext.gridMngr = new smGridManager(m_appContext.txnMngr, m_appContext.jsonFactory, m_appConfig.grid);
		m_appContext.cameraMngr = new smCameraManager(m_appContext.gridMngr, new smCamera(), m_appConfig.minSnapTime, m_appConfig.maxSnapTime);
		m_appContext.addressMngr = new smCellAddressManager(m_appContext, m_appConfig.addressCacheSize, m_appConfig.addressCacheExpiration_seconds, this);
		m_appContext.accountMngr = new smClientAccountManager(signInValidator, signUpValidator, m_appContext.txnMngr, m_appContext.jsonFactory);
		m_appContext.codeMngr = new smCellCodeManager(m_appContext);
		m_appContext.cellBufferMngr = new smCellBufferManager(m_appContext.codeMngr);
		
		//--- DRK > Configure transaction stuff.
		m_appContext.requestPathMngr.register(smE_RequestPath.values());
		m_appContext.txnMngr.setSyncRequestDispatcher(new smInlineRequestDispatcher(m_appContext.jsonFactory, m_appContext.requestPathMngr, m_appConfig.appId));
		m_appContext.txnMngr.setAsyncRequestDispatcher(new smGwtRequestDispatcher(m_appContext.jsonFactory, m_appContext.requestPathMngr));
	}
	
	protected void stage_startViewManagers()
	{
		m_viewContext.recaptchaWrapper = new smRecaptchaWrapper();
		m_viewContext.clickMngr = new smClickManager();
		m_viewContext.toolTipMngr = new smToolTipManager(m_appContext.platformInfo.getPlatform() != smE_Platform.IOS, smS_UI.TOOL_TIP_DELAY);
		
		//--- DRK > Set defaults for tool tips.
		for( int i = smE_ZIndex.TOOL_TIP_1.ordinal(), j = 0; i <= smE_ZIndex.TOOL_TIP_5.ordinal(); i++, j++ )
		{
			m_viewContext.toolTipMngr.setDefaultZIndex(smE_ToolTipType.values()[j], i);
		}
		m_viewContext.toolTipMngr.setDefaultPadding(smS_UI.TOOl_TIP_PADDING);
	}
	
	protected void stage_registerStateMachine(smI_StateEventListener stateEventListener)
	{
		stateEventListener = stateEventListener != null ? stateEventListener : new smViewController(m_viewContext, m_viewConfig, m_appConfig);
		
		m_stateContext = m_viewContext.stateContext = new smStateContext(new StateMachine_Base(m_appContext), stateEventListener);
		{
			m_stateContext.registerState(new State_Initializing(m_appContext));
			
			m_stateContext.registerState(new State_GenericDialog());
			m_stateContext.registerState(new State_AsyncDialog());
			
			m_stateContext.registerState(new StateContainer_Base());
			{
				m_stateContext.registerState(new StateMachine_Camera(m_appContext));
				{
					m_stateContext.registerState(new State_CameraFloating());
					m_stateContext.registerState(new State_GettingMapping());
					m_stateContext.registerState(new State_CameraSnapping(m_appContext, m_appConfig.cellHudHeight));
					m_stateContext.registerState(new State_ViewingCell(m_appContext));
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
		m_stateContext.didEnter();
		m_stateContext.didForeground();
		
		m_appContext.txnMngr.flushSyncResponses();
	}

	protected void registerCodeEditingStates()
	{
		m_stateContext.registerState(new StateMachine_EditingCode(m_appContext));
		{
			m_stateContext.registerState(new State_EditingCode(m_appContext));
			m_stateContext.registerState(new State_EditingCodeBlocker());
		}
	}
	
	protected void registerAccountStates()
	{
		m_stateContext.registerState(new StateMachine_Account(m_appContext.accountMngr));
		{
			m_stateContext.registerState(new State_ManageAccount(m_appContext.accountMngr));
			m_stateContext.registerState(new State_AccountStatusPending());
			m_stateContext.registerState(new State_SignInOrUp(m_appContext.accountMngr, m_appContext.userMngr));
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
		m_stateContext.didUpdate(currentTime - m_lastTime);
		m_lastTime = currentTime;

		m_appContext.txnMngr.flushSyncResponses();
	}

	@Override
	public double getTime()
	{
		return m_lastTime;
	}
}
