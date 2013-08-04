package b33hive.client.app;

import com.google.gwt.user.client.Window;

public class bhPlatformInfo
{
	private final bhE_Platform m_platform;
	
	private String m_rawUserAgent;
	private String m_rawPlatform;
	private String m_cssTransform;
	
	private boolean m_has3dTransforms = false;
	
	public bhPlatformInfo()
	{
		m_rawUserAgent = Window.Navigator.getUserAgent().toLowerCase();
		m_rawPlatform = Window.Navigator.getPlatform();

		if( isIOS() )
		{
			m_platform = bhE_Platform.IOS;
		}
		else if( m_rawUserAgent.indexOf("chrome") != -1 )
		{
			m_platform = bhE_Platform.CHROME;
		}
		else
		{
			m_platform = bhE_Platform.OTHER;
		}
		
		m_has3dTransforms = has3dTransforms_native();
		
		m_cssTransform = getCssTransform();
	}
	
	public String getTransformProperty()
	{
		return m_cssTransform;
	}
	
	private native String getCssTransform()
	/*-{
			return $wnd.Modernizr.prefixed("transform");
	}-*/;
	
	private static native boolean has3dTransforms_native()
	/*-{
		    var el = $doc.createElement('p'),
		    has3d,
		    transforms = {
		        'webkitTransform':'-webkit-transform',
		        'OTransform':'-o-transform',
		        'msTransform':'-ms-transform',
		        'MozTransform':'-moz-transform',
		        'transform':'transform'
		    };
		 
		    // Add it to the body to get the computed style
		    $doc.body.insertBefore(el, null);
		 
		    for(var t in transforms){
		        if( el.style[t] !== undefined ){
		            el.style[t] = 'translate3d(1px,1px,1px)';
		            has3d = window.getComputedStyle(el).getPropertyValue(transforms[t]);
		        }
		    }
		 
		    $doc.body.removeChild(el);
		 
		    return (has3d !== undefined && has3d.length > 0 && has3d !== "none");
	}-*/;
	
	private native boolean isIOS()
	/*-{
			if( navigator.userAgent.match(/(iPhone|iPod|iPad)/i) )
			{
				return true;
			}
			else
			{
				return false;
			}
	}-*/;
	
	public String getRawPlatform()
	{
		return m_rawUserAgent;
	}
	
	public bhE_Platform getPlatform()
	{
		return m_platform;
	}
	
	public boolean isSupportedPlatform()
	{
		return m_platform == bhE_Platform.CHROME;
	}
	
	public boolean isGPUEnabled()
	{
		return m_platform == bhE_Platform.CHROME;
	}
	
	public boolean has3dTransforms()
	{
		return m_has3dTransforms;
	}
}
