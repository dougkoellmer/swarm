package swarm.client.view.tabs.code;

import com.google.gwt.user.client.ui.Widget;

import swarm.client.view.ViewContext;
import swarm.client.view.tabs.A_Tab;
import swarm.client.view.tabs.I_Tab;
import swarm.client.view.tabs.I_TabContent;
import swarm.shared.statemachine.A_BaseStateEvent;

public class CodeEditorTab extends A_Tab
{
	public CodeEditorTab(ViewContext viewContext)
	{
		super("HTML", "View or edit the html of a cell.", new CodeEditorTabContent(viewContext));
	}
}
