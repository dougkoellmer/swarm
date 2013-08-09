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
import b33hive.client.states.State_ViewingBookmarks;
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
	
	private bhInlineRequestDispatcher m_synchronousRequestDispatcher;
	private double m_lastTime = 0;
	
	private bhClientAppConfig m_appConfig;
	private bhViewConfig m_viewConfig;
	
	protected bhA_ClientApp()
	{
		super(bhE_AppEnvironment.CLIENT);
		
		bh_c.app = this;
	}
	
	public bhViewConfig getViewConfig()
	{
		return m_viewConfig;
	}
	
	protected void entryPoint(bhClientAppConfig appConfig, bhViewConfig viewConfig)
	{
		m_appConfig = appConfig;
		m_viewConfig = viewConfig;
		
		bh_c.cellSandbox = new bhCellSandbox(new bhCellSandbox.I_StartUpCallback()
		{
			public void onStartUpComplete(boolean success)
			{
				if( success )
				{
					entryPoint_continued();
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
	
	protected void entryPoint_continued()
	{
		//--- DRK > Check platform info...mostly for determining touch/iOS-specific stuff for now.
		bh_c.platformInfo = new bhPlatformInfo();
		bhE_Platform platform = bh_c.platformInfo.getPlatform();
				
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
		
		//--- DRK > Start up a bunch of shared managers and services.
		bh_c.clickMngr = new bhClickManager();
		bh.jsonFactory = new bhGwtJsonFactory(bhS_App.VERBOSE_TRANSACTIONS);
		bh.codeCompiler = new bhClientCodeCompiler();
		bh_c.recaptchaWrapper = new bhRecaptchaWrapper();
		bh_c.addressMngr = new bhCellAddressManager(m_appConfig.addressCacheSize, m_appConfig.addressCacheExpiration_seconds, this);
		bh_c.toolTipMngr = new bhToolTipManager(platform != bhE_Platform.IOS, bhS_UI.TOOL_TIP_DELAY);
		bh_c.accountMngr = new bhClientAccountManager();
		bh_c.codeCache = new bhCellCodeCache(m_appConfig.codeCacheSize, m_appConfig.codeCacheExpiration_seconds, this);
		bh_c.userMngr = new bhUserManager(bh_c.accountMngr, bh_c.codeCache, m_appConfig.user);
		bh_c.gridMngr = new bhGridManager(m_appConfig.grid);
		bh.requestPathMngr = new bhRequestPathManager(bhS_App.VERBOSE_TRANSACTIONS);
		bh_c.txnMngr = new bhClientTransactionManager(bh.requestPathMngr);
		
		//--- DRK > Get transaction-related crap configured.
		bh_c.requestPathMngr.register(bhE_RequestPath.values());
		m_synchronousRequestDispatcher = new bhInlineRequestDispatcher();
		bh_c.txnMngr.setSynchronousRequestRouter(m_synchronousRequestDispatcher);
		bh_c.txnMngr.setAsynchronousRequestRouter(new bhGwtRequestDispatcher());

		//--- DRK > Set defaults for tool tips.
		for( int i = bhE_ZIndex.TOOL_TIP_1.ordinal(), j = 0; i <= bhE_ZIndex.TOOL_TIP_5.ordinal(); i++, j++ )
		{
			bh_c.toolTipMngr.setDefaultZIndex(bhE_ToolTipType.values()[j], i);
		}
		bh_c.toolTipMngr.setDefaultPadding(bhS_UI.TOOl_TIP_PADDING);
		
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
		timer.scheduleRepeating(m_appConfig.framerateMilliseconds);
		
		registerStates(m_appConfig);
		
		bhA_StateMachine.root_didEnter(StateMachine_Base.class, new bhViewController(m_viewConfig, m_appConfig));
		bhA_StateMachine.root_didForeground(StateMachine_Base.class);
		
		m_synchronousRequestDispatcher.flushQueuedSynchronousResponses();
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

	/**
	 * The braces and indenting here are just to make the overall structure of the machine more apparent.
	 */
	protected void registerStates(bhClientAppConfig config)
	{
		b33hive.shared.statemachine.bhA_State.register(new StateMachine_Base());
		{
			bhA_State.register(new State_Initializing());
			
			bhA_State.register(new State_GenericDialog());
			bhA_State.register(new State_AsyncDialog());
			
			bhA_State.register(new StateContainer_Base());
			{
				bhA_State.register(new StateMachine_Camera(config.minSnapTime, config.snapTimeRange));
				{
					bhA_State.register(new State_CameraFloating());
					bhA_State.register(new State_GettingMapping());
					bhA_State.register(new State_CameraSnapping(config.cellHudHeight));
					bhA_State.register(new State_ViewingCell());
				}
				
				bhA_State.register(new StateMachine_Tabs());
				{
					bhA_State.register(new StateMachine_EditingCode());
					{
						bhA_State.register(new State_EditingCode());
						bhA_State.register(new State_EditingCodeBlocker());
					}
					
					bhA_State.register(new StateMachine_Account());
					{
						bhA_State.register(new State_ManageAccount());
						bhA_State.register(new State_AccountStatusPending());
						bhA_State.register(new State_SignInOrUp());
					}
					
					bhA_State.register(new State_ViewingBookmarks());
				}
			}
		}
	}
	
	public void update()
	{
		double currentTime = bhU_Time.getSeconds();
		bhA_StateMachine.root_didUpdate(StateMachine_Base.class, currentTime - m_lastTime);
		m_lastTime = currentTime;
		
		m_synchronousRequestDispatcher.flushQueuedSynchronousResponses();
	}

	@Override
	public double getTime()
	{
		return m_lastTime;
	}
}
