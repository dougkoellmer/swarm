package swarm.server.handlers.normal;

import swarm.server.account.smE_Role;
import swarm.server.account.sm_s;
import swarm.server.session.smSessionManager;
import swarm.server.structs.smServerGridCoordinate;
import swarm.server.transaction.smI_RequestHandler;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.app.sm;
import swarm.shared.code.smA_CodeCompiler;
import swarm.shared.code.smCompilerResult;
import swarm.shared.entities.smE_CodeSafetyLevel;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smCodePrivileges;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;


public class previewCode implements smI_RequestHandler
{
	@Override
	public void handleRequest(smTransactionContext context, smTransactionRequest request, smTransactionResponse response)
	{
		if( !sm_s.sessionMngr.isAuthorized(request, response, smE_Role.USER) )
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
		
		smServerGridCoordinate coordinate = new smServerGridCoordinate();
		coordinate.readJson(request.getJson());
		
		//--- DRK > Obviously we're trusting the client here as to their privileges, which could easily be hacked, but it doesn't really matter.
		//---		This handler should be completely self-contained, so there's no chance of the hacked code leaking into the database.
		//---		This is an optimization so that we don't have to hit the database, but in the future I might just hit the database for it.
		bhCodePrivileges privileges = new smCodePrivileges(request.getJson());
		
		bhCode sourceCode = new smCode(request.getJson(), smE_CodeType.SOURCE);
		
		bhCompilerResult result = sm.codeCompiler.compile(sourceCode, privileges, coordinate.writeString());
		
		result.writeJson(response.getJson());
	}
}
