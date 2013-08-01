package com.b33hive.client.ui.tabs.account;

import com.b33hive.client.ui.tooltip.bhE_ToolTipType;
import com.b33hive.client.ui.tooltip.bhToolTipConfig;
import com.b33hive.client.ui.tooltip.bhToolTipManager;
import com.b33hive.client.ui.widget.bhCheckBox;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.CheckBox;

public class bhStaySignedInCheckbox extends bhCheckBox
{
	bhStaySignedInCheckbox()
	{
		super("Remember Me");
		
		//this.getElement().getStyle().setMarginBottom(16, Unit.PX);
		this.setSize("100px", "20px"); // TODO: no, no, no
		
		bhToolTipConfig config = new bhToolTipConfig(bhE_ToolTipType.MOUSE_OVER, "Remembers you.");
		bhToolTipManager.getInstance().addTip(this.getClickCatcher(), config);
		
		//this.get
		//this.getElement().getStyle().setCursor(Cursor.);
	}
}
