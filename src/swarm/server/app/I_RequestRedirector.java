package swarm.server.app;

import java.io.IOException;

public interface I_RequestRedirector
{
	void redirectToMainPage(Object nativeResponse) throws IOException;
}
