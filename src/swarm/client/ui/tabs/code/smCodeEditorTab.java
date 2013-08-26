package swarm.client.ui.tabs.code;

import com.google.gwt.user.client.ui.Widget;

import swarm.client.ui.tabs.smA_Tab;
import swarm.client.ui.tabs.smI_Tab;
import swarm.client.ui.tabs.smI_TabContent;
import swarm.shared.statemachine.smStateEvent;

public class smCodeEditorTab extends smA_Tab
{
	public smCodeEditorTab()
	{
		super("HTML", "View or edit the html of a cell.", new smCodeEditorTabContent());
	}
}
