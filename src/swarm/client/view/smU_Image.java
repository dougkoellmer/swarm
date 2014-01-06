package swarm.client.view;

import swarm.client.js.bhU_Caching;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;

public class smU_Image
{
	private static Element createImage(Element container, String imagePath, int zIndex, boolean cached)
	{
		Element part = DOM.createElement("img");
		part.getStyle().setZIndex(zIndex);
		part.getStyle().setPosition(Position.ABSOLUTE);
		String path = cached ? imagePath : imagePath + bhU_Caching.calcRandomVersion();
		part.setAttribute("src", path);
		
		if( container != null )
		{
			container.appendChild(part);
		}
		
		part.getStyle().setLeft(0, Unit.PX);
		part.getStyle().setTop(0, Unit.PX);
		
		return part;
	}
	
	public static Element createNonCachedImage(Element container, String imagePath, int zIndex)
	{
		return createImage(container, imagePath, zIndex, false);
	}
	
	public static Element createCachedImage(Element container, String imagePath, int zIndex)
	{
		return createImage(container, imagePath, zIndex, true);
	}
}
