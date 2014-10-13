package swarm.client.view.cell;

import java.util.logging.Logger;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class SpritePlateAnimation
{
	private static final Logger s_logger = Logger.getLogger(SpritePlateAnimation.class.getName());
	
	private final double m_frameRate;
	private double m_time = 0.0;
	private int m_frame = 0;
	private final int m_frameCount;
	private final int m_framesAcross;
	private int m_frameWidth;
	private int m_frameHeight;
	
	private final ImageElement m_image;
	
	private boolean m_loaded = false;
	
	public SpritePlateAnimation(String imageUrl, double frameRate, int frameCount, int framesAcross)
	{
		m_frameRate = frameRate;
		m_frameCount = frameCount;
		m_framesAcross = framesAcross;

		Image img = new Image();
		m_image = ImageElement.as(img.getElement());
	    img.addLoadHandler(new LoadHandler()
	    {
	    	@Override public void onLoad(LoadEvent event)
	        {
	    		m_loaded = true;
	    		m_frameWidth = m_image.getWidth() / m_framesAcross;
	    		
	    		int framesDown = m_frameCount / m_framesAcross;
	    		framesDown += (m_frameCount % m_framesAcross) > 0 ? 1 : 0;
	    				
	    		m_frameHeight = m_image.getHeight() / framesDown;
	        }
	    });

	    img.setVisible(false);
	    RootPanel.get().add(img);
	    img.setUrl(imageUrl);
	}
	
	public void update(double timestep)
	{
		if( !m_loaded )  return;
		
		m_time += timestep;
		
		if( m_time >= m_frameRate )
		{
			m_time = 0.0;
			m_frame++;
			m_frame = m_frame % m_frameCount;
		}
	}
	
	public void draw(Context2d context, double timestep, int x, int y, double scaling)
	{
		if( !m_loaded )  return;
		
		int m = m_frame % m_framesAcross;
		int n = (m_frame) / m_framesAcross;
		int offsetX = m * m_frameWidth;
		int offsetY = n * m_frameHeight;
		int widthScaled = (int) (m_frameWidth * scaling);
		int heightScaled = (int) (m_frameHeight * scaling);
		
		x -= widthScaled/2;
		y -= heightScaled/2;
		
//		s_logger.severe(m_frame + " " + m + " " + n);
		
		context.drawImage(m_image, offsetX, offsetY, m_frameWidth, m_frameHeight, x, y, widthScaled, heightScaled);
	}
}
