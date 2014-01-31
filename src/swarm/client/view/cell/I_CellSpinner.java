package swarm.client.view.cell;

import com.google.gwt.user.client.ui.IsWidget;

public interface I_CellSpinner extends IsWidget
{
	public void update(double timeStep);
	
	public void reset();
}
