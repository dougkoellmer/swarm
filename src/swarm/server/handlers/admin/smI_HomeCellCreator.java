package swarm.server.handlers.admin;

import javax.servlet.ServletContext;

import swarm.server.account.smUserSession;
import swarm.server.app.smServerContext;
import swarm.server.entities.smServerUser;
import swarm.server.transaction.smTransactionContext;
import swarm.shared.transaction.smTransactionRequest;
import swarm.shared.transaction.smTransactionResponse;

public interface smI_HomeCellCreator
{
	void initialize(smServerContext serverContext, ServletContext servletContext);
	
	void run(smTransactionRequest request, smTransactionResponse response, smTransactionContext context, smUserSession session, smServerUser user);
}
