package swarm.client.view.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;

public class ImageButton extends BaseButton
{
	/*private Element m_imageElement;
	
	private boolean m_isMouseDown = false;
	
	public static smImageButton wrap(com.google.gwt.dom.client.Element element, String image, boolean preventCaching, boolean dimOnPress)
	{
	    // Assert that the element is attached.
	    assert Document.get().getBody().isOrHasChild(element);

	    smImageButton button = new smImageButton(element, image, preventCaching, dimOnPress);

	    // Mark it attached and remember it for cleanup.
	    button.onAttach();
	    RootPanel.detachOnWindowClose(button);

	    return button;
	}
	
	protected smImageButton(Element element, String image, boolean preventCaching, boolean dimOnPress)
	{
		super(element);
		
		init(image, preventCaching, dimOnPress);
	}
	
	public bhImageButton(String image, boolean preventCaching, boolean dimOnPress)
	{
		init(image, preventCaching, dimOnPress);
	}
	
	private void init(String image, boolean preventCaching, boolean dimOnPress)
	{
		getElement().getStyle().setBackgroundColor("rgba(0,0,0,0)");
		getElement().getStyle().setBorderWidth(0, Unit.PX);
		getElement().getStyle().setCursor(Cursor.POINTER);
		
		if( preventCaching )
		{
			m_imageElement = bhU_Image.createNonCachedImage(getElement(), image, 1);
		}
		else
		{
			m_imageElement = bhU_Image.createCachedImage(getElement(), image, 1);
		}
		
		this.getElement().appendChild(m_imageElement);
		
		if( dimOnPress )
		{
			this.addMouseDownHandler(new MouseDownHandler()
			{
				@Override
				public void onMouseDown(MouseDownEvent event)
				{
					dim();
					
					m_isMouseDown = true;
					
					Event.setCapture(event.getRelativeElement());
				}
			});
			
			this.addMouseUpHandler(new MouseUpHandler()
			{
				@Override
				public void onMouseUp(MouseUpEvent event)
				{
					Event.releaseCapture(event.getRelativeElement());
					
					unDim();
				}
			});
			
			this.addTouchStartHandler(new TouchStartHandler()
			{
				@Override
				public void onTouchStart(TouchStartEvent event)
				{
					dim();
					
					Event.setCapture(event.getRelativeElement());
				}
			});
			
			this.addTouchEndHandler(new TouchEndHandler()
			{
				@Override
				public void onTouchEnd(TouchEndEvent event)
				{
					if( event.getTouches().length() == 0 )
					{
						Event.releaseCapture(event.getRelativeElement());
						
						unDim();
					}
				}
			});
		}
	}
	
	private void dim()
	{
		m_imageElement.getStyle().setOpacity(.5);
	}
	
	private void unDim()
	{
		m_imageElement.getStyle().clearOpacity();
	}*/
}
