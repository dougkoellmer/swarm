package swarm.server.app;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.rdbms.AppEngineDriver;

import swarm.client.app.ClientAppConfig;
import swarm.server.account.SqlAccountDatabase;
import swarm.server.account.ServerAccountManager;
import swarm.server.code.ServerCodeCompiler;
import swarm.server.data.blob.BlobManagerFactory;
import swarm.server.entities.BaseServerGrid;
import swarm.server.handlers.admin.adminHandler;
import swarm.server.handlers.admin.deleteHomeCells;
import swarm.server.handlers.admin.I_HomeCellCreator;
import swarm.server.handlers.admin.clearCell;
import swarm.server.handlers.admin.createGrid;
import swarm.server.handlers.admin.deactivateUserCells;
import swarm.server.handlers.admin.recompileCells;
import swarm.server.handlers.admin.refreshHomeCells;
import swarm.server.handlers.normal.getAccountInfo;
import swarm.server.handlers.normal.getCellAddress;
import swarm.server.handlers.normal.getCellAddressMapping;
import swarm.server.handlers.normal.getCode;
import swarm.server.handlers.normal.getFocusedCellSize;
import swarm.server.handlers.normal.getGridData;
import swarm.server.handlers.normal.getHashedPassword;
import swarm.server.handlers.normal.getPasswordChangeToken;
import swarm.server.handlers.normal.getServerVersion;
import swarm.server.handlers.normal.getUserData;
import swarm.server.handlers.normal.logAssert;
import swarm.server.handlers.normal.sessionQueryTest;
import swarm.server.handlers.normal.setNewDesiredPassword;
import swarm.server.handlers.normal.signIn;
import swarm.server.handlers.normal.signOut;
import swarm.server.handlers.normal.signUp;
import swarm.server.handlers.normal.syncCode;
import swarm.server.session.SessionManager;
import swarm.server.telemetry.TelemetryDatabase;
import swarm.server.thirdparty.json.ServerJsonFactory;
import swarm.server.thirdparty.servlet.ServletRedirector;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.E_AdminRequestPath;
import swarm.server.transaction.E_DebugRequestPath;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.I_TransactionScopeListener;
import swarm.server.transaction.InlineTransactionManager;
import swarm.server.transaction.ServerTransactionManager;
import swarm.shared.E_AppEnvironment;
import swarm.shared.account.SignInValidator;
import swarm.shared.account.SignUpValidator;
import swarm.shared.app.BaseAppContext;
import swarm.shared.app.A_App;
import swarm.shared.app.S_CommonApp;
import swarm.shared.debugging.I_AssertionDelegate;
import swarm.shared.debugging.U_Debug;
import swarm.shared.json.JsonHelper;
import swarm.shared.transaction.E_RequestPath;
import swarm.shared.transaction.E_TelemetryRequestPath;
import swarm.shared.transaction.I_RequestPath;
import swarm.shared.transaction.RequestPathManager;

public abstract class A_ServerApp extends A_App
{
	private static final Logger s_logger = Logger.getLogger(A_ServerApp.class.getName());
	
	private static A_ServerApp s_instance;
	
	private ServerAppConfig m_appConfig;
	protected ServerContext m_context;
	
	protected A_ServerApp()
	{
		super(E_AppEnvironment.SERVER);
		
		s_instance = this;
		
		m_context = new ServerContext();
	}
	
	public static A_ServerApp getInstance()
	{
		return s_instance;
	}
	
	public ServerContext getContext()
	{
		return m_context;
	}
	
	public ServerAppConfig getConfig()
	{
		return m_appConfig;
	}
	
	protected void entryPoint(ServerAppConfig appConfig)
	{
		m_appConfig = appConfig;
		m_context.config = m_appConfig;
		
		U_Debug.setDelegate(new I_AssertionDelegate()
		{
			@Override
			public void doAssert(String message)
			{
				assert(false);
			}
		});
		
		try
		{
			DriverManager.registerDriver(new AppEngineDriver());
		}
		catch (SQLException e1)
		{
			s_logger.log(Level.SEVERE, "Could not start up sql databases.", e1);
		}
		
		boolean clientSide = false;
		
		SqlAccountDatabase accountDatabase = null;
		
		if( m_context.accountMngr == null )
		{
			accountDatabase = new SqlAccountDatabase(appConfig.databaseUrl, appConfig.accountsDatabase);
			SignInValidator signInValidator = new SignInValidator(clientSide);
			SignUpValidator signUpValidator = new SignUpValidator(clientSide);
			m_context.accountMngr = new ServerAccountManager(signInValidator, signUpValidator, accountDatabase);
		}
		
		m_context.jsonFactory = new ServerJsonFactory();
		m_context.codeCompiler = new ServerCodeCompiler(true);
		m_context.requestPathMngr = new RequestPathManager(m_context.jsonFactory, appConfig.verboseTransactions);
		m_context.txnMngr = new ServerTransactionManager((A_ServerJsonFactory) m_context.jsonFactory, m_context.requestPathMngr, appConfig.verboseTransactions, appConfig.libServerVersion, appConfig.appServerVersion);
		m_context.inlineTxnMngr = new InlineTransactionManager(m_context.txnMngr, m_context.requestPathMngr, (A_ServerJsonFactory) m_context.jsonFactory, appConfig.appId, appConfig.verboseTransactions);
		m_context.blobMngrFactory = new BlobManagerFactory();
		m_context.sessionMngr = new SessionManager(m_context.blobMngrFactory, m_context.jsonFactory);
		m_context.redirector = new ServletRedirector(appConfig.mainPage);
		
		if( appConfig.telemetryDatabase != null )
		{
			m_context.telemetryDb = new TelemetryDatabase(appConfig.databaseUrl, appConfig.telemetryDatabase);
			addTxnScopeListener(m_context.telemetryDb);
		}
		
		addClientHandlers();
		addAdminHandlers(m_appConfig.T_homeCellCreator);
		addTelemetryHandlers();
		//addDebugHandlers();
		//addDebugPathResponseErrors();
		
		addTxnScopeListener(m_context.blobMngrFactory);
		addTxnScopeListener(m_context.sessionMngr);
		
		if( accountDatabase != null )
		{
			addTxnScopeListener(accountDatabase);
		}
	}
	
	private void addTxnScopeListener(I_TransactionScopeListener listener)
	{
		m_context.txnMngr.addScopeListener(listener);
		m_context.inlineTxnMngr.addScopeListener(listener);
	}
	
	private void addDebugPathResponseErrors()
	{
		m_context.txnMngr.setDebugResponseError(E_RequestPath.syncCode);
	}
	
	private void addClientHandlers()
	{
		m_context.requestPathMngr.register(E_RequestPath.values());
		
		getCode getCodeHandler = new getCode();
		
		setNormalHandler(getCodeHandler,				E_RequestPath.getCode);
		setNormalHandler(new syncCode(),				E_RequestPath.syncCode);
		setNormalHandler(new getCellAddress(),			E_RequestPath.getCellAddress);
		setNormalHandler(new getCellAddressMapping(),	E_RequestPath.getCellAddressMapping);
		setNormalHandler(new getUserData(false, m_appConfig.gridExpansionDelta),		E_RequestPath.getUserData);
		setNormalHandler(new getGridData(),				E_RequestPath.getGridData);
		setNormalHandler(new signIn(),					E_RequestPath.signIn);
		setNormalHandler(new signUp(m_appConfig.publicRecaptchaKey, m_appConfig.privateRecaptchaKey), E_RequestPath.signUp);
		setNormalHandler(new signOut(),					E_RequestPath.signOut);
		setNormalHandler(new getAccountInfo(),			E_RequestPath.getAccountInfo);
		setNormalHandler(new setNewDesiredPassword(),	E_RequestPath.setNewDesiredPassword);
		setNormalHandler(new getPasswordChangeToken(),	E_RequestPath.getPasswordChangeToken);
		setNormalHandler(new getServerVersion(),		E_RequestPath.getServerVersion);
		//setNormalHandler(new getHashedPassword(),		E_RequestPath.getHashedPassword);
		setNormalHandler(new getFocusedCellSize(),		E_RequestPath.getFocusedCellSize);
		
		m_context.txnMngr.addDeferredHandler(getCodeHandler);
	}
	
	protected void setNormalHandler(A_DefaultRequestHandler handler_nullable, I_RequestPath path)
	{
		if( handler_nullable != null )
		{
			handler_nullable.init(m_context);
		}
		
		m_context.txnMngr.setRequestHandler(handler_nullable, path);
	}
	
	private void addAdminHandlers(Class<? extends I_HomeCellCreator> T_homeCellCreator)
	{
		m_context.requestPathMngr.register(E_AdminRequestPath.values());
		
		setAdminHandler(new createGrid(BaseServerGrid.class),				E_AdminRequestPath.createGrid);
		setAdminHandler(new deactivateUserCells(),							E_AdminRequestPath.deactivateUserCells);
		setAdminHandler(new refreshHomeCells(T_homeCellCreator),			E_AdminRequestPath.refreshHomeCells);
		setAdminHandler(new clearCell(),									E_AdminRequestPath.clearCell);
		setAdminHandler(new recompileCells(),								E_AdminRequestPath.recompileCells);
		setAdminHandler(new deleteHomeCells(),								E_AdminRequestPath.deleteHomeCells);
	}
	
	protected void setAdminHandler(A_DefaultRequestHandler handler_nullable, I_RequestPath path)
	{
		if( handler_nullable != null )
		{
			handler_nullable.init(m_context);
			
			adminHandler adminWrapper = new adminHandler(m_context.sessionMngr, handler_nullable);
			
			m_context.txnMngr.setRequestHandler(adminWrapper, path);
		}
		else
		{
			m_context.txnMngr.setRequestHandler(null, path);
		}
	}
	
	private void addTelemetryHandlers()
	{
		setNormalHandler(new logAssert(),	E_TelemetryRequestPath.logAssert);
	}
	
	private void addDebugHandlers()
	{
		ServerTransactionManager txnManager = m_context.txnMngr;
		m_context.requestPathMngr.register(E_DebugRequestPath.values());
		
		txnManager.setRequestHandler(new sessionQueryTest(),		E_DebugRequestPath.sessionQueryTest);
	}
}
