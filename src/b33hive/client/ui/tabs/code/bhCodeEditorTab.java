package b33hive.client.ui.tabs.code;

import com.google.gwt.user.client.ui.Widget;

import b33hive.client.ui.tabs.bhA_Tab;
import b33hive.client.ui.tabs.bhI_Tab;
import b33hive.client.ui.tabs.bhI_TabContent;
import b33hive.shared.statemachine.bhStateEvent;

public class bhCodeEditorTab extends bhA_Tab
{
	public bhCodeEditorTab()
	{
		super("HTML", "View or edit the html of a cell.", new bhCodeEditorTabContent());
	}
}
