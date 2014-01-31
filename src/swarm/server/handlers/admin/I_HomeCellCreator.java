package swarm.server.handlers.admin;

import javax.servlet.ServletContext;

import swarm.server.account.UserSession;
import swarm.server.app.ServerContext;
import swarm.server.entities.ServerUser;
import swarm.server.transaction.TransactionContext;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public interface I_HomeCellCreator
{
	void initialize(ServerContext serverContext, ServletContext servletContext);
	
	void run(TransactionRequest request, TransactionResponse response, TransactionContext context, UserSession session, ServerUser user);
}
