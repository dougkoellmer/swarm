package b33hive.client.ui.cell;

import java.util.ArrayList;
import java.util.logging.Logger;

import b33hive.client.entities.bhI_BufferCellListener;
import b33hive.client.ui.bhE_ZIndex;
import b33hive.client.ui.bhS_UI;
import b33hive.client.ui.bhU_UI;
import b33hive.client.ui.tabs.code.bhHtmlSandbox;
import b33hive.client.ui.tabs.code.bhI_CodeLoadListener;
import b33hive.client.ui.widget.bhUIBlocker;
import b33hive.shared.app.bhS_App;
import b33hive.shared.utils.bhU_BitTricks;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.entities.bhE_CodeSafetyLevel;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.structs.bhCode;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;

public class bhVisualCell extends AbsolutePanel implements bhI_BufferCellListener
{
	private static final Logger s_logger = Logger.getLogger(bhVisualCell.class.getName());
	
	//private static final String SPINNER_HTML = "<img src='/r.img/spinner.gif?v=1' />";
	
	private static int s_currentId = 0;
	
	private static final class CodeLoadListener implements bhI_CodeLoadListener
	{
		private final bhVisualCell m_this;
		
		CodeLoadListener(bhVisualCell thisArg)
		{
			m_this = thisArg;
		}
		
		@Override
		public void onCodeLoad()
		{
			m_this.setStatusHtml(null, false);
		}
	}
	
	private int m_id;
	private final AbsolutePanel m_contentPanel = new AbsolutePanel();
	private final AbsolutePanel m_backgroundPanel = new AbsolutePanel();
	private final bhUIBlocker	m_statusPanel = new bhUIBlocker();
	private final AbsolutePanel m_glassPanel = new AbsolutePanel();
	private final bhCellSpinner m_spinner	= new bhCellSpinner(bhS_UI.SPINNER_ROTATION_RATE);
	
	private final ArrayList<Image> m_backgroundImages = new ArrayList<Image>();
	private int m_currentImageIndex = -1;
	
	private static final String CELL_PLUS_SPACING_PIXEL_COUNT = bhS_App.CELL_PLUS_SPACING_PIXEL_COUNT + "px";
	private static final String CELL_PIXEL_COUNT = bhS_App.CELL_PIXEL_COUNT + "px";
	
	private int m_cellSize = -1;
	private boolean m_isValidated = false;
	
	private final CodeLoadListener m_codeLoadListener = new CodeLoadListener(this);
	
	public bhVisualCell()
	{
		m_id = s_currentId; s_currentId++;
		
		this.addStyleName("visual_cell");
		m_glassPanel.addStyleName("bh_cell_glass");

		m_backgroundPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
		m_statusPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
		m_glassPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
		
		m_contentPanel.setSize(CELL_PIXEL_COUNT, CELL_PIXEL_COUNT);
		m_statusPanel.setSize(CELL_PIXEL_COUNT, CELL_PIXEL_COUNT);
		
		bhE_ZIndex.CELL_STATUS.assignTo(m_statusPanel);
		bhE_ZIndex.CELL_GLASS.assignTo(m_glassPanel);
		bhE_ZIndex.CELL_CONTENT.assignTo(m_contentPanel);
		
		m_statusPanel.setVisible(false);
		
		int maxImages = bhS_App.MAX_CELL_IMAGES;
		int currentBit = 1;
		for( int i = 0; i <= 1; i++ )
		{
			Image image = new Image();
			image.setUrl("/r.img/cell_size_" + currentBit + ".png");
			image.getElement().getStyle().setDisplay(Display.NONE);
			m_backgroundImages.add(image);
			m_backgroundPanel.add(image);
			
			currentBit <<= 1;
		}
		
		bhU_UI.toggleSelectability(m_backgroundPanel.getElement(), false);
		this.allowUserSelect(false);
		
		this.add(m_backgroundPanel);
		this.add(m_contentPanel);
		this.add(m_statusPanel);
		this.add(m_glassPanel);
	}
	
	int getId()
	{
		return m_id;
	}
	
	private void allowUserSelect(boolean selectable)
	{
		if( selectable )
		{
			m_contentPanel.getElement().getStyle().setProperty("userSelect", "text");
			m_contentPanel.getElement().getStyle().setProperty("msUserSelect", "text");
			m_contentPanel.getElement().getStyle().setProperty("WebkitUserSelect", "text");
			m_contentPanel.getElement().getStyle().setProperty("OUserSelect", "text");
			m_contentPanel.getElement().getStyle().setProperty("MozUserSelect", "text");
			
		}
		else
		{
			m_contentPanel.getElement().getStyle().setProperty("userSelect", "none");
			m_contentPanel.getElement().getStyle().setProperty("msUserSelect", "none");
			m_contentPanel.getElement().getStyle().setProperty("WebkitUserSelect", "none");
			m_contentPanel.getElement().getStyle().setProperty("OUserSelect", "none");
			m_contentPanel.getElement().getStyle().setProperty("MozUserSelect", "none");
		}
	}
	
	public void update(double timeStep)
	{
		if( m_spinner.getParent() != null )
		{
			m_spinner.update(timeStep);
		}
	}
	
	public static double calcCellScaling(double distanceRatio, int cellSize)
	{
		if( cellSize == 1 )
		{
			return distanceRatio;
		}
		else
		{
			return (distanceRatio * bhS_App.SCALING_RATIO) * ((double)cellSize);
		}
	}
	
	void validate()
	{
		if( m_isValidated )  return;
		
		if( m_currentImageIndex != -1 )
		{
			m_backgroundImages.get(m_currentImageIndex).getElement().getStyle().setDisplay(Display.NONE);
		}
		
		if( m_cellSize == 1 )
		{
			this.setSize(CELL_PLUS_SPACING_PIXEL_COUNT, CELL_PLUS_SPACING_PIXEL_COUNT);
			m_backgroundPanel.setSize(CELL_PLUS_SPACING_PIXEL_COUNT, CELL_PLUS_SPACING_PIXEL_COUNT);
			
			m_currentImageIndex = 0;
			m_backgroundImages.get(m_currentImageIndex).getElement().getStyle().setDisplay(Display.BLOCK);
			
			//--- DRK > Rare case of jumping from beyond max imaged zoom to all the way to cell size 1,
			//---		but could technically happen with bad frame rate or something, so clearing this here just in case.
			m_backgroundPanel.getElement().getStyle().clearBackgroundColor();
			
			m_contentPanel.addStyleName("visual_cell_content");
		}
		else if( m_cellSize > 1 )
		{
			this.setStatusHtml(null, false); // shouldn't have to be done, but what the hell.
			
			this.setSize(CELL_PIXEL_COUNT, CELL_PIXEL_COUNT);
			m_backgroundPanel.setSize(CELL_PIXEL_COUNT, CELL_PIXEL_COUNT);
			
			if( m_cellSize > bhS_App.MAX_IMAGED_CELL_SIZE )
			{				
				m_backgroundPanel.getElement().getStyle().setBackgroundColor("white");
			}
			else
			{
				m_backgroundPanel.getElement().getStyle().clearBackgroundColor();
				
				m_currentImageIndex = bhU_BitTricks.calcBitPosition(m_cellSize);
				m_backgroundImages.get(m_currentImageIndex).getElement().getStyle().setDisplay(Display.BLOCK);
			}
			
			m_contentPanel.removeStyleName("visual_cell_content");
		}
		
		m_isValidated = true;
	}
	
	public void onCreate(int cellSize)
	{
		 //--- DRK > NOTE: for some reason this gets reset somehow...at least in hosted mode, so can't put it in constructor.
		this.getElement().getStyle().setPosition(Position.ABSOLUTE);
		
		m_isValidated = false;
		m_cellSize = cellSize;
		m_currentImageIndex = -1;
	}
	
	public void onDestroy()
	{
		m_cellSize = -1;
		
		if( m_currentImageIndex != -1 )
		{
			m_backgroundImages.get(m_currentImageIndex).getElement().getStyle().setDisplay(Display.NONE);
		}

		this.insertSafeHtml("");
		
		this.pushDown();
	}
	
	@Override
	public void onFocusGained()
	{
		this.m_glassPanel.setVisible(false);
		this.addStyleName("visual_cell_focused");

		this.allowUserSelect(true);
		
		bhHtmlSandbox sandbox = bhHtmlSandbox.getInstance();
		
		sandbox.allowScrolling(m_contentPanel.getElement(), true);
	}
	
	@Override
	public void onFocusLost()
	{
		this.m_glassPanel.setVisible(true);
		
		this.removeStyleName("visual_cell_focused");
		
		this.allowUserSelect(false);
		
		bhHtmlSandbox sandbox = bhHtmlSandbox.getInstance();
		
		if( sandbox.isRunning() )
		{
			sandbox.stop();
		}
		
		sandbox.allowScrolling(m_contentPanel.getElement(), false);
	}
	
	public void popUp()
	{
		bhE_ZIndex.CELL_POPPED.assignTo(this);
	}
	
	public void pushDown()
	{
		this.getElement().getStyle().clearZIndex();
	}

	@Override
	public void onCellRecycled(int cellSize)
	{
		if( cellSize != m_cellSize )
		{
			m_isValidated = false;
		}

		m_cellSize = cellSize;

		this.insertSafeHtml("");
		
		this.pushDown();
	}
	
	@Override
	public void onError(bhE_CodeType eType)
	{
		//TODO: These are placeholder error messages...should have some prettier error text, or maybe nothing at all?
		
		switch( eType )
		{
			case SPLASH:
			{
				this.setStatusHtml(null, false);
				this.showEmptyContent();
				
				break;
			}
			
			case COMPILED:
			{
				this.setStatusHtml("Problem contacting server.", false);
				
				break;
			}
		}
	}

	@Override
	public void setCode(bhCode code, String idClass)
	{
		this.setStatusHtml(null, false);
		
		bhHtmlSandbox sandbox = bhHtmlSandbox.getInstance();
		
		if( code.getSafetyLevel() == bhE_CodeSafetyLevel.SAFE )
		{
			this.insertSafeHtml(code.getRawCode());
		}
		else if( code.getSafetyLevel() == bhE_CodeSafetyLevel.REQUIRES_STATIC_SANDBOX )
		{
			this.setStatusHtml(null, false);
			
			sandbox.insertStaticHtml(m_contentPanel.getElement(), code.getRawCode(), idClass);
		}
		else if( code.getSafetyLevel() == bhE_CodeSafetyLevel.REQUIRES_DYNAMIC_SANDBOX )
		{
			//--- DRK > The sandbox, using server-cajoled code, can take a second or two to start up,
			//---		so loading used to be shown to let the user know that something is going on.
			//---		Client-side es5mode has been fast enough so far that this text flashes in and out
			//---		and is more annoying than anything...maybe we'll put it back if code exceeds some
			//---		character count or something.
			//this.setStatusText("Initializing...", true);
			
			if( sandbox.isRunning() )
			{
				sandbox.stop();
			}
			
			sandbox.start(m_contentPanel.getElement(), code.getRawCode(), null, idClass, m_codeLoadListener);
		}
	}
	
	private void insertSafeHtml(String html)
	{
		m_contentPanel.getElement().setInnerHTML(html);
	}
	
	bhUIBlocker getBlocker()
	{
		return m_statusPanel;
	}

	@Override
	public void showLoading()
	{
		if( m_spinner.getParent() == null )
		{
			m_spinner.reset();
			
			this.m_statusPanel.setContent(m_spinner);
		}
	}

	@Override
	public void showEmptyContent()
	{
		this.insertSafeHtml("");
	}
	
	private void setStatusHtml(String text, boolean forLoading)
	{
		m_statusPanel.setHtml(text);
	}

	@Override
	public void clearLoading()
	{
		if( m_spinner.getParent() != null )
		{
			this.m_statusPanel.setContent(null);
		}
	}
}
