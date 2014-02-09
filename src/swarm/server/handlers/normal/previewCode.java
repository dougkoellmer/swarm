package swarm.server.handlers.normal;

import swarm.server.account.E_Role;

import swarm.server.session.SessionManager;
import swarm.server.structs.ServerGridCoordinate;
import swarm.server.transaction.A_DefaultRequestHandler;
import swarm.server.transaction.I_RequestHandler;
import swarm.server.transaction.TransactionContext;
import swarm.shared.app.BaseAppContext;
import swarm.shared.code.A_CodeCompiler;
import swarm.shared.code.CompilerResult;
import swarm.shared.entities.E_CodeSafetyLevel;
import swarm.shared.entities.E_CodeType;
import swarm.shared.structs.Code;
import swarm.shared.structs.CodePrivileges;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;


public class previewCode extends A_DefaultRequestHandler
{
	@Override
	public void handleRequest(TransactionContext context, TransactionRequest request, TransactionResponse response)
	{
		if( !m_serverContext.sessionMngr.isAuthorized(request, response, E_Role.USER) )
		{
			return;
		}

		//--- DRK > NOTE: For performance reasons, we don't check if user owns this cell.
		//---		Since it can't result in a database write anyway, it should be harmless
		//---		to just assume that the user owns it, and avoid any database communication.
		
		//--- DRK > NOTE: This is a chink in the armor for a DoS attack.  Enforcing ownership of the cell
		//---		would require a database read before compiling, which would probably be easier on the cloud
		//---		environment as a whole under a DoS attack, especially if memcache came into play, but I'm leaving
		//---		it for now because I think the low risk of a DoS isn't worth the slower performance for well-meaning users.
		
		ServerGridCoordinate coordinate = new ServerGridCoordinate(m_serverContext.jsonFactory, request.getJsonArgs());
		
		//--- DRK > Obviously we're trusting the client here as to their privileges, which could easily be hacked, but it doesn't really matter.
		//---		This handler should be completely self-contained, so there's no chance of the hacked code leaking into the database.
		//---		This is an optimization so that we don't have to hit the database, but in the future I might just hit the database for it.
		CodePrivileges privileges = new CodePrivileges(m_serverContext.jsonFactory, request.getJsonArgs());
		
		Code sourceCode = new Code(m_serverContext.jsonFactory, request.getJsonArgs(), E_CodeType.SOURCE);
		
		CompilerResult result = m_serverContext.codeCompiler.compile(sourceCode, privileges, coordinate.writeString(), m_serverContext.config.appId);
		
		result.writeJson(response.getJsonArgs(), m_serverContext.jsonFactory);
	}
}
