package swarm.client.view.sandbox;

import swarm.client.view.tabs.code.I_CodeLoadListener;

import com.google.gwt.user.client.Element;

public interface I_CellSandbox
{
	void start(Element host, String rawCode, String cellNamespace, I_CodeLoadListener listener);
	
	void stop(Element host);
}
