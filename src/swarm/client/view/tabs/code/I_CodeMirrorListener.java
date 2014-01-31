package swarm.client.view.tabs.code;

public interface I_CodeMirrorListener
{
	void onChange();
	
	void onSave();
	
	void onPreview();
}