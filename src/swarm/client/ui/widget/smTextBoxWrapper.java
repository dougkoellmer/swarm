package swarm.client.ui.widget;

import swarm.client.ui.smS_UI;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * This class exists to solve problems laying out text fields with width = 100%
 * 
 * @author Doug
 *
 */
public class smTextBoxWrapper extends FlowPanel
{
	private final TextBox m_textBox;
	
	public smTextBoxWrapper(TextBox textBox)
	{
		m_textBox = textBox;
		
		//m_textBox.setWidth("100%");
		
		this.getElement().getStyle().setPaddingRight(smS_UI.MAGIC_TEXT_INPUT_PADDING, Unit.PX);
		
		this.add(m_textBox);
	}
	
	public TextBox getTextBox()
	{
		return m_textBox;
	}
	
	public String getText()
	{
		return m_textBox.getText();
	}
}
