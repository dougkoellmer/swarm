package swarm.client.view.cell;

import swarm.shared.structs.BitArray;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.ui.RootPanel;

public class CanvasBacking
{
	public static interface I_Skipper
	{
		int skip(int m, int n);
	}
	
	private final Canvas m_canvas = Canvas.createIfSupported();
	
	private final Canvas m_stageCanvas = Canvas.createIfSupported();
	private final CanvasElement m_stageCanvasElement;
	
	private CssColor m_fillStyle;
	private final I_Skipper m_skipper;
	private final SpritePlateAnimation m_animation;
	
	private int m_width = 0;
	private int m_height = 0;
	
	public CanvasBacking(SpritePlateAnimation animation, String cellBackgroundColor, int maxCellWidth, int maxCellHeight, I_Skipper skipper)
	{
		m_skipper = skipper;
		m_animation = animation;
		
		m_stageCanvasElement = m_stageCanvas.getCanvasElement();
		
		setCanvasSize(m_stageCanvas, maxCellWidth, maxCellHeight);
		
		setColor(cellBackgroundColor);
		
//		RootPanel.get().add(m_stageCanvas);
//		m_stageCanvasElement.getStyle().setZIndex(1000);
//		m_stageCanvasElement.getStyle().setPosition(Position.FIXED);
	}
	
	public void setColor(String color)
	{
		m_fillStyle = CssColor.make(color);
		
		Context2d stageContext = m_stageCanvas.getContext2d();
		stageContext.setFillStyle(m_fillStyle);
	}
	
	public Canvas getCanvas()
	{
		return m_canvas;
	}
	
	public void onResize(int width, int height)
	{
		if( width <= m_width && height <= m_height )  return;
		
		m_width = width > m_width ? width : m_width;
		m_height = height > m_height ? height : m_height;
		
		setCanvasSize(m_canvas, m_width, m_height);
	}
	
	private static void setCanvasSize(Canvas canvas, int width, int height)
	{
		canvas.setWidth(width + "px");
		canvas.setHeight(height + "px");
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);
	}
	
	public void update(double startX_meta, double startY_meta, int startM, int startN, int across, int down, double cellSize, double cellSizePlusPadding, int totalGridSize, BitArray ownership, double metaCellSize, int metaSubCellCount, int startM_meta, int startN_meta, double scaling, double timestep)
	{
 		clear();
		
 		final double pinch = .75;
		Context2d context = m_canvas.getContext2d();
		Context2d stageContext = m_stageCanvas.getContext2d();
		cellSize -= pinch*2;
		
		double cellSize_div2 = cellSize/2;
		
		int limit_n = startN + down;
		int limit_m = startM + across;
		
		boolean foundFirstCell = false;
//		
		for( int n = startN; n < limit_n; n++ )
		{
			for( int m = startM; m < limit_m; m++ )
			{
				int skip = m_skipper.skip(m, n);
				
				if( skip == 1 )
				{
					continue;
				}
				else if( skip > 1 )
				{
					m += skip;
					m -= 1;
					
					continue;
				}
				
				int index = n*totalGridSize + m;
				
				if( !ownership.isSet(index) )  continue;
				
				int offsetM = m - startM_meta;
				int offsetN = n - startN_meta;
				int offsetM_mod = offsetM % metaSubCellCount;
				int offsetN_mod = offsetN % metaSubCellCount;
				offsetM -= offsetM_mod;
				offsetN -= offsetN_mod;
				offsetM /= metaSubCellCount;
				offsetN /= metaSubCellCount;
				
				double currX = startX_meta + offsetM * metaCellSize + offsetM_mod * cellSizePlusPadding;
				double currY = startY_meta + offsetN * metaCellSize + offsetN_mod * cellSizePlusPadding;
				currX += pinch;
				currY += pinch;
				
				
//				context.fillRect(currX, currY, cellSize, cellSize);
//				
//				if( m_animation != null )
//				{
//					currX += cellSize_div2;
//					currY += cellSize_div2;
//					m_animation.draw(context, timestep, (int)currX, (int)currY, scaling);
//				}
				
				
				
				if( !foundFirstCell )
				{
					stageContext.fillRect(0, 0, cellSize, cellSize);
					
					if( m_animation != null )
					{
						m_animation.draw(stageContext, timestep, (int)cellSize_div2, (int)cellSize_div2, scaling);
					}
					
					foundFirstCell = true;
				}

				context.drawImage(m_stageCanvasElement, 0, 0, cellSize, cellSize, currX, currY, cellSize, cellSize);
			}
		}
		
		m_animation.update(timestep);
	}
	
	
	private void clear()
	{
		int width = m_canvas.getCoordinateSpaceWidth();
		int height = m_canvas.getCoordinateSpaceHeight();
		Context2d context = m_canvas.getContext2d();
		context.clearRect(0, 0, width, height);
	}
}
