package swarm.client.view.cell;

import java.util.ArrayList;
import java.util.logging.Logger;

import swarm.client.app.smAppContext;
import swarm.client.entities.smI_BufferCellListener;
import swarm.client.view.smE_ZIndex;
import swarm.client.view.smS_UI;
import swarm.client.view.smU_UI;
import swarm.client.view.sandbox.smSandboxManager;
import swarm.client.view.tabs.code.smI_CodeLoadListener;
import swarm.client.view.widget.smUIBlocker;
import swarm.shared.app.smS_App;
import swarm.shared.utils.smU_Bits;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smE_CodeSafetyLevel;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smMutableCode;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;

public class smVisualCell extends AbsolutePanel implements smI_BufferCellListener
{
	private static final Logger s_logger = Logger.getLogger(smVisualCell.class.getName());
	
	//private static final String SPINNER_HTML = "<img src='/r.img/spinner.gif?v=1' />";
	
	private static int s_currentId = 0;
	
	private static final class CodeLoadListener implements smI_CodeLoadListener
	{
		private final smVisualCell m_this;
		
		CodeLoadListener(smVisualCell thisArg)
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
	//private final AbsolutePanel //m_backgroundPanel = new AbsolutePanel();
	private final smUIBlocker	m_statusPanel = new smUIBlocker();
	private final AbsolutePanel m_glassPanel = new AbsolutePanel();
	private final smCellSpinner m_spinner	= new smCellSpinner(smS_UI.SPINNER_ROTATION_RATE);
	private final smMutableCode m_utilCode = new smMutableCode(smE_CodeType.values());
	
	//private final ArrayList<Image> //m_backgroundImages = new ArrayList<Image>();
	private int m_currentImageIndex = -1;
	
	//private static final String CELL_PLUS_SPACING_PIXEL_COUNT = smS_App.CELL_PLUS_SPACING_PIXEL_COUNT + "px";
	//private static final String CELL_PIXEL_COUNT = smS_App.CELL_PIXEL_COUNT + "px";
	
	private int m_subCellDimension = -1;
	private int m_width = 0;
	private int m_height = 0;
	private int m_padding = 0;
	
	private boolean m_isValidated = false;
	
	private final CodeLoadListener m_codeLoadListener = new CodeLoadListener(this);
	
	private final smSandboxManager m_sandbox;
	
	public smVisualCell(smSandboxManager sandbox)
	{
		m_sandbox = sandbox;
		m_id = s_currentId; s_currentId++;
		
		this.addStyleName("visual_cell");
		m_glassPanel.addStyleName("sm_cell_glass");

		//m_backgroundPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
		m_statusPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
		m_glassPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
		
		smE_ZIndex.CELL_STATUS.assignTo(m_statusPanel);
		smE_ZIndex.CELL_GLASS.assignTo(m_glassPanel);
		smE_ZIndex.CELL_CONTENT.assignTo(m_contentPanel);
		
		m_statusPanel.setVisible(false);
		
		/*int maxImages = smS_App.MAX_CELL_IMAGES;
		int currentBit = 1;
		for( int i = 0; i <= 1; i++ )
		{
			Image image = new Image();
			image.setUrl("/r.img/cell_size_" + currentBit + ".png");
			image.getElement().getStyle().setDisplay(Display.NONE);
			//m_backgroundImages.add(image);
			//m_backgroundPanel.add(image);
			
			currentBit <<= 1;
		}*/
		
		//smU_UI.toggleSelectability(//m_backgroundPanel.getElement(), false);
		this.allowUserSelect(false);
		
		//this.add(//m_backgroundPanel);
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
	
	void validate()
	{
		if( m_isValidated )  return;
		
		if( m_currentImageIndex != -1 )
		{
			//m_backgroundImages.get(m_currentImageIndex).getElement().getStyle().setDisplay(Display.NONE);
		}
		
		if( m_subCellDimension == 1 )
		{
			this.setSize(m_width+m_padding + "px", m_height+m_padding + "px");
			//m_backgroundPanel.setSize(m_width+m_padding + "px", m_height+m_padding + "px");
			
			m_currentImageIndex = 0;
			//m_backgroundImages.get(m_currentImageIndex).getElement().getStyle().setDisplay(Display.BLOCK);
			
			//--- DRK > Rare case of jumping from beyond max imaged zoom to all the way to cell size 1,
			//---		but could technically happen with bad frame rate or something, so clearing this here just in case.
			//m_backgroundPanel.getElement().getStyle().clearBackgroundColor();
			
			m_contentPanel.addStyleName("visual_cell_content");
		}
		else if( m_subCellDimension > 1 )
		{
			this.setStatusHtml(null, false); // shouldn't have to be done, but what the hell.
			
			this.setSize(m_width+"px", m_height+"px");
			//m_backgroundPanel.setSize(m_width+"px", m_height+"px");
			
			if( m_subCellDimension > smS_App.MAX_IMAGED_CELL_SIZE )
			{				
				//m_backgroundPanel.getElement().getStyle().setBackgroundColor("white");
			}
			else
			{
				//m_backgroundPanel.getElement().getStyle().clearBackgroundColor();
				
				m_currentImageIndex = smU_Bits.calcBitPosition(m_subCellDimension);
				//m_backgroundImages.get(m_currentImageIndex).getElement().getStyle().setDisplay(Display.BLOCK);
			}
			
			m_contentPanel.removeStyleName("visual_cell_content");
		}
		
		m_isValidated = true;
	}
	
	public void onCreate(int width, int height, int padding, int subCellDimension)
	{
		 //--- DRK > NOTE: for some reason this gets reset somehow...at least in hosted mode, so can't put it in constructor.
		this.getElement().getStyle().setPosition(Position.ABSOLUTE);
		
		m_isValidated = false;
		m_subCellDimension = subCellDimension;
		m_width = width;
		m_height = height;
		m_padding = padding;
		m_currentImageIndex = -1;
		
		m_contentPanel.setSize(m_width+"px", m_height+"px");
		m_statusPanel.setSize(m_width+"px", m_height+"px");
	}
	
	public void onDestroy()
	{
		m_subCellDimension = -1;
		
		if( m_currentImageIndex != -1 )
		{
			//m_backgroundImages.get(m_currentImageIndex).getElement().getStyle().setDisplay(Display.NONE);
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
		
		m_sandbox.allowScrolling(m_contentPanel.getElement(), true);
	}
	
	@Override
	public void onFocusLost()
	{
		this.m_glassPanel.setVisible(true);
		
		this.removeStyleName("visual_cell_focused");
		
		this.allowUserSelect(false);
		
		if( m_sandbox.isRunning() )
		{
			m_sandbox.stop(m_contentPanel.getElement());
		}
		
		m_sandbox.allowScrolling(m_contentPanel.getElement(), false);
	}
	
	public void popUp()
	{
		smE_ZIndex.CELL_POPPED.assignTo(this);
	}
	
	public void pushDown()
	{
		this.getElement().getStyle().clearZIndex();
	}

	@Override
	public void onCellRecycled(int cellSize)
	{
		if( cellSize != m_subCellDimension )
		{
			m_isValidated = false;
		}

		m_subCellDimension = cellSize;

		this.insertSafeHtml("");
		
		this.pushDown();
	}
	
	@Override
	public void onError(smE_CodeType eType)
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
	public void setCode(smCode code, String cellNamespace)
	{
		this.setStatusHtml(null, false);
		
		if( m_sandbox.isRunning() )
		{
			m_sandbox.stop(m_contentPanel.getElement());
		}
		
		m_sandbox.start(m_contentPanel.getElement(), code, cellNamespace, m_codeLoadListener);
	}
	
	private void insertSafeHtml(String html)
	{
		m_utilCode.setRawCode(html);
		m_utilCode.setSafetyLevel(smE_CodeSafetyLevel.NO_SANDBOX);

		m_sandbox.start(m_contentPanel.getElement(), m_utilCode, null, m_codeLoadListener);
	}
	
	smUIBlocker getBlocker()
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
