package swarm.server.handlers.normal;

import javax.servlet.http.HttpServletRequest;

import swarm.server.account.bhS_ServerAccount;
import swarm.server.account.bhServerAccountManager;
import swarm.server.account.bhUserSession;
import swarm.server.account.sm_s;
import swarm.server.data.blob.bhBlobException;
import swarm.server.data.blob.bhBlobManagerFactory;
import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhI_BlobManager;
import swarm.server.debugging.bhServerTelemetryAssert;
import swarm.server.entities.bhE_GridType;
import swarm.server.entities.bhServerCell;
import swarm.server.session.bhSessionManager;
import swarm.server.structs.bhServerCellAddressMapping;
import swarm.server.telemetry.bhTelemetryDatabase;
import swarm.server.transaction.bhI_RequestHandler;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.app.sm;
import swarm.shared.app.bhS_App;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhJsonHelper;
import swarm.shared.structs.bhE_GetCellAddressError;
import swarm.shared.structs.bhGetCellAddressResult;
import swarm.shared.transaction.bhE_ResponseError;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public class getPasswordChangeToken implements bhI_RequestHandler
{
	@Override
	public void handleRequest(bhTransactionContext context, bhTransactionRequest request, bhTransactionResponse response)
	{
		bhSessionManager sessionManager = sm_s.sessionMngr;
		HttpServletRequest nativeRequest = (HttpServletRequest) request.getNativeRequest();
		
		String passwordChangeToken = nativeRequest.getParameter(bhS_ServerAccount.PASSWORD_CHANGE_TOKEN_PARAMETER_NAME);
		
		if( passwordChangeToken != null )
		{
			//--- DRK > Fringe case here, but might as well check.
			if( sessionManager.isSessionActive(request, response) )
			{
				sessionManager.endSession(request, response);
			}
			
			if( sm_s.accountMngr.isPasswordChangeTokenValid(passwordChangeToken) )
			{
				sm.jsonFactory.getHelper().putString(response.getJson(), bhE_JsonKey.passwordChangeToken, passwordChangeToken);
			}
			else
			{
				response.setError(bhE_ResponseError.BAD_STATE);
			}
		}
	}
}
