package b33hive.server.handlers.admin;

import javax.servlet.ServletContext;

import b33hive.server.account.bhUserSession;
import b33hive.server.entities.bhServerUser;
import b33hive.server.transaction.bhTransactionContext;
import b33hive.shared.transaction.bhTransactionRequest;
import b33hive.shared.transaction.bhTransactionResponse;

public interface bhI_HomeCellCreator
{
	void initialize(ServletContext context);
	
	void run(bhTransactionRequest request, bhTransactionResponse response, bhTransactionContext context, bhUserSession session, bhServerUser user);
}
