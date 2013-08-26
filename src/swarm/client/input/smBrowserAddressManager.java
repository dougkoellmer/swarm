package swarm.client.input;

import com.google.gwt.user.client.Window;

public class smBrowserAddressManager
{
	public smBrowserAddressManager()
	{
		
	}
	
	public String getCurrentPathLowercased()
	{
		return getCurrentPath().toLowerCase();
	}
	
	public String getCurrentPath()
	{
		String path = Window.Location.getPath();
		
		if( path == null ) // DRK > Not sure if it can be null, just being anal.
		{
			path = "";
		}
		
		return path;
	}
}
