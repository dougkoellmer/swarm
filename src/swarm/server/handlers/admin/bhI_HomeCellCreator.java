package swarm.server.handlers.admin;

import javax.servlet.ServletContext;

import swarm.server.account.bhUserSession;
import swarm.server.entities.bhServerUser;
import swarm.server.transaction.bhTransactionContext;
import swarm.shared.transaction.bhTransactionRequest;
import swarm.shared.transaction.bhTransactionResponse;

public interface bhI_HomeCellCreator
{
	void initialize(ServletContext context);
	
	void run(bhTransactionRequest request, bhTransactionResponse response, bhTransactionContext context, bhUserSession session, bhServerUser user);
}
