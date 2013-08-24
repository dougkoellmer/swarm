package swarm.client.ui.tabs.code;

import com.google.gwt.user.client.ui.Widget;

import swarm.client.ui.tabs.bhA_Tab;
import swarm.client.ui.tabs.bhI_Tab;
import swarm.client.ui.tabs.bhI_TabContent;
import swarm.shared.statemachine.bhStateEvent;

public class bhCodeEditorTab extends bhA_Tab
{
	public bhCodeEditorTab()
	{
		super("HTML", "View or edit the html of a cell.", new bhCodeEditorTabContent());
	}
}
