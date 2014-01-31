package swarm.client.view;

import swarm.client.app.PlatformInfo;
import swarm.client.app.AppContext;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Various utility methods, mostly for dealing with cross-browser nonsense.
 *
 * @author Doug
 *
 */
public class U_Css
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

	private U_Css()
	{

	}

	public static void setSize(com.google.gwt.dom.client.Element element, double width, double height)
	{
		element.getStyle().setWidth(width, Unit.PX);
		element.getStyle().setHeight(height, Unit.PX);
	}

	public static void setTransformOrigin(com.google.gwt.dom.client.Element m_actorWrapper, String transformOrigin)
	{
		for( int i = 0; i < TRANSFORM_ORIGIN_PROPERTIES.length; i++ )
		{
			//--- DRK > This is fucking ridiculous, but yea...
			m_actorWrapper.getStyle().setProperty(TRANSFORM_ORIGIN_PROPERTIES[i], transformOrigin);
		}
	}

	public static void setTransform(com.google.gwt.dom.client.Element m_actorWrapper, String transform)
	{
		for( int i = 0; i < TRANSFORM_PROPERTIES.length; i++ )
		{
			//--- DRK > This is fucking ridiculous, but yea...
			m_actorWrapper.getStyle().setProperty(TRANSFORM_PROPERTIES[i], transform);
		}
	}

	public static void setPosition(com.google.gwt.dom.client.Element element, double x, double y)
	{
		element.getStyle().setLeft(x, Unit.PX);
		element.getStyle().setTop(y, Unit.PX);
	}

	public static void setLinearGradient(com.google.gwt.dom.client.Element element, String from, String to)
	{
		element.getStyle().setProperty("background", "-webkit-gradient(linear, left top, left bottom, from("+from+"), to("+to+"))");
		element.getStyle().setProperty("background", "-moz-linear-gradient(top,  "+from+",  "+to+")");
		element.getStyle().setProperty("filter", "progid:DXImageTransform.Microsoft.gradient(startColorstr="+from+", endColorstr="+to+")");


		//background: -moz-linear-gradient(top,  #ccc,  #000);
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

	public static void leverageGPU(com.google.gwt.dom.client.Element element)
	{
		element.getStyle().setProperty("WebkitTransform", "scale3d(1, 1, 1)");
		element.getStyle().setProperty("MozTransform", "scale3d(1, 1, 1)");
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

	public static void allowUserSelect(Widget widget, boolean selectable)
	{
		allowUserSelect(widget.getElement(), selectable);
	}

	public static void allowUserSelect(Element element, boolean selectable)
	{
		if( selectable )
		{
			element.getStyle().setProperty("userSelect", "text");
			element.getStyle().setProperty("msUserSelect", "text");
			element.getStyle().setProperty("WebkitUserSelect", "text");
			element.getStyle().setProperty("OUserSelect", "text");
			element.getStyle().setProperty("MozUserSelect", "text");
		}
		else
		{
			element.getStyle().setProperty("userSelect", "none");
			element.getStyle().setProperty("msUserSelect", "none");
			element.getStyle().setProperty("WebkitUserSelect", "none");
			element.getStyle().setProperty("OUserSelect", "none");
			element.getStyle().setProperty("MozUserSelect", "none");
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
