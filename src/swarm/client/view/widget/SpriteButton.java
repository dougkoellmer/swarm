package swarm.client.view.widget;

import com.google.gwt.dom.client.Element;

public class SpriteButton extends BaseButton
{
	private static final String INNER_HTML = "<table style='width:100%; height:100%;'><tr><td style='width:100%; height:100%;'><div></div></div></td></tr></table>";
	private String m_spriteId = null;
	private Element m_sprite;
	
	public SpriteButton(String spriteId)
	{
		this.getElement().setInnerHTML(INNER_HTML);
		
		//--- DRK > Wow, what a hack.
		m_sprite = this.getElement().getFirstChildElement().getFirstChildElement().getFirstChildElement().getFirstChildElement().getFirstChildElement();
		m_sprite.addClassName("sm_icon");
		
		this.setSpriteId(spriteId);
	}
	
	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		
		if( enabled )
		{
			m_sprite.getStyle().setOpacity(1);
		}
		else
		{
			m_sprite.getStyle().setOpacity(.5);
		}
	}
	
	private static String calcStyleName(String spriteId, boolean enabled)
	{
		if( !enabled )
		{
			spriteId += "_disabled";
		}
		
		return "sm_icon-" + spriteId + "-png";
	}
	
	public void setSpriteId(String spriteId)
	{
		if( m_spriteId != null )
		{
			m_sprite.removeClassName(calcStyleName(m_spriteId, this.isEnabled()));
		}
		
		m_spriteId = spriteId;
		
		if( m_spriteId != null )
		{
			m_sprite.addClassName(calcStyleName(m_spriteId, this.isEnabled()));
		}
	}
}
