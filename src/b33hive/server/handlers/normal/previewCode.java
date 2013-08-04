package b33hive.server.handlers.normal;

import b33hive.server.account.bhE_Role;
import b33hive.server.session.bhSessionManager;
import b33hive.server.structs.bhServerGridCoordinate;
import b33hive.server.transaction.bhI_RequestHandler;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.code.bhA_CodeCompiler;
import b33hive.shared.code.bhCompilerResult;
import b33hive.shared.entities.bhE_CodeSafetyLevel;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.structs.bhCode;
import b33hive.shared.structs.bhCodePrivileges;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;
import b33hive.shared.utils.bhU_Singletons;

public class previewCode implements bhI_RequestHandler
{
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		if( !bhSessionManager.getInstance().isAuthorized(request, response, bhE_Role.USER) )
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
		
		bhServerGridCoordinate coordinate = new bhServerGridCoordinate();
		coordinate.readJson(request.getJson());
		
		//--- DRK > Obviously we're trusting the client here as to their privileges, which could easily be hacked, but it doesn't really matter.
		//---		This handler should be completely self-contained, so there's no chance of the hacked code leaking into the database.
		//---		This is an optimization so that we don't have to hit the database, but in the future I might just hit the database for it.
		bhCodePrivileges privileges = new bhCodePrivileges(request.getJson());
		
		bhCode sourceCode = new bhCode(request.getJson(), bhE_CodeType.SOURCE);
		
		bhA_CodeCompiler compiler = bhU_Singletons.get(bhA_CodeCompiler.class);
		
		bhCompilerResult result = compiler.compile(sourceCode, privileges, coordinate.writeString());
		
		result.writeJson(response.getJson());
	}
}
