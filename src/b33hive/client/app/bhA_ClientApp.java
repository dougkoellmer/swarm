package b33hive.client.app;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.google.gwt.logging.client.TextLogFormatter;
import com.google.gwt.user.client.Timer;

import b33hive.client.code.bhClientCodeCompiler;
import b33hive.client.input.bhClickManager;
import b33hive.client.managers.bhCellAddressManager;
import b33hive.client.managers.bhClientAccountManager;
import b33hive.client.managers.bhGridManager;
import b33hive.client.managers.bhUserManager;
import b33hive.client.states.StateContainer_Base;
import b33hive.client.states.StateMachine_Base;
import b33hive.client.states.StateMachine_Tabs;
import b33hive.client.states.State_AsyncDialog;
import b33hive.client.states.State_GenericDialog;
import b33hive.client.states.State_Initializing;
import b33hive.client.states.account.StateMachine_Account;
import b33hive.client.states.account.State_AccountStatusPending;
import b33hive.client.states.account.State_ManageAccount;
import b33hive.client.states.account.State_SignInOrUp;
import b33hive.client.states.camera.StateMachine_Camera;
import b33hive.client.states.camera.State_CameraFloating;
import b33hive.client.states.camera.State_CameraSnapping;
import b33hive.client.states.camera.State_GettingMapping;
import b33hive.client.states.camera.State_ViewingCell;
import b33hive.client.states.code.StateMachine_EditingCode;
import b33hive.client.states.code.State_EditingCode;
import b33hive.client.states.code.State_EditingCodeBlocker;
import b33hive.client.structs.bhCellCodeCache;
import b33hive.client.thirdparty.captcha.bhRecaptchaWrapper;
import b33hive.client.thirdparty.json.bhGwtJsonFactory;
import b33hive.client.thirdparty.transaction.bhGwtRequestDispatcher;
import b33hive.client.time.bhU_Time;
import b33hive.client.transaction.bhClientTransactionManager;
import b33hive.client.transaction.bhInlineRequestDispatcher;
import b33hive.client.ui.bhE_ZIndex;
import b33hive.client.ui.bhS_UI;
import b33hive.client.ui.bhViewConfig;
import b33hive.client.ui.bhViewController;
import b33hive.client.ui.tabs.code.bhCellSandbox;
import b33hive.client.ui.tooltip.bhE_ToolTipType;
import b33hive.client.ui.tooltip.bhToolTipManager;
import b33hive.shared.bhE_AppEnvironment;
import b33hive.shared.app.bh;
import b33hive.shared.app.bhA_App;
import b33hive.shared.app.bhS_App;
import b33hive.shared.debugging.bhI_AssertionDelegate;
import b33hive.shared.debugging.bhTelemetryAssert;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.statemachine.bhA_StateMachine;
import b33hive.shared.time.bhI_TimeSource;
import b33hive.shared.transaction.bhE_RequestPath;
import b33hive.shared.transaction.bhE_TelemetryRequestPath;
import b33hive.shared.transaction.bhRequestPathManager;

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
		
		bh_c.app = this;
	}
	
	protected void startUp(bhE_StartUpStage stage)
	{
		switch(stage)
		{
			case CONFIGURE_LOGGING:
			{
				stage_configureLogging();			break;
			}
		
			case CHECK_BROWSER_SUPPORT:// this case goes async, so can't recurse immediately.
			{
				stage_browserSupportCheck();		return;
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
				
				String platform = bh_c.platformInfo.getRawPlatform();
				bhTelemetryAssert telemetryAssert = new bhTelemetryAssert(message, platform);
				bh_c.txnMngr.makeRequest(bhE_TelemetryRequestPath.logAssert, telemetryAssert);

				//assert(false);
			}
		});
	}
	
	protected void stage_browserSupportCheck()
	{
		bh_c.cellSandbox = new bhCellSandbox(new bhCellSandbox.I_StartUpCallback()
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
	
	protected void stage_startAppManagers()
	{
		bh_c.platformInfo = new bhPlatformInfo();
		bh.jsonFactory = new bhGwtJsonFactory(bhS_App.VERBOSE_TRANSACTIONS);
		bh.codeCompiler = new bhClientCodeCompiler();
		bh_c.addressMngr = new bhCellAddressManager(m_appConfig.addressCacheSize, m_appConfig.addressCacheExpiration_seconds, this);
		bh_c.accountMngr = new bhClientAccountManager();
		bh_c.codeCache = new bhCellCodeCache(m_appConfig.codeCacheSize, m_appConfig.codeCacheExpiration_seconds, this);
		bh_c.userMngr = new bhUserManager(bh_c.accountMngr, bh_c.codeCache, m_appConfig.user);
		bh_c.gridMngr = new bhGridManager(m_appConfig.grid);
		bh.requestPathMngr = new bhRequestPathManager(bhS_App.VERBOSE_TRANSACTIONS);
		bh_c.txnMngr = new bhClientTransactionManager(bh.requestPathMngr);
		
		//--- DRK > Configure transaction stuff.
		bh_c.requestPathMngr.register(bhE_RequestPath.values());
		bh_c.txnMngr.setSyncRequestDispatcher(new bhInlineRequestDispatcher());
		bh_c.txnMngr.setAsyncRequestDispatcher(new bhGwtRequestDispatcher());
	}
	
	protected void stage_startViewManagers()
	{
		bh_c.recaptchaWrapper = new bhRecaptchaWrapper();
		bh_c.clickMngr = new bhClickManager();
		bh_c.toolTipMngr = new bhToolTipManager(bh_c.platformInfo.getPlatform() != bhE_Platform.IOS, bhS_UI.TOOL_TIP_DELAY);
		
		//--- DRK > Set defaults for tool tips.
		for( int i = bhE_ZIndex.TOOL_TIP_1.ordinal(), j = 0; i <= bhE_ZIndex.TOOL_TIP_5.ordinal(); i++, j++ )
		{
			bh_c.toolTipMngr.setDefaultZIndex(bhE_ToolTipType.values()[j], i);
		}
		bh_c.toolTipMngr.setDefaultPadding(bhS_UI.TOOl_TIP_PADDING);
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
				bhA_State.register(new StateMachine_Camera(m_appConfig.minSnapTime, m_appConfig.snapTimeRange));
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
		
		bh_c.txnMngr.flushSyncResponses();
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
				var unsupportedHtml =	"<table class='bh_unsupported_platform_font' style='width:100%; height:100%;'><tr><td style='vertical-align:middle; text-align:center;'>" +
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

		bh_c.txnMngr.flushSyncResponses();
	}

	@Override
	public double getTime()
	{
		return m_lastTime;
	}
}
