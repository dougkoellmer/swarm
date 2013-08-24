package swarm.client.ui;

import swarm.client.ui.widget.bhUIBlocker;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

/**
 * Acts as a universal "blocking" screen for any console tab screens that involve blocking transactions or whatever.
 * 
 * @author Doug
 *
 */
public class bhConsoleBlocker extends bhUIBlocker
{
	private static bhConsoleBlocker s_instance = null;
	
	private bhConsoleBlocker()
	{
		bhE_ZIndex.CONSOLE_BLOCKER.assignTo(this);
	}
	
	public static bhConsoleBlocker getInstance()
	{
		if( s_instance == null)
		{
			s_instance = new bhConsoleBlocker();
		}
		
		return s_instance;
	}
	
	public void detachFrom(Panel panel)
	{
		if( panel == this.getParent() )
		{
			this.removeFromParent();
		}
	}
	
	public void attachTo(Panel panel)
	{
		if( this.getParent() != null )
		{
			if( panel == this.getParent() )
			{
				return;
			}
			
			this.removeFromParent();
		}
		
		panel.add(this);
	}
}
