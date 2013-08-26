package swarm.client.ui.tabs.account;

import swarm.client.app.sm_c;
import swarm.client.ui.tooltip.smE_ToolTipType;
import swarm.client.ui.tooltip.smToolTipConfig;
import swarm.client.ui.tooltip.smToolTipManager;
import swarm.client.ui.widget.smCheckBox;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.CheckBox;

public class smStaySignedInCheckbox extends smCheckBox
{
	smStaySignedInCheckbox()
	{
		super("Remember Me");
		
		//this.getElement().getStyle().setMarginBottom(16, Unit.PX);
		this.setSize("100px", "20px"); // TODO: no, no, no
		
		smToolTipConfig config = new smToolTipConfig(smE_ToolTipType.MOUSE_OVER, "Remembers you.");
		sm_c.toolTipMngr.addTip(this.getClickCatcher(), config);
		
		//this.get
		//this.getElement().getStyle().setCursor(Cursor.);
	}
}
