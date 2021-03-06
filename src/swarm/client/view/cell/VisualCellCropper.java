package swarm.client.view.cell;

import swarm.client.view.E_ZIndex;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;

public class VisualCellCropper extends FlowPanel
{
	//--- DRK > Just an optimization so we don't descend into browser guts to figure out
	//---		if it's already visible or not.
	private boolean m_isVisible = true;
	
	public VisualCellCropper()
	{
		this.addStyleName("sm_cell_cropper");
		
		E_ZIndex.CELL_CROPPER.assignTo(this);
		
		this.setVisible(false);
	}
	
	public void setPositionComponent(int index, double value)
	{
		switch(index)
		{
			case 0:  this.getElement().getStyle().setLeft(value, Unit.PX);  break;
			case 1:  this.getElement().getStyle().setTop(value, Unit.PX);  break;
		}
	}
	
	@Override
	public void setVisible(boolean visible)
	{
		if( visible && m_isVisible || !visible && !m_isVisible )
		{
			return;
		}
		
		super.setVisible(visible);
		
		m_isVisible = visible;
	}
}
