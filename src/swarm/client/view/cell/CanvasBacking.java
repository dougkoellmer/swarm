package swarm.client.view.cell;

import swarm.shared.structs.BitArray;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;

public class CanvasBacking
{
	private final Canvas m_canvas = Canvas.createIfSupported();
	
	private CssColor m_fillStyle;
	private final CssColor m_gapStyle = CssColor.make("rgba(0,0,0,0)");
	
	public CanvasBacking()
	{
		
	}
	
	public void setColor(String color)
	{
		m_fillStyle = CssColor.make(color);
	}
	
	public Canvas getCanvas()
	{
		return m_canvas;
	}
	
	public void onResize(int width, int height)
	{
		m_canvas.setWidth(width + "px");
		m_canvas.setHeight(height + "px");
		m_canvas.setCoordinateSpaceWidth(width);
		m_canvas.setCoordinateSpaceHeight(height);
	}
	
	public void update(int startX, int startY, int startM, int startN, int across, int down, int cellSize, int gapSize, int totalGridSize, BitArray ownership)
	{
 		clear();
		
		Context2d context = m_canvas.getContext2d();
		context.setFillStyle(m_fillStyle);
		
		int totalCellSize = cellSize + gapSize;
		int limit_n = startN + down;
		int limit_m = startM + across;
//		limit_n = limit_n < totalGridSize ? limit_n : totalGridSize-1;
//		limit_m = limit_m < totalGridSize ? limit_m : totalGridSize-1;
//		
		for( int n = startN; n < limit_n; n++, startY+=totalCellSize )
		{
			int currX = startX;
			for( int m = startM; m < limit_m; m++, currX+=totalCellSize )
			{
				int index = n*totalGridSize + m;
				
				if( index > ownership.getBitCount() )
				{
					int blah = 10;
				}
				
				if( !ownership.isSet(index) )  continue;
				
				context.fillRect(currX, startY, cellSize, cellSize);
			}
		}
	}
	
	
	private void clear()
	{
		int width = m_canvas.getCoordinateSpaceWidth();
		int height = m_canvas.getCoordinateSpaceHeight();
		Context2d context = m_canvas.getContext2d();
		context.setFillStyle(m_gapStyle);
		context.clearRect(0, 0, width, height);
	}
}
