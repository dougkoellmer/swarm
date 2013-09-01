package swarm.client.view.tabs.account;

import swarm.client.app.smAppContext;
import swarm.client.view.smViewContext;
import swarm.client.view.tooltip.smE_ToolTipType;
import swarm.client.view.tooltip.smToolTipConfig;
import swarm.client.view.tooltip.smToolTipManager;
import swarm.client.view.widget.smCheckBox;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.CheckBox;

public class smStaySignedInCheckbox extends smCheckBox
{
	smStaySignedInCheckbox(smViewContext viewContext)
	{
		super(viewContext.clickMngr, "Remember Me");
		
		//this.getElement().getStyle().setMarginBottom(16, Unit.PX);
		this.setSize("100px", "20px"); // TODO: no, no, no
		
		smToolTipConfig config = new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Remembers you.");
		viewContext.toolTipMngr.addTip(this.getClickCatcher(), config);
		
		//this.get
		//this.getElement().getStyle().setCursor(Cursor.);
	}
}
