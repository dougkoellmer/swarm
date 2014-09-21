package swarm.client.view.cell;

import java.util.ArrayList;
import java.util.logging.Logger;

import swarm.client.app.A_ClientApp;
import swarm.client.app.AppContext;
import swarm.client.entities.BufferCell;
import swarm.client.entities.I_BufferCellListener;
import swarm.client.managers.CameraManager;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.view.E_ZIndex;
import swarm.client.view.S_UI;
import swarm.client.view.U_Css;
import swarm.client.view.U_View;
import swarm.client.view.ViewContext;
import swarm.client.view.sandbox.SandboxManager;
import swarm.client.view.tabs.code.I_CodeLoadListener;
import swarm.client.view.widget.UIBlocker;
import swarm.shared.app.S_CommonApp;
import swarm.shared.utils.U_Bits;
import swarm.shared.utils.U_Math;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.A_Grid;
import swarm.shared.entities.E_CodeSafetyLevel;
import swarm.shared.entities.E_CodeType;
import swarm.shared.structs.Code;
import swarm.shared.structs.MutableCode;
import swarm.shared.structs.Point;
import swarm.shared.structs.Rect;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class VisualCell extends AbsolutePanel implements I_BufferCellListener
{
	public static interface I_CodeListener
	{
		void onCodeLoaded(VisualCell cell);
	}
	static enum LayoutState
	{
		NOT_CHANGING,
		CHANGING_FROM_TIME,
		CHANGING_FROM_SNAP
	};
	
	private static final Logger s_logger = Logger.getLogger(VisualCell.class.getName());
	
	//private static final String SPINNER_HTML = "<img src='/r.img/spinner.gif?v=1' />";
	
	private static int s_currentId = 0;
	
	private static final class CodeLoadListener implements I_CodeLoadListener
	{
		private final VisualCell m_this;
		
		CodeLoadListener(VisualCell thisArg)
		{
			m_this = thisArg;
		}
		
		@Override
		public void onCodeLoad()
		{
			m_this.setStatusHtml(null, false);
		}
	}
	
	private I_CodeListener m_codeListener;
	private int m_id;
	private final AbsolutePanel m_contentPanel = new AbsolutePanel();
	private final UIBlocker	m_statusPanel = new UIBlocker();
	private final AbsolutePanel m_glassPanel = new AbsolutePanel();
	private final I_CellSpinner m_spinner;
	private final MutableCode m_utilCode = new MutableCode(E_CodeType.values());
	private BufferCell m_bufferCell = null;
	
	private int m_subCellDimension = -1;
	private int m_width = 0;
	private int m_height = 0;
	private int m_padding = 0;
	
	private int m_defaultWidth = 0;
	private int m_defaultHeight = 0;
	private int m_baseWidth = 0;
	private int m_baseHeight = 0;
	private int m_targetWidth = 0;
	private int m_targetHeight = 0;
	
	private int m_targetXOffset = 0;
	private int m_targetYOffset = 0;
	private int m_xOffset = 0;
	private int m_yOffset = 0;
	private int m_baseXOffset = 0;
	private int m_baseYOffset = 0;
	
	private int m_startingXOffset = 0;
	private int m_startingYOffset = 0;
	
	private double m_baseChangeValue = 0;
	
	private boolean m_isValidated = false;
	
	private final CodeLoadListener m_codeLoadListener = new CodeLoadListener(this);
	
	private final SandboxManager m_sandboxMngr;
	private final CameraManager m_cameraMngr;
	
	private E_CodeSafetyLevel m_codeSafetyLevel;
	
	private boolean m_isSnapping = false;
	private boolean m_isFocused = false;
	private LayoutState m_layoutState = LayoutState.NOT_CHANGING;
	private final double m_sizeChangeTime;
	private final double m_retractionEasing;
	
	public VisualCell(I_CellSpinner spinner, SandboxManager sandboxMngr, CameraManager cameraMngr, double retractionEasing, double sizeChangeTime)
	{
		m_retractionEasing = retractionEasing;
		m_spinner = spinner;
		m_cameraMngr = cameraMngr;
		m_sandboxMngr = sandboxMngr;
		m_sizeChangeTime = sizeChangeTime;
		m_id = s_currentId;
		s_currentId++;
		
		this.addStyleName("visual_cell");
		m_glassPanel.addStyleName("sm_cell_glass");

		//m_backgroundPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
		m_statusPanel.getElement().getStyle().setPosition(Position.RELATIVE);
		m_statusPanel.getElement().getStyle().setTop(-100, Unit.PCT);
		m_glassPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
		
		E_ZIndex.CELL_STATUS.assignTo(m_statusPanel);
		E_ZIndex.CELL_GLASS.assignTo(m_glassPanel);
		E_ZIndex.CELL_CONTENT.assignTo(m_contentPanel);
		
		m_statusPanel.setVisible(false);
		
		m_contentPanel.addStyleName("visual_cell_content");
		
		this.getElement().getStyle().setOpacity(.5);
		
		U_Css.allowUserSelect(m_contentPanel.getElement(), false);
		
		this.add(m_contentPanel);
		this.add(m_statusPanel);
		this.add(m_glassPanel);
	}
	
	public void setCodeListener(I_CodeListener listener)
	{
		m_codeListener = listener;
	}
	
	public BufferCell getBufferCell()
	{
		return m_bufferCell;
	}
	
	LayoutState getSizeChangeState()
	{
		return m_layoutState;
	}
	
	int getId()
	{
		return m_id;
	}
	
	public int getWidth()
	{
		return m_width;
	}
	
	int getHeight()
	{
		return m_height;
	}
	
	public void update(double timeStep)
	{
		if( m_spinner.asWidget().getParent() != null )
		{
			m_spinner.update(timeStep);
		}

		if( this.m_layoutState == LayoutState.CHANGING_FROM_SNAP )
		{
			double snapProgress = m_cameraMngr.getWeightedSnapProgress();
			//s_logger.severe("cell: " + " " + m_baseChangeValue + " " + snapProgress + " ");
			double mantissa = m_baseChangeValue == 1 ? 1 : (snapProgress - m_baseChangeValue) / (1-m_baseChangeValue);
			mantissa = U_Math.clampMantissa(mantissa);
				
			this.updateLayout(mantissa, mantissa);
		}
		else if( this.m_layoutState == LayoutState.CHANGING_FROM_TIME )
		{
			m_baseChangeValue += timeStep;
			double mantissa = m_baseChangeValue / m_sizeChangeTime;
			mantissa = U_Math.clampMantissa(mantissa);
			
			double easingMultiplier = 2.25;
			
			double retractionEasingX = m_retractionEasing - 1;
			retractionEasingX += m_baseWidth / m_defaultWidth;
			retractionEasingX *= easingMultiplier;
			double mantissaX = U_View.easeMantissa(mantissa, retractionEasingX);

			double retractionEasingY = m_retractionEasing - 1;
			retractionEasingY += m_baseHeight / m_defaultHeight;
			retractionEasingY *= easingMultiplier;
			double mantissaY = U_View.easeMantissa(mantissa, retractionEasingY);
			
//			s_logger.severe("START");
//			s_logger.severe(m_retractionEasing + " " + retractionEasingX + " " + retractionEasingY);
//			s_logger.severe(mantissa + " " + mantissaX + " " + mantissaY);
//			s_logger.severe("END");
			
			this.updateLayout(mantissaX, mantissaY);
		}
	}
	
	private void updateLayout(double progressMantissaX, double progressMantissaY)
	{
		double widthDelta = (m_targetWidth - m_baseWidth) * progressMantissaX;
		m_width = (int) (m_baseWidth + widthDelta);
		
		double heightDelta = (m_targetHeight - m_baseHeight) * progressMantissaY;
		m_height = (int) (m_baseHeight + heightDelta);
		
		double xOffsetDelta = (m_targetXOffset - m_baseXOffset) * progressMantissaX;
		m_xOffset = (int) (m_baseXOffset + xOffsetDelta);
		
		double yOffsetDelta = (m_targetYOffset - m_baseYOffset) * progressMantissaY;
		m_yOffset = (int) (m_baseYOffset + yOffsetDelta);
		
		//s_logger.severe(m_xOffset + " " + m_targetXOffset + " " + m_baseXOffset);
		
		if( progressMantissaX >= 1 )
		{
			this.ensureTargetLayout();
		}
		else
		{
			this.flushLayout();
		}
	}
	
	void validate()
	{
		if( m_isValidated )  return;
		
		if( m_subCellDimension == 1 )
		{
			this.flushLayout();
			this.getElement().getStyle().setPaddingRight(m_padding, Unit.PX);
			this.getElement().getStyle().setPaddingBottom(m_padding, Unit.PX);
			//m_backgroundPanel.setSize(m_width+m_padding + "px", m_height+m_padding + "px");
			
			//--- DRK > Rare case of jumping from beyond max imaged zoom to all the way to cell size 1,
			//---		but could technically happen with bad frame rate or something, so clearing this here just in case.
			//m_backgroundPanel.getElement().getStyle().clearBackgroundColor();
			
			m_contentPanel.addStyleName("visual_cell_content");
		}
		else if( m_subCellDimension > 1 )
		{
//			U_Debug.ASSERT(false, "not implemented");
			this.setStatusHtml(null, false); // shouldn't have to be done, but what the hell.
			
			this.setSize(m_width+"px", m_height+"px");
			this.getElement().getStyle().clearPaddingRight();
			this.getElement().getStyle().clearPaddingBottom();
			//m_backgroundPanel.setSize(m_width+"px", m_height+"px");
			
			if( m_subCellDimension > S_CommonApp.MAX_IMAGED_CELL_SIZE )
			{				
				//m_backgroundPanel.getElement().getStyle().setBackgroundColor("white");
			}
			else
			{
				//m_backgroundPanel.getElement().getStyle().clearBackgroundColor();
			}
			
			m_contentPanel.removeStyleName("visual_cell_content");
		}
		
		m_isValidated = true;
	}
	
	void crop(int thisX, int thisY, int windowWidth, int windowHeight)
	{
		int totalWidth = m_width+m_padding;
		int totalHeight = m_height+m_padding;
		thisX -= m_xOffset;
		thisY -= m_yOffset;
		
		int overflow = (thisX + totalWidth) - windowWidth;
		
		if( overflow > 0 )
		{
			totalWidth -= overflow;
		}
		
		overflow = (thisY + totalHeight) - windowHeight;
		
		if( overflow > 0)
		{
			totalHeight -= overflow;
		}
		
		m_contentPanel.setSize(m_width + "px", m_height + "px");
		this.setSize(totalWidth + "px", totalHeight + "px");
	}
	
	void removeCrop()
	{
		this.flushLayout();
		m_contentPanel.getElement().getStyle().clearWidth();
		m_contentPanel.getElement().getStyle().clearHeight();
	}
	
	private void flushLayout()
	{
		this.setSize(m_width+m_padding + "px", m_height+m_padding + "px");
	}
	
	public void onCreate(BufferCell bufferCell, int width, int height, int padding, int subCellDimension)
	{
		m_bufferCell = bufferCell;
		
		onCreatedOrRecycled(width, height, padding, subCellDimension);
	}
	
	private void onCreatedOrRecycled(int width, int height, int padding, int subCellDimension)
	{		
		m_layoutState = LayoutState.NOT_CHANGING;
		m_isFocused = false;
		m_isValidated = false;
		m_subCellDimension = subCellDimension;
		
		m_targetWidth = m_defaultWidth = m_baseWidth = m_width = width;
		m_targetHeight = m_defaultHeight = m_baseHeight = m_height = height;
		m_targetXOffset = m_baseXOffset = m_xOffset = 0;
		m_targetYOffset = m_baseYOffset = m_yOffset = 0;
		m_padding = padding;

		this.setScrollMode(E_ScrollMode.NOT_SCROLLING);
		this.removeCrop();
	}
	
	public int calcNaturalHeight()
	{
		int naturalHeight = m_contentPanel.getElement().getScrollHeight();
		naturalHeight = naturalHeight < m_defaultHeight ? m_defaultHeight : naturalHeight;
		
		return naturalHeight;
	}
	
	public int calcNaturalWidth()
	{
		int naturalWidth = m_contentPanel.getElement().getScrollWidth();
		naturalWidth = naturalWidth < m_defaultWidth ? m_defaultWidth : naturalWidth;
		
		return naturalWidth;
	}
	
	public void setTargetLayout(int width, int height, int xOffset, int yOffset)
	{
		m_baseXOffset = m_xOffset;
		m_baseYOffset = m_yOffset;
		m_baseWidth = this.m_width;
		m_baseHeight = this.m_height;

		m_targetWidth = width;
		m_targetHeight = height;
		
		m_targetXOffset = xOffset;
		m_targetYOffset = yOffset;
		
		if( this.m_isSnapping )
		{
			m_baseChangeValue = m_cameraMngr.getWeightedSnapProgress();
			m_layoutState = LayoutState.CHANGING_FROM_SNAP;
		}
		else if( this.m_isFocused )
		{
			this.ensureTargetLayout();
			
			m_startingXOffset = m_xOffset;
			m_startingYOffset = m_yOffset;
		}
	}
	
	private void ensureTargetLayout()
	{
		//--- If we get the focused cell size while viewing cell,
		//--- we don't make user wait, and just instantly expand it.
		//--- Maybe a little jarring, but should be fringe case.
		m_baseWidth = m_width = m_targetWidth;
		m_baseHeight = m_height = m_targetHeight;
		m_baseXOffset = m_xOffset = m_targetXOffset;
		m_baseYOffset = m_yOffset = m_targetYOffset;
		m_layoutState = LayoutState.NOT_CHANGING;
		
		this.flushLayout();
	}
	
	public void onDestroy()
	{
		m_bufferCell = null;
		m_isFocused = false;
		m_subCellDimension = -1;

		if( !m_codeSafetyLevel.isStatic() )
		{
			this.insertSafeHtml("");
		}
		
		m_codeSafetyLevel = null;
		
		this.pushDown();
	}
	
	void setScrollMode(E_ScrollMode mode)
	{
		if( mode == E_ScrollMode.SCROLLING_FOCUSED || mode == E_ScrollMode.NOT_SCROLLING )
		{
			this.getElement().getStyle().setPosition(Position.ABSOLUTE);
		}
		else
		{
			this.getElement().getStyle().setPosition(Position.FIXED);
		}
	}
	
	@Override
	public void onFocusGained()
	{
		m_isSnapping = false;
		m_isFocused = true;
		
		E_ZIndex.CELL_FOCUSED.assignTo(this);
		this.ensureTargetLayout();
		
		m_startingXOffset = m_xOffset;
		m_startingYOffset = m_yOffset;
		
		this.m_glassPanel.setVisible(false);
		this.addStyleName("visual_cell_focused");

		U_Css.allowUserSelect(m_contentPanel.getElement(), true);
		
		m_sandboxMngr.allowScrolling(m_contentPanel.getElement(), true);
	}
	
	@Override
	public void onFocusLost()
	{
		m_isSnapping = false; // just in case.
		m_isFocused = false;
		
		this.m_glassPanel.setVisible(true);
		this.removeStyleName("visual_cell_focused");
		U_Css.allowUserSelect(m_contentPanel.getElement(), false);
		m_sandboxMngr.allowScrolling(m_contentPanel.getElement(), false);
		E_ZIndex.CELL_POPPED.assignTo(this);
		
		/*if( m_sandboxMngr.isRunning() )
		{
			m_sandboxMngr.stop(m_contentPanel.getElement());
		}*/
		
		this.setToTargetSizeDefault();
	}
	
	public int getXOffset()
	{
		return m_xOffset;
	}
	
	public int getYOffset()
	{
		return m_yOffset;
	}
	
	public int getStartingXOffset()
	{
		return m_startingXOffset;
	}
	
	public int getStartingYOffset()
	{
		return m_startingYOffset;
	}
	
	public void calcTopLeft(Point point_out)
	{
		A_Grid grid = m_bufferCell.getGrid();
		m_bufferCell.getCoordinate().calcPoint(point_out, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
		point_out.inc(m_xOffset, m_yOffset, 0);
	}
	
	public void calcTargetTopLeft(Point point_out)
	{
		A_Grid grid = m_bufferCell.getGrid();
		m_bufferCell.getCoordinate().calcPoint(point_out, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
		point_out.inc(m_targetXOffset, m_targetYOffset, 0);
	}
	
	public int getTargetWidth()
	{
		return m_targetWidth;
	}
	
	public int getTargetHeight()
	{
		return m_targetHeight;
	}
	
	private void setToTargetSizeDefault()
	{
		m_layoutState = LayoutState.CHANGING_FROM_TIME;
		m_baseChangeValue = 0;
		this.setTargetLayout(m_defaultWidth, m_defaultHeight, 0, 0);
	}
	
	public void popUp()
	{
		//--- DRK > Added this conditional because for fringe case of instant snap,
		//--- 		onFocusGained can be called before popUp. I thought this case wasn't
		//---		a problem before, but may not have tested it, or something might have changed.
		//---		It does make sense that it's needed though, because onFocusGained call originates
		//---		in state machine, and popUp is invoked from a UI handler of the state event,
		//---		so comes later if the snap is instant.
		if( !m_isFocused )
		{
			E_ZIndex.CELL_POPPED.assignTo(this);
			m_isSnapping = true;
		}
	}
	
	public void cancelPopUp()
	{
		boolean wasSnapping = m_isSnapping;
		m_isSnapping = false;
		
		if( m_layoutState == LayoutState.CHANGING_FROM_SNAP )
		{
			this.setToTargetSizeDefault();
			
			U_Debug.ASSERT(wasSnapping, "expected cell to know it was snapping");
		}
	}
	
	public void pushDown()
	{
		this.getElement().getStyle().clearZIndex();
	}

	@Override
	public void onCellRecycled(int width, int height, int padding, int subCellDimension)
	{
		if( subCellDimension != m_subCellDimension )
		{
			m_isValidated = false;
		}
		
		this.onCreatedOrRecycled(width, height, padding, subCellDimension);

		this.insertSafeHtml("");
		
		this.pushDown();
	}
	
	@Override
	public void onError(E_CodeType eType)
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
	public void setCode(Code code, String cellNamespace)
	{
		this.setStatusHtml(null, false);
		
		/*if( m_sandboxMngr.isRunning() )
		{
			m_sandboxMngr.stop(m_contentPanel.getElement());
		}*/
		
		m_codeSafetyLevel = code.getSafetyLevel();
		
		m_sandboxMngr.start(m_contentPanel.getElement(), code, cellNamespace, m_codeLoadListener);
		
		if( m_codeListener != null )  m_codeListener.onCodeLoaded(this);
	}
	
	public E_CodeSafetyLevel getCodeSafetyLevel()
	{
		return m_codeSafetyLevel;
	}
	
	private void insertSafeHtml(String html)
	{
		m_utilCode.setRawCode(html);
		m_utilCode.setSafetyLevel(E_CodeSafetyLevel.NO_SANDBOX_STATIC);
		
		this.setCode(m_utilCode, "");
	}
	
	UIBlocker getBlocker()
	{
		return m_statusPanel;
	}

	@Override
	public void showLoading()
	{
		if( m_spinner.asWidget().getParent() == null )
		{
			m_spinner.reset();
			
			this.m_statusPanel.setContent(m_spinner.asWidget());
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
		if( m_spinner.asWidget().getParent() != null )
		{
			this.m_statusPanel.setContent(null);
		}
	}
}