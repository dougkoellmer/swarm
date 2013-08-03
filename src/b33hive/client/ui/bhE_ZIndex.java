package b33hive.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * All (well, most) z-indices are kept here in order to have a central location
 * so it's easier to see what should be on top of what.  With everything in css,
 * it was getting confusing.
 * 
 * @author Doug
 *
 */
public enum bhE_ZIndex
{
	DEFAULT,
	
	//--- DRK > These are still in css for various reasons, but put here for reference.
	TAB_SELECTED, 
	CODE_MIRROR_RELATED,
	
	CELL_CONTENT,
	CELL_STATUS,
	CELL_GLASS,
	CELL_CROPPER,
	CELL_HIGHLIGHT,
	CELL_FOCUSER,
	CELL_SPLASH_GLASS,
	CELL_POPPED,
	CONSOLE_BLOCKER,
	CELL_HUD,
	MAGNIFIER,
	MAGNIFIER_DRAG_BUTTON,
	MINIMIZER_MAXIMIZER,
	DIALOG_GLASS,
	DIALOG,
	TOOL_TIP_1,
	TOOL_TIP_2,
	TOOL_TIP_3,
	TOOL_TIP_4,
	TOOL_TIP_5;
	
	public void assignTo(IsWidget widget)
	{
		widget.asWidget().getElement().getStyle().setZIndex(this.ordinal());
	}
	
	public void assignTo(IsWidget widget, Enum offset)
	{
		widget.asWidget().getElement().getStyle().setZIndex(this.ordinal() + offset.ordinal());
	}
	
	public void assignTo(Element element, Enum offset)
	{
		element.getStyle().setZIndex(this.ordinal() + offset.ordinal());
	}
}