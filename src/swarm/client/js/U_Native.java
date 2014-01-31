package swarm.client.js;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;

public class U_Native
{
	private U_Native()
	{
		
	}
	
	public static native void removeSelection()
	/*-{
		   if (document.selection)
	        {
	            document.selection.empty();
	        }
	        else
	        {
	            window.getSelection().removeAllRanges();
	        }
	}-*/;
	
	public static void insertScript(String scriptSrc)
	{
		Document document = Document.get();
		ScriptElement element = document.createScriptElement();
		element.setType("text/javascript");
		element.setLang("javascript");
		
		document.getElementsByTagName("head").getItem(0).appendChild(element);
		
		element.setSrc(scriptSrc);
	}
	
	public static native JsArray getGlobalArray(String variableName)
	/*-{
			return $wnd[variableName];
	}-*/;
	
	public static native JavaScriptObject getGlobalObject(String variableName)
	/*-{
			return $wnd[variableName];
	}-*/;
	
	public static native void setGlobalObject(String variableName, JavaScriptObject value)
	/*-{
			$wnd[variableName] = value;
	}-*/;

}
