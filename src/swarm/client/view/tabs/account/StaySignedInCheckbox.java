package swarm.client.view.tabs.account;

import swarm.client.app.AppContext;
import swarm.client.view.ViewContext;
import swarm.client.view.tooltip.E_ToolTipType;
import swarm.client.view.tooltip.ToolTipConfig;
import swarm.client.view.tooltip.ToolTipManager;
import swarm.client.view.widget.BaseCheckBox;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.CheckBox;

public class StaySignedInCheckbox extends BaseCheckBox
{
	StaySignedInCheckbox(ViewContext viewContext)
	{
		super(viewContext.clickMngr, "Remember Me");
		
		//this.getElement().getStyle().setMarginBottom(16, Unit.PX);
		this.setSize("100px", "20px"); // TODO: no, no, no
		
		ToolTipConfig config = new ToolTipConfig(E_ToolTipType.MOUSE_OVER, "Remembers you.");
		viewContext.toolTipMngr.addTip(this.getClickCatcher(), config);
		
		//this.get
		//this.getElement().getStyle().setCursor(Cursor.);
	}
}
