package swarm.client.ui.tabs.code;

import java.util.logging.Logger;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

public class bhCodeMirrorWrapper extends Widget
{
	private static final Logger s_logger = Logger.getLogger(bhCodeMirrorWrapper.class.getName());
	
	private String m_id = null;
	private Element m_hostElement;
	
	private bhI_CodeMirrorListener m_listener = null;
	
	private JavaScriptObject m_codeMirror = null; // a native object reference to the editor
	
	private boolean m_suppressOnChange = false;
	
	private final boolean m_readOnly;
	
	public bhCodeMirrorWrapper(bhI_CodeMirrorListener listener, boolean readOnly)
	{
		super();
		
		m_hostElement = DOM.createDiv();
		setElement(m_hostElement);
		
		this.setStyleName("code_mirror");
	
		m_listener = listener;
		
		m_readOnly = readOnly;
		
		m_codeMirror = this.initCodeMirror(m_readOnly);
	}
	
	@Override
	public void onLoad()
	{
		super.onLoad();
	}
	
	private native JavaScriptObject initCodeMirror(boolean readOnly)
	/*-{
		var self = this;
		
		var onChange = function()
		{
			self.@swarm.client.ui.tabs.code.bhCodeMirrorWrapper::onChange()();
		}
		
		var onSave = function()
		{
			self.@swarm.client.ui.tabs.code.bhCodeMirrorWrapper::onSave()();
		}
		
		var onPreview = function()
		{
			self.@swarm.client.ui.tabs.code.bhCodeMirrorWrapper::onPreview()();
		}
		
		var config = 
		{
			mode:"htmlmixed",
			lineNumbers:"true",
			readOnly:readOnly,
			indentWithTabs:true,
			indentUnit:4,
			extraKeys:
			{
			    "Ctrl-S":onSave,
			    "Ctrl-P":onPreview
			}
		};
		
		var hostElement = this.@swarm.client.ui.tabs.code.bhCodeMirrorWrapper::m_hostElement;
		
		var codeMirror = $wnd.CodeMirror(hostElement, config);
		
		if( !readOnly )
		{
			codeMirror.on("change", onChange);
		}
		
		return codeMirror;
	}-*/;
	
	private void onChange()
	{
		if( !m_suppressOnChange )
		{
			m_listener.onChange();
		}
	}
	
	private void onSave()
	{
		m_listener.onSave();
	}
	
	private void onPreview()
	{
		m_listener.onPreview();
	}
	
	public native void setCodeMirrorHeight(String height)
	/*-{
			var codeMirror = this.@swarm.client.ui.tabs.code.bhCodeMirrorWrapper::m_codeMirror;
			codeMirror.setSize(null, height);
	}-*/;
	
	public native String getContent()
	/*-{
			var codeMirror = this.@swarm.client.ui.tabs.code.bhCodeMirrorWrapper::m_codeMirror;
			return codeMirror.getValue();
	}-*/;
	
	public void setContent(String content)
	{
		m_suppressOnChange = true;
		{
			native_setContent(content);
		}
		m_suppressOnChange = false;
	}
	
	private native void native_setContent(String content)
	/*-{
			var codeMirror = this.@swarm.client.ui.tabs.code.bhCodeMirrorWrapper::m_codeMirror;
			codeMirror.setValue(content);
	}-*/;
	
	public native void focus()
	/*-{
			var codeMirror = this.@swarm.client.ui.tabs.code.bhCodeMirrorWrapper::m_codeMirror;
			codeMirror.focus();
	}-*/;
	
	public native void refresh()
	/*-{
			if (document.createEvent)// W3C
			{
			    var ev = document.createEvent('Event');
			    ev.initEvent('resize', true, true);
			    $wnd.dispatchEvent(ev);
			}
			else // IE
			{
			    element=document.documentElement;
			    var event=document.createEventObject();
			    element.fireEvent("onresize",event);
			}
	
			var codeMirror = this.@swarm.client.ui.tabs.code.bhCodeMirrorWrapper::m_codeMirror;
			codeMirror.refresh();
	}-*/;
	
	public native void blur()
	/*-{
			var codeMirror = this.@swarm.client.ui.tabs.code.bhCodeMirrorWrapper::m_codeMirror;
			codeMirror.getInputField().blur();
	}-*/;
}
