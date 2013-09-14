package swarm.client.view;

import swarm.client.app.smPlatformInfo;
import swarm.client.app.smAppContext;

import com.google.gwt.user.client.Element;

/**
 * Various utility methods, mostly for dealing with cross-browser nonsense.
 * 
 * @author Doug
 *
 */
public class smU_Css
{
	private static final String[] TRANSFORM_PROPERTIES = 
	{
		"transform",
		"msTransform",
		"WebkitTransform",
		"OTransform",
		"MozTransform"
	};
	
	private static final String[] TRANSFORM_ORIGIN_PROPERTIES = 
	{
		"transformOrigin",
		"msTransformOrigin",
		"WebkitTransformOrigin",
		"OTransformOrigin",
		"MozTransformOrigin"
	};
	
	private smU_Css()
	{
		
	}
	
	public static void toggleSelectability(Element element, boolean selectable)
	{
		if( selectable )
		{
			element.removeAttribute("ondragstart");
			element.removeAttribute("onselectstart");
		}
		else
		{
			element.setAttribute("ondragstart", "return false;");
			element.setAttribute("onselectstart", "return false;");
		}
	}
	
	public static void setTransformOrigin(Element element, String x, String y)
	{
		String value = x + " " + y;
		for( int i = 0; i < TRANSFORM_ORIGIN_PROPERTIES.length; i++ )
		{
			//--- DRK > This is fucking ridiculous, but yea...
			element.getStyle().setProperty(TRANSFORM_ORIGIN_PROPERTIES[i], value);
		}
	}
	
	public static String createScaleTransform(double scale, boolean use3d)
	{
		return createScaleTransform(scale, scale, use3d);
	}
	
	public static String createTranslateTransform(double xValue, double yValue, boolean use3d)
	{
		if( use3d )
		{
			return "translate3d("+xValue+"px,"+yValue+"px, 0px)";
		}
		else
		{
			return "translate("+xValue+"px,"+yValue+"px)";
		}
	}
	
	public static String createScaleTransform(double xScale, double yScale, boolean use3d)
	{
		if( use3d )
		{
			return "scale3d("+xScale+","+yScale+", 1)";
		}
		else
		{
			return "scale("+xScale+","+yScale+")";
		}
	}
	
	public static String createRotate2dTransform(double degrees, boolean use3d)
	{
		if( use3d )
		{
			return "rotate3d(0, 0, 1, "+degrees+"deg)";
		}
		else
		{
			return "rotate("+degrees+"deg)";
		}
	}
	
	/*public static void removeScaleTransform(Element element)
	{
		for( int i = 0; i < TRANSFORM_PROPERTIES.length; i++ )
		{
			//--- DRK > This is fucking ridiculous, but yea...
			element.getStyle().clearProperty(TRANSFORM_PROPERTIES[i]);
		}
	}*/
	
	public static void setBoxShadow(Element element, String value)
	{
		element.getStyle().setProperty("boxShadow", value);
		element.getStyle().setProperty("MozBoxShadow", value);
		element.getStyle().setProperty("WebkitBoxShadow", value);
	}
}
