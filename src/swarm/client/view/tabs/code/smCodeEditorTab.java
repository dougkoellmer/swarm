package swarm.client.view.tabs.code;

import com.google.gwt.user.client.ui.Widget;

import swarm.client.view.smViewContext;
import swarm.client.view.tabs.smA_Tab;
import swarm.client.view.tabs.smI_Tab;
import swarm.client.view.tabs.smI_TabContent;
import swarm.shared.statemachine.smStateEvent;

public class smCodeEditorTab extends smA_Tab
{
	public smCodeEditorTab(smViewContext viewContext)
	{
		super("HTML", "View or edit the html of a cell.", new smCodeEditorTabContent(viewContext));
	}
}
