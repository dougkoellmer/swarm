package swarm.client.view;

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
public enum E_ZIndex
{
	DEFAULT,
	
	//--- DRK > These are still in css for various reasons, but put here for reference.
	TAB_SELECTED, 
	CODE_MIRROR_RELATED,
	
	//-- DRK > These are assigned manually to elements' styles.,
	CELL_BACKING,
	CELL_1_LOADING,
	CELL_128,
	CELL_64,
	CELL_32,
	CELL_16,
	CELL_8,
	CELL_4,
	CELL_2,
	CELL_1,
	CELL_META_ON_DEATH_ROW_ABOVE_CELL_1,
	CELL_PUSHED_BACK_DOWN,
	CELL_HIGHLIGHT,
	CELL_STATUS_BEHIND,
	CELL_CONTENT,
	CELL_STATUS,
	CELL_GLASS,
	CELL_CROPPER,
	CELL_FOCUSER,
	CELL_POPPED,
	CELL_SPLASH_GLASS,
	CELL_FOCUSED,
	CONSOLE_BLOCKER,
	CELL_HUD,
	MAGNIFIER,
	MAGNIFIER_DRAG_BUTTON,
	CONSOLE,
	MINIMIZER_MAXIMIZER,
	SPLITTER_GLASS,
	DIALOG_GLASS,
	DIALOG,
	TOOL_TIP_1,
	TOOL_TIP_2,
	TOOL_TIP_3,
	TOOL_TIP_4,
	TOOL_TIP_5;
	
	public int get()
	{
		return this.ordinal();
	}
	
	public void assignTo(IsWidget widget)
	{
		widget.asWidget().getElement().getStyle().setZIndex(this.get());
	}
	
	public void assignTo(IsWidget widget, Enum offset)
	{
		widget.asWidget().getElement().getStyle().setZIndex(this.get() + offset.ordinal());
	}
	
	public void assignTo(Element element)
	{
		element.getStyle().setZIndex(this.get());
	}
	
	public void assignTo(Element element, Enum offset)
	{
		element.getStyle().setZIndex(this.get() + offset.ordinal());
	}
}