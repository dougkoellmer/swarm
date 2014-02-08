package swarm.client.app;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.google.gwt.logging.client.TextLogFormatter;
import com.google.gwt.user.client.Timer;

import swarm.client.code.ClientCodeCompiler;
import swarm.client.entities.Camera;
import swarm.client.input.ClickManager;
import swarm.client.managers.CameraManager;
import swarm.client.managers.CellAddressManager;
import swarm.client.managers.CellBufferManager;
import swarm.client.managers.CellCodeManager;
import swarm.client.managers.CellSizeManager;
import swarm.client.managers.ClientAccountManager;
import swarm.client.managers.GridManager;
import swarm.client.managers.UserManager;
import swarm.client.navigation.U_CameraViewport;
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
import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
import swarm.client.states.camera.Action_Camera_SnapToCoordinate.I_Filter;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_CameraFloating;
import swarm.client.states.camera.State_CameraSnapping;
import swarm.client.states.camera.State_GettingMapping;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.states.camera.Action_Camera_SnapToCoordinate.Args;
import swarm.client.states.code.StateMachine_EditingCode;
import swarm.client.states.code.State_EditingCode;
import swarm.client.states.code.State_EditingCodeBlocker;
import swarm.client.structs.CellCodeCache;
import swarm.client.thirdparty.captcha.RecaptchaWrapper;
import swarm.client.thirdparty.json.GwtJsonFactory;
import swarm.client.thirdparty.transaction.GwtRequestDispatcher;
import swarm.client.time.U_Time;
import swarm.client.transaction.ClientTransactionManager;
import swarm.client.transaction.InlineRequestDispatcher;
import swarm.client.view.E_ZIndex;
import swarm.client.view.S_UI;
import swarm.client.view.ViewConfig;
import swarm.client.view.ViewContext;
import swarm.client.view.ViewController;
import swarm.client.view.cell.B33Spinner;
import swarm.client.view.cell.I_CellSpinner;
import swarm.client.view.cell.I_CellSpinnerFactory;
import swarm.client.view.cell.SpritePlateSpinner;
import swarm.client.view.sandbox.SandboxManager;
import swarm.client.view.tooltip.E_ToolTipType;
import swarm.client.view.tooltip.ToolTipManager;
import swarm.shared.E_AppEnvironment;
import swarm.shared.account.SignInValidator;
import swarm.shared.account.SignUpValidator;
import swarm.shared.app.BaseAppContext;
import swarm.shared.app.A_App;
import swarm.shared.app.S_CommonApp;
import swarm.shared.debugging.I_AssertionDelegate;
import swarm.shared.debugging.TelemetryAssert;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.A_Grid;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.A_StateMachine;
import swarm.shared.statemachine.I_StateEventListener;
import swarm.shared.statemachine.StateContext;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;
import swarm.shared.time.I_TimeSource;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.E_TelemetryRequestPath;
import swarm.shared.transaction.RequestPathManager;

public class A_ClientApp extends A_App implements I_TimeSource
{
	private static final Logger s_logger = Logger.getLogger(A_ClientApp.class.getName());	
	
	private double m_lastTime = 0;
	
	protected final ClientAppConfig m_appConfig;
	protected final ViewConfig m_viewConfig;
	protected final AppContext m_appContext;
	protected final ViewContext m_viewContext;
	protected StateContext m_stateContext;
	
	protected A_ClientApp(ClientAppConfig appConfig, ViewConfig viewConfig)
	{
		super(E_AppEnvironment.CLIENT);
		
		m_appConfig = appConfig;
		m_viewConfig = viewConfig;
		m_appContext = new AppContext();
		m_viewContext = new ViewContext();
		m_viewContext.appConfig = m_appConfig;
		m_viewContext.viewConfig = viewConfig;
		m_appContext.config = m_appConfig;
		m_viewContext.appContext = m_appContext;
		
		m_appContext.timeSource = this;
	}
	
	protected void startUp(E_StartUpStage stage)
	{
		switch(stage)
		{
			case CHECK_BROWSER_SUPPORT:// this case may go async with caja init...if so can't recurse immediately.
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
				stage_registerStateMachine(null, null);	break;
			}
			
			case START_UPDATE_LOOP:
			{
				stage_startUpdateLoop();			break;
			}
			
			case GUNSHOT_SOUND:
			{
				stage_gunshotSound();				break;
			}
		};
		
		E_StartUpStage nextStage = stage.getNext();
		
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
		
		U_Debug.setDelegate(new I_AssertionDelegate()
		{
			@Override
			public void doAssert(String message)
			{
				//--- DRK > Asserts are sometimes pretty silent, so this lets you know that eclipse has at least a stack trace for you if you're lucky.
				//Window.alert("ASSERTION FAILED: " + message);
				
				s_logger.severe("ASSERTION FAILED: " + message);
				
				String platform = m_appContext.platformInfo.getRawPlatform();
				TelemetryAssert telemetryAssert = new TelemetryAssert(message, platform);
				m_appContext.txnMngr.makeRequest(E_TelemetryRequestPath.logAssert, telemetryAssert);

				//assert(false);
			}
		});
	}
	
	protected void stage_browserSupportCheck()
	{
		m_appContext.cellSandbox = new SandboxManager(m_viewContext, new SandboxManager.I_StartUpCallback()
		{
			public void onStartUpComplete(boolean success)
			{
				if( success )
				{
					startUp(E_StartUpStage.CHECK_BROWSER_SUPPORT.getNext());
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
		SignInValidator signInValidator = new SignInValidator(clientSide);
		SignUpValidator signUpValidator = new SignUpValidator(clientSide);
		
		m_appContext.platformInfo = new PlatformInfo();
		m_appContext.jsonFactory = new GwtJsonFactory(m_appConfig.verboseTransactions);
		m_appContext.codeCompiler = new ClientCodeCompiler();
		
		Camera.DefaultMaxZAlgorithm maxZAlgo = new Camera.DefaultMaxZAlgorithm();
		Camera camera = new Camera(maxZAlgo);
		maxZAlgo.init(m_appConfig.grid, camera);
		
		m_appContext.codeCache = new CellCodeCache(m_appConfig.codeCacheSize, m_appConfig.codeCacheExpiration_seconds, this);
		m_appContext.userMngr = new UserManager(m_appContext, m_appConfig.user);
		m_appContext.requestPathMngr = new RequestPathManager(m_appContext.jsonFactory, m_appConfig.verboseTransactions);
		m_appContext.txnMngr = new ClientTransactionManager(m_appContext.requestPathMngr, m_appContext.jsonFactory);
		m_appContext.gridMngr = new GridManager(m_appContext.txnMngr, m_appContext.jsonFactory, m_appConfig.grid);
		m_appContext.cameraMngr = new CameraManager(m_appContext.gridMngr, camera, m_appConfig.minSnapTime, m_appConfig.snapTimeRange);
		m_appContext.addressMngr = new CellAddressManager(m_appContext, m_appConfig.addressCacheSize, m_appConfig.addressCacheExpiration_seconds, this);
		m_appContext.accountMngr = new ClientAccountManager(signInValidator, signUpValidator, m_appContext.txnMngr, m_appContext.jsonFactory);
		m_appContext.codeMngr = new CellCodeManager(m_appContext);
		m_appContext.cellSizeMngr = new CellSizeManager(m_appContext);
		m_appContext.cellBufferMngr = new CellBufferManager(m_appContext.codeMngr, m_appContext.cellSizeMngr);
		
		//--- DRK > Configure transaction stuff.
		m_appContext.requestPathMngr.register(E_RequestPath.values());
		m_appContext.txnMngr.setSyncRequestDispatcher(new InlineRequestDispatcher(m_appContext.jsonFactory, m_appContext.requestPathMngr, m_appConfig.appId));
		m_appContext.txnMngr.setAsyncRequestDispatcher(new GwtRequestDispatcher(m_appContext.jsonFactory, m_appContext.requestPathMngr));
	}
	
	protected void stage_startViewManagers()
	{
		m_viewContext.recaptchaWrapper = new RecaptchaWrapper();
		m_viewContext.clickMngr = new ClickManager();
		m_viewContext.toolTipMngr = new ToolTipManager(m_appContext.platformInfo.getPlatform() != E_Platform.IOS, S_UI.TOOL_TIP_DELAY);
		
		//--- DRK > Set defaults for tool tips.
		for( int i = E_ZIndex.TOOL_TIP_1.ordinal(), j = 0; i <= E_ZIndex.TOOL_TIP_5.ordinal(); i++, j++ )
		{
			m_viewContext.toolTipMngr.setDefaultZIndex(E_ToolTipType.values()[j], i);
		}
		m_viewContext.toolTipMngr.setDefaultPadding(S_UI.TOOl_TIP_PADDING);
		
		m_viewContext.spinnerFactory = new I_CellSpinnerFactory()
		{
			@Override
			public I_CellSpinner newSpinner()
			{
				return new B33Spinner();
			}
		};
	}
	
	protected void stage_registerStateMachine(I_StateEventListener stateEventListener, Class<? extends A_State> consoleState_T_null)
	{
		Action_Camera_SnapToCoordinate.I_Filter snapFilter = new Action_Camera_SnapToCoordinate.I_Filter()
		{
			@Override
			public void adjustTargetPoint(Action_Camera_SnapToCoordinate.Args args)
			{				
				m_viewContext.scrollNavigator.adjustSnapTargetPoint(args);
			}
			
			@Override
			public void setTargetPoint(Action_Camera_SnapToCoordinate.Args args)
			{				
				A_Grid grid = m_appContext.gridMngr.getGrid();
				Camera camera = m_appContext.cameraMngr.getCamera();
				
				U_CameraViewport.calcViewWindowTopLeft(grid, args.getTargetCoordinate(), m_appConfig.cellHudHeight, args.getTargetPoint());
				U_CameraViewport.calcConstrainedCameraPoint(grid, args.getTargetCoordinate(), args.getTargetPoint(), camera.getViewWidth(), camera.getViewHeight(), m_appConfig.cellHudHeight, args.getTargetPoint());
				
				m_viewContext.scrollNavigator.adjustSnapTargetPoint(args);
			}
		};
		
		stateEventListener = stateEventListener != null ? stateEventListener : new ViewController(m_viewContext, m_viewConfig, m_appConfig);
		
		m_stateContext = m_viewContext.stateContext = new StateContext(new StateMachine_Base(m_appContext), stateEventListener);
		{
			m_stateContext.registerState(new State_Initializing(m_appContext));
			
			m_stateContext.registerState(new State_GenericDialog());
			m_stateContext.registerState(new State_AsyncDialog());
			
			m_stateContext.registerState(new StateContainer_Base(consoleState_T_null));
			{
				m_stateContext.registerState(new StateMachine_Camera(m_appContext, snapFilter));
				{
					m_stateContext.registerState(new State_CameraFloating());
					m_stateContext.registerState(new State_GettingMapping());
					m_stateContext.registerState(new State_CameraSnapping(m_appContext, m_appConfig.cellHudHeight));
					m_stateContext.registerState(new State_ViewingCell(m_appContext, m_appConfig.cellHudHeight));
				}
			}
		}
	}
	
	protected void stage_startUpdateLoop()
	{
		U_Time.startUp();
		
		/*Timer timer = new Timer()
		{
			@Override
			public void run()
			{
				smA_ClientApp.this.update();				
			}
		};
		timer.scheduleRepeating(m_appConfig.framerate_milliseconds);*/
		
		requestAnimationFrameLoop();
	}
	
	private native void requestAnimationFrameLoop()
	/*-{
			var thisArg = this;
		
			function update()
			{
				thisArg.@swarm.client.app.A_ClientApp::update()();
				
				$wnd.requestAnimationFrame(update);
			}

			$wnd.requestAnimationFrame(update);
	}-*/;
	
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
		double currentTime = U_Time.getSeconds();
		m_stateContext.didUpdate(currentTime - m_lastTime);
		m_lastTime = currentTime;

		m_appContext.txnMngr.flushSyncResponses();
		m_appContext.txnMngr.flushAsyncRequestQueue();
	}

	@Override
	public double getTime()
	{
		return m_lastTime;
	}
}
