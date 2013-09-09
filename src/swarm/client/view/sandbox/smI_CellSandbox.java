package swarm.client.view.sandbox;

import swarm.client.view.tabs.code.smI_CodeLoadListener;

import com.google.gwt.user.client.Element;

public interface smI_CellSandbox
{
	void start(Element host, String rawCode, String cellNamespace, smI_CodeLoadListener listener);
	
	void stop(Element host);
}
