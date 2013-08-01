package com.b33hive.client.ui.tabs;

import com.b33hive.client.ui.bhI_UIElement;
import com.b33hive.shared.statemachine.bhA_Action;
import com.b33hive.shared.statemachine.bhStateEvent;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class bhBookmarkTab extends AbsolutePanel implements bhI_TabContent
{
	public bhBookmarkTab()
	{
		this.getElement().getStyle().setBackgroundColor("#ffffff");
		
		this.addStyleName("bh_bookmark_viewer");
	}
	
	@Override
	public void onStateEvent(bhStateEvent event)
	{
	}

	@Override
	public void onResize()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSelect()
	{
		// TODO Auto-generated method stub
		
	}
}
