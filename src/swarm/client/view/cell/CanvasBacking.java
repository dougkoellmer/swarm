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
	
	public static class UpdateConfig
	{
		double startX_meta, startY_meta;
		int startM, startN;
		int across, down;
		double cellSize;
		double cellSizePlusPadding;
		int totalGridSize;
		BitArray ownership;
		double metaCellSize;
		int metaSubCellCount;
		int startM_meta, startN_meta;
		double scaling;
		double timestep;
		
		public void set
		(
			double startX_meta, double startY_meta, int startM, int startN, int across, int down, double cellSize,
			double cellSizePlusPadding, int totalGridSize, BitArray ownership, double metaCellSize, int metaSubCellCount,
			int startM_meta, int startN_meta, double scaling, double timestep
		)
		{
			this.startX_meta = startX_meta;
			this.startY_meta = startY_meta;
			this.startM = startM;
			this.startN = startN;
			this.across = across;
			this.down = down;
			this.cellSize = cellSize;
			this.cellSizePlusPadding = cellSizePlusPadding;
			this.totalGridSize = totalGridSize;
			this.ownership = ownership;
			this.metaCellSize = metaCellSize;
			this.metaSubCellCount = metaSubCellCount;
			this.startM_meta = startM_meta;
			this.startN_meta = startN_meta;
			this.scaling = scaling;
			this.timestep = timestep;
		}
	}
	
	private final Canvas m_canvas = Canvas.createIfSupported();
	
	private final Canvas m_stageCanvas = Canvas.createIfSupported();
	private final CanvasElement m_stageCanvasElement;
	
	private CssColor m_fillStyle;
	private final I_Skipper m_skipper;
	private final SpritePlateAnimation m_animation;
	private final double m_pinch;
	
	private int m_width = 0;
	private int m_height = 0;
	private boolean m_clear = true;
	
	public CanvasBacking(SpritePlateAnimation animation, String cellBackgroundColor, int maxCellWidth, int maxCellHeight, double pinch, I_Skipper skipper)
	{
		m_skipper = skipper;
		m_animation = animation;
		m_pinch = pinch;
		
		m_stageCanvasElement = m_stageCanvas.getCanvasElement();
		
		setCanvasSize(m_stageCanvas, maxCellWidth, maxCellHeight);
		
		setColor(cellBackgroundColor);
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
	
	public void update(UpdateConfig config)
	{
 		clear();
		
		Context2d context = m_canvas.getContext2d();
		Context2d stageContext = m_stageCanvas.getContext2d();
		double cellSize = config.cellSize;
		cellSize -= m_pinch*2;
		
		double cellSize_div2 = cellSize/2;
		
		int limit_n = config.startN + config.down;
		int limit_m = config.startM + config.across;
		
		boolean foundFirstCell = false;
//		
		for( int n = config.startN; n < limit_n; n++ )
		{
			for( int m = config.startM; m < limit_m; m++ )
			{
				int skip = m_skipper.skip(m, n);
				
				if( skip == 1 )
				{
					continue;
				}
				else if( skip > 1 )
				{
					m += skip;
					m -= 1; // will get incremented in for loop so offsetting here.
					
					continue;
				}
				
				int index = n*config.totalGridSize + m;
				
				if( !config.ownership.isSet(index) )  continue;
				
				int offsetM = m - config.startM_meta;
				int offsetN = n - config.startN_meta;
				int offsetM_mod = offsetM % config.metaSubCellCount;
				int offsetN_mod = offsetN % config.metaSubCellCount;
				offsetM -= offsetM_mod;
				offsetN -= offsetN_mod;
				offsetM /= config.metaSubCellCount;
				offsetN /= config.metaSubCellCount;
				
				double currX = config.startX_meta + offsetM * config.metaCellSize + offsetM_mod * config.cellSizePlusPadding;
				double currY = config.startY_meta + offsetN * config.metaCellSize + offsetN_mod * config.cellSizePlusPadding;
				currX += m_pinch;
				currY += m_pinch;
				
				if( !foundFirstCell )
				{
					stageContext.fillRect(0, 0, cellSize, cellSize);
					
					if( m_animation != null )
					{
						m_animation.draw(stageContext, (int)cellSize_div2, (int)cellSize_div2, config.scaling);
					}
					
					foundFirstCell = true;
				}

				context.drawImage(m_stageCanvasElement, 0, 0, cellSize, cellSize, currX, currY, cellSize, cellSize);
				m_clear = false;
			}
		}
		
		m_animation.update(config.timestep);
	}
	
	
	private void clear()
	{
		if( m_clear )  return;
		
		int width = m_canvas.getCoordinateSpaceWidth();
		int height = m_canvas.getCoordinateSpaceHeight();
		Context2d context = m_canvas.getContext2d();
		context.clearRect(0, 0, width, height);
		
		m_clear = true;
	}
}
