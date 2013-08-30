package swarm.client.view.widget;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Acts as a universal "blocking" screen for ui.
 * 
 * @author Doug
 *
 */
public class smUIBlocker extends FlowPanel
{
	private final HorizontalPanel	m_innerContainer = new HorizontalPanel();
	private final Label				m_label = new Label();
	private Widget m_widgetContent = null;
	
	public smUIBlocker()
	{
		this.addStyleName("sm_ui_blocker");
		m_innerContainer.addStyleName("sm_ui_blocker_inner");
		
		m_label.setWordWrap(false);
		
		this.add(m_innerContainer);
	}
	
	private void removeWidgetContent()
	{
		if( m_widgetContent != null && m_widgetContent.getParent() == m_innerContainer )
		{
			m_widgetContent.removeFromParent();
			m_widgetContent = null;
		}
	}
	
	private void removeLabelContent()
	{
		if( m_label.getParent() == m_innerContainer )
		{
			m_label.removeFromParent();
		}
	}
	
	private void ensureLabelAttachment()
	{
		if( m_label.getParent() != m_innerContainer )
		{
			m_innerContainer.add(m_label);
			m_innerContainer.setCellHorizontalAlignment(m_label, HasHorizontalAlignment.ALIGN_CENTER);
			m_innerContainer.setCellVerticalAlignment(m_label, HasVerticalAlignment.ALIGN_MIDDLE);
		}
	}
	
	public void setContent(Widget widget_nullable)
	{
		if( widget_nullable == null )
		{
			removeWidgetContent();
			
			if( m_label.getParent() == null )
			{
				this.setVisible(false);
			}
		}
		else
		{
			removeLabelContent();
			
			if( widget_nullable == m_widgetContent )  return;
			
			removeWidgetContent();
			
			m_innerContainer.add(widget_nullable);
			m_innerContainer.setCellHorizontalAlignment(widget_nullable, HasHorizontalAlignment.ALIGN_CENTER);
			m_innerContainer.setCellVerticalAlignment(widget_nullable, HasVerticalAlignment.ALIGN_MIDDLE);
			m_widgetContent = widget_nullable;
			
			this.setVisible(true);
		}
	}
	
	public void setHtml(String text_nullable)
	{
		if( text_nullable == null )
		{
			removeWidgetContent();
			m_label.getElement().setInnerHTML("");
			
			this.setVisible(false);
		}
		else
		{
			removeWidgetContent();
			ensureLabelAttachment();
			
			m_label.getElement().setInnerHTML(text_nullable);
			
			this.setVisible(true);
		}
	}
}
