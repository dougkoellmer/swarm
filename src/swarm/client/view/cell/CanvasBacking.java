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
	
	private int m_color = 0xFFFFFFFF;
	private BitArray m_bitArray = null;
	private int m_logicalGridSize;
	private int m_cellSize;
	private int m_gapSize;
	private int m_physicalGridSize;
	private CssColor m_fillStyle;
	private final CssColor m_gapStyle = CssColor.make("rgba(0,0,0,0)");
	
	public CanvasBacking()
	{
		
	}
	
	public void set(int color, int logicalGridSize, int physicalGridSize, int logicalCellSize, int gapSize, BitArray array_cloned)
	{
		m_logicalGridSize = logicalGridSize;
		m_physicalGridSize = physicalGridSize;
		m_color = color;
		m_cellSize = logicalCellSize;
		m_gapSize = gapSize;
		String int_hexed = Integer.toHexString(color);
		m_fillStyle = CssColor.make(int_hexed);
		
		updateSize();
		
		if( m_bitArray != null )
		{
			clear();
		}
		m_bitArray = array_cloned.clone();
		
		draw();
	}
	
	private void clear()
	{
		Context2d context = m_canvas.getContext2d();
		context.setFillStyle(m_gapStyle);
		context.fillRect(0, 0, m_logicalGridSize, m_logicalGridSize);
	}
	
	private void updateSize()
	{
		m_canvas.setWidth(m_physicalGridSize + "px");
		m_canvas.setHeight(m_physicalGridSize + "px");
		m_canvas.setCoordinateSpaceWidth(m_logicalGridSize);
		m_canvas.setCoordinateSpaceWidth(m_physicalGridSize);
	}
	
	private void draw()
	{
		Context2d context = m_canvas.getContext2d();
		context.setFillStyle(m_fillStyle);
		
		if( m_bitArray == null )  return;
		
		int totalCellSize = m_cellSize + m_gapSize;
		
		for(int i = 0; i < m_bitArray.getBitCount(); i++ )
		{
			int modWidth = i % m_logicalGridSize;
			int row = (i - modWidth) / m_logicalGridSize;
			int col = modWidth;
			
			int row_canvas = row * totalCellSize;
			int col_canvas = col * totalCellSize;
			
			context.fillRect(row_canvas, col_canvas, m_cellSize, m_cellSize);
		}
	}
}
