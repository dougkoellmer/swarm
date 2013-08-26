package swarm.client.entities;

import java.util.logging.Logger;

import swarm.client.managers.smCellCodeManager;
import swarm.shared.app.smS_App;
import swarm.shared.utils.smU_Math;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smA_Cell;
import swarm.shared.entities.smA_Grid;
import swarm.shared.entities.smE_CodeSafetyLevel;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smPoint;


/**
 * ...
 * @author 
 */
public class smBufferCell extends smA_Cell
{
	private static final Logger s_logger = Logger.getLogger(smBufferCell.class.getName());
	
	private static final smPoint s_utilPoint = new smPoint();
	
	private smI_BufferCellListener m_visualization = null;
	
	private final smE_CodeStatus m_codeStatus[] = new smE_CodeStatus[smE_CodeType.values().length];
	
	private boolean m_isFocused = false;
	private boolean m_hasBeenPreviewed = false;
	
	private smCellAddress m_address = null;
	private smA_Grid m_grid = null;
	
	public smBufferCell() 
	{
		this.setStatusAll(smE_CodeStatus.NEEDS_CODE);
	}
	
	public void init(smA_Grid grid)
	{
		m_grid = grid;
	}
	
	public smA_Grid getGrid()
	{
		return m_grid;
	}
	
	public smCellAddress getCellAddress()
	{
		return m_address;
	}
	
	public boolean isFocused()
	{
		return m_isFocused;
	}
	
	public boolean hasBeenPreviewed()
	{
		return m_hasBeenPreviewed;
	}
	
	public void onPreviewSuccess(smCode compiled)
	{
		if( m_isFocused )
		{
			this.setCode(smE_CodeType.COMPILED, compiled);
			
			m_hasBeenPreviewed = true;
		}
		else
		{
			smU_Debug.ASSERT(false, "smBufferCell::Can't preview cell if it isn't focused.");
		}
	}
	
	public void onSyncStart(smCode source, smCode compiled)
	{
		this.setCode(smE_CodeType.SOURCE, source);
		this.setCode(smE_CodeType.SPLASH, null);
		this.setCode(smE_CodeType.COMPILED, compiled);
			
		m_hasBeenPreviewed = false;
	}
	
	public void onSyncResponseSuccess(smCode splash, smCode compiled_nullable)
	{
		this.setCode(smE_CodeType.SPLASH, splash);
		
		this.setCode_private(smE_CodeType.COMPILED, compiled_nullable, false);
	}
	
	public void onSyncResponseError()
	{
		m_codeStatus[smE_CodeType.COMPILED.ordinal()] = smE_CodeStatus.HAS_CODE;
		m_codeStatus[smE_CodeType.SPLASH.ordinal()] = smE_CodeStatus.HAS_CODE;
		
		if( m_visualization != null )
		{
			m_visualization.clearLoading();
		}
	}
	
	public void onAddressFound(smCellAddress address)
	{
		m_address = address;
	}
	
	/**
	 * Should only be called from smCellDataManager.
	 */
	public void onServerRequest(smE_CodeType eType)
	{
		m_codeStatus[eType.ordinal()] = smE_CodeStatus.WAITING_ON_CODE;
		
		if( m_visualization == null )  return;
		
		if( m_isFocused && eType == smE_CodeType.COMPILED )
		{
			m_visualization.showLoading();
		}
		else if( !m_isFocused && eType == smE_CodeType.SPLASH )
		{
			m_visualization.showLoading();
		}
	}
	
	/**
	 * Cell is nuked by cell populator either when snapping to a cell, or refreshing cell.
	 * There could be more cases in the future, but the basic idea is to force a refresh of a cell's contents.
	 */
	public void nuke(smE_CellNuke nukeType)
	{
		switch( nukeType )
		{
			case EVERYTHING:
			{
				for( int i = 0 ; i < m_codeStatus.length; i++ )
				{
					this.setCode(smE_CodeType.values()[i], null);
					m_codeStatus[i] = smE_CodeStatus.NEEDS_CODE;
				}
				
				break;
			}
			
			case ERRORS_ONLY:
			{
				for( int i = 0 ; i < m_codeStatus.length; i++ )
				{
					if( m_codeStatus[i] == smE_CodeStatus.GET_ERROR )
					{
						this.setCode(smE_CodeType.values()[i], null);
						m_codeStatus[i] = smE_CodeStatus.NEEDS_CODE;
					}
				}
				
				break;
			}
		}
	}
	
	public void onFocusGained()
	{
		m_hasBeenPreviewed = false;
		m_isFocused = true;
		
		if( m_visualization != null )
		{
			m_visualization.onFocusGained();
			
			smE_CodeType type = smE_CodeType.COMPILED;
			smE_CodeStatus compiledStatus = getStatus(type);
			
			if( compiledStatus == smE_CodeStatus.HAS_CODE )
			{
				smCode code = this.getCode(type);
				
				if( code.isStandInFor(smE_CodeType.SPLASH) )
				{
					//--- DRK > NOTE: In this case, we're assuming that the splash code was already set on the visualization,
					//---				and that the splash code is identical to the compiled code, so we don't do anything
					//---				for reasons of optimization.  This may be a bad assumption however...time will tell.
					
					smU_Debug.ASSERT(this.getCode(smE_CodeType.SPLASH).isStandInFor(smE_CodeType.COMPILED), "onFocusGained1");
				}
				else
				{
					m_visualization.setCode(code, this.getCellNamespace());
				}
			}
			else if( compiledStatus == smE_CodeStatus.GET_ERROR )
			{
				m_visualization.onError(type);
			}
			else if( compiledStatus == smE_CodeStatus.WAITING_ON_CODE )
			{
				m_visualization.showLoading();
			}
			else
			{
				//--- DRK > This block was hit once when looking at the "terms" cell from afar, signing in, then focusing the cell.
				//---		It was in local release mode (pretty sure about release), and probably after local cache expired.
				//---		At the time, a "showLoading" call was made to the visualization instead of an error, and basically prevented
				//---		the cell from being updated again...I didn't try panning away from the cell and coming back though.
				//---
				//---		Anyway, after that, I changed the below to first check if we have splash code that can stand in for compile.
				//---		At this point, something's still very wrong, because we should also have had compiled code that can stand in for splash.
				//---		But, at least we try to salvage the UX as much as possible.
				//---
				//---	NOTE: This above problem might be because I was an idiot and didn't have a WAITING_ON_CODE case defined above...now there is one.
				
				smE_CodeStatus splashStatus = this.getStatus(smE_CodeType.SPLASH);
				smCode code = this.getCode(type);
				if( splashStatus == smE_CodeStatus.HAS_CODE && code != null && code.isStandInFor(smE_CodeType.COMPILED))
				{
					m_visualization.setCode(code, this.getCellNamespace());
					
					smU_Debug.ASSERT(compiledStatus == smE_CodeStatus.WAITING_ON_CODE, "onFocusGained2 " + compiledStatus);
				}
				else
				{
					m_visualization.onError(type);

					smU_Debug.ASSERT(compiledStatus == smE_CodeStatus.WAITING_ON_CODE, "onFocusGained3 " + compiledStatus);
				}
			}
		}
	}
	
	public void onFocusLost()
	{
		m_isFocused = false;
		
		if( m_visualization != null )
		{
			m_visualization.onFocusLost();
			
			smE_CodeStatus status = getStatus(smE_CodeType.SPLASH);
			if( status == smE_CodeStatus.HAS_CODE )
			{
				m_visualization.clearLoading(); // in case we're loading compiled code.
				
				boolean setCodeVisualization = true;
				
				//--- DRK > The following if block is an optimization that causes static-only code to not 
				//---		get reset on unfocus. Commenting it out for now to err on the side of always resetting a user's
				//---		cell to the original state if you navigate away from it.  This ensures that scroll
				//---		bars, interactive elements, tab order, etc. are all put back in their original place.
				//---		At some point in the future it might be decided that letting cells keep their state
				//---		as much as possible is a good thing, but for now, this code will be left commented out.
				/*if( !m_hasBeenPreviewed )
				{
					if( getStatus(smE_CodeType.COMPILED) == smE_CodeStatus.HAS_CODE )
					{
						if( getCode(smE_CodeType.COMPILED).isStandInFor(smE_CodeType.SPLASH) )
						{
							//--- DRK > NOTE: This is the corollary to the note in onFocusGained.
							//---				It's an optimization, but could be a bad one.
							setCodeVisualization = false;
						}
					}
				}*/
				
				if( setCodeVisualization )
				{
					m_visualization.setCode(this.getCode(smE_CodeType.SPLASH), this.getCellNamespace());
				}
			}
			else if( status == smE_CodeStatus.WAITING_ON_CODE )
			{
				//--- DRK > May already be showing loading...visualization has to account for this
				//---		if showing loading twice in a row resets an animation or something.
				m_visualization.showLoading();
				m_visualization.showEmptyContent();
			}
			else
			{
				m_visualization.clearLoading(); // in case we're loading compiled code.
				m_visualization.showEmptyContent();
			}
		}
		
		m_hasBeenPreviewed = false;
	}
	
	public void onGetResponseError(smE_CodeType eCodeType)
	{
		this.setCode(eCodeType, null);

		m_codeStatus[eCodeType.ordinal()] = smE_CodeStatus.GET_ERROR;
		
		if( m_visualization != null )
		{
			if( m_isFocused )
			{
				if( eCodeType == smE_CodeType.COMPILED )
				{
					m_visualization.onError(eCodeType);
				}
			}
			else
			{
				if( eCodeType == smE_CodeType.SPLASH )
				{
					m_visualization.onError(eCodeType);
				}
			}
		}
	}
	
	private void setStatusAll(smE_CodeStatus eStatus)
	{
		for( int i = 0; i < m_codeStatus.length; i++ )
		{
			m_codeStatus[i] = eStatus;
		}
	}
	
	public smE_CodeStatus getStatus(smE_CodeType eType)
	{
		return m_codeStatus[eType.ordinal()];
	}
	
	private String getCellNamespace()
	{
		return this.getCoordinate().writeString();
	}
	
	@Override
	public void setCode(smE_CodeType eType, smCode code)
	{
		this.setCode_private(eType, code, true);
	}
	
	private void setCode_private(smE_CodeType eType, smCode code, boolean updateVisualization)
	{
		super.setCode(eType, code);
		
		if( code == null )  return;
		
		this.m_codeStatus[eType.ordinal()] = smE_CodeStatus.HAS_CODE;
		
		if( m_visualization != null )
		{
			if( m_isFocused )
			{
				//--- DRK > If focused, only COMPILED can be set on the visualization.
				if( eType == smE_CodeType.COMPILED )
				{
					if( updateVisualization )
					{
						m_visualization.setCode(code, this.getCellNamespace());
					}
				}
			}
			else
			{
				//--- DRK > If not focused, only SPLASH_SCREEN can be set on the visualization.
				if( eType == smE_CodeType.SPLASH )
				{
					if( updateVisualization )
					{
						m_visualization.setCode(code, this.getCellNamespace());
					}
				}
			}
		}
	}
	
	public void setVisualization(smI_BufferCellListener visualization)
	{
		m_visualization = visualization;
	}
	
	public smI_BufferCellListener getVisualization()
	{
		return m_visualization;
	}
	
	public void onCellRecycled(int cellSize)
	{
		this.clear_private();
		
		if( this.m_visualization != null )
		{
			m_visualization.onCellRecycled(cellSize);
		}
	}
	
	public boolean isTouchingPoint(smPoint point)
	{
		double cellWidthPlusPadding = m_grid.getCellWidth() + m_grid.getCellPadding();
		double cellHeightPlusPadding = m_grid.getCellHeight() + m_grid.getCellPadding();
		
		if ( point.getZ() != 0 )  return false;
		
		this.getCoordinate().calcPoint(s_utilPoint, m_grid.getCellWidth(), m_grid.getCellHeight(), m_grid.getCellPadding(), 1);
		
		if
		(
			smU_Math.isWithin(point.getX(), s_utilPoint.getX(), s_utilPoint.getX() + cellWidthPlusPadding) &&
			smU_Math.isWithin(point.getY(), s_utilPoint.getY(), s_utilPoint.getY() + cellHeightPlusPadding)
		)
		{
			return true;
		}
		
		return false;
	}
	
	public void onServerRequestCancelled(smE_CodeType eType)
	{
		if( eType == smE_CodeType.COMPILED )
		{
			m_visualization.clearLoading();
		}
		
		if( this.getCode(eType) != null )
		{
			this.m_codeStatus[eType.ordinal()] = smE_CodeStatus.HAS_CODE;
		}
		else
		{
			this.m_codeStatus[eType.ordinal()] = smE_CodeStatus.NEEDS_CODE;
		}
	}
	
	private void clear_private()
	{
		super.clearCode();
		
		this.setStatusAll(smE_CodeStatus.NEEDS_CODE);
		
		smU_Debug.ASSERT(!m_isFocused);
		
		m_isFocused = false;
		m_address = null;
		m_hasBeenPreviewed = false;
	}
	
	public void clear()
	{
		this.clear_private();
		
		this.m_visualization = null;
		this.m_grid = null;
	}
}