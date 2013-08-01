package com.b33hive.client.entities;

import java.util.logging.Logger;

import com.b33hive.client.managers.bhCellCodeManager;
import com.b33hive.shared.app.bhS_App;
import com.b33hive.shared.bhU_Math;
import com.b33hive.shared.debugging.bhU_Debug;
import com.b33hive.shared.entities.bhA_Cell;
import com.b33hive.shared.entities.bhE_CodeSafetyLevel;
import com.b33hive.shared.entities.bhE_CodeType;
import com.b33hive.shared.structs.bhCellAddress;
import com.b33hive.shared.structs.bhCode;
import com.b33hive.shared.structs.bhPoint;


/**
 * ...
 * @author 
 */
public class bhBufferCell extends bhA_Cell
{
	private static final Logger s_logger = Logger.getLogger(bhBufferCell.class.getName());
	
	private static final bhPoint s_utilPoint = new bhPoint();
	
	private bhI_BufferCellListener m_visualization = null;
	
	private final bhE_CodeStatus m_codeStatus[] = new bhE_CodeStatus[bhE_CodeType.values().length];
	
	private boolean m_isFocused = false;
	private boolean m_hasBeenPreviewed = false;
	
	private bhCellAddress m_address = null;
	
	public bhBufferCell() 
	{
		this.setStatusAll(bhE_CodeStatus.NEEDS_CODE);
	}
	
	public bhCellAddress getCellAddress()
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
	
	public void onPreviewSuccess(bhCode compiled)
	{
		if( m_isFocused )
		{
			this.setCode(bhE_CodeType.COMPILED, compiled);
			
			m_hasBeenPreviewed = true;
		}
		else
		{
			bhU_Debug.ASSERT(false, "bhBufferCell::Can't preview cell if it isn't focused.");
		}
	}
	
	public void onSyncStart(bhCode source, bhCode compiled)
	{
		this.setCode(bhE_CodeType.SOURCE, source);
		this.setCode(bhE_CodeType.SPLASH, null);
		this.setCode(bhE_CodeType.COMPILED, compiled);
			
		m_hasBeenPreviewed = false;
	}
	
	public void onSyncResponseSuccess(bhCode splash, bhCode compiled_nullable)
	{
		this.setCode(bhE_CodeType.SPLASH, splash);
		
		this.setCode_private(bhE_CodeType.COMPILED, compiled_nullable, false);
	}
	
	public void onSyncResponseError()
	{
		m_codeStatus[bhE_CodeType.COMPILED.ordinal()] = bhE_CodeStatus.HAS_CODE;
		m_codeStatus[bhE_CodeType.SPLASH.ordinal()] = bhE_CodeStatus.HAS_CODE;
		
		if( m_visualization != null )
		{
			m_visualization.clearLoading();
		}
	}
	
	public void onAddressFound(bhCellAddress address)
	{
		m_address = address;
	}
	
	/**
	 * Should only be called from bhCellDataManager.
	 */
	public void onServerRequest(bhE_CodeType eType)
	{
		m_codeStatus[eType.ordinal()] = bhE_CodeStatus.WAITING_ON_CODE;
		
		if( m_visualization == null )  return;
		
		if( m_isFocused && eType == bhE_CodeType.COMPILED )
		{
			m_visualization.showLoading();
		}
		else if( !m_isFocused && eType == bhE_CodeType.SPLASH )
		{
			m_visualization.showLoading();
		}
	}
	
	/**
	 * Cell is nuked by cell populator either when snapping to a cell, or refreshing cell.
	 * There could be more cases in the future, but the basic idea is to force a refresh of a cell's contents.
	 */
	public void nuke(bhE_CellNuke nukeType)
	{
		switch( nukeType )
		{
			case EVERYTHING:
			{
				for( int i = 0 ; i < m_codeStatus.length; i++ )
				{
					this.setCode(bhE_CodeType.values()[i], null);
					m_codeStatus[i] = bhE_CodeStatus.NEEDS_CODE;
				}
				
				break;
			}
			
			case ERRORS_ONLY:
			{
				for( int i = 0 ; i < m_codeStatus.length; i++ )
				{
					if( m_codeStatus[i] == bhE_CodeStatus.GET_ERROR )
					{
						this.setCode(bhE_CodeType.values()[i], null);
						m_codeStatus[i] = bhE_CodeStatus.NEEDS_CODE;
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
			
			bhE_CodeType type = bhE_CodeType.COMPILED;
			bhE_CodeStatus compiledStatus = getStatus(type);
			
			if( compiledStatus == bhE_CodeStatus.HAS_CODE )
			{
				bhCode code = this.getCode(type);
				
				if( code.isStandInFor(bhE_CodeType.SPLASH) )
				{
					//--- DRK > NOTE: In this case, we're assuming that the splash code was already set on the visualization,
					//---				and that the splash code is identical to the compiled code, so we don't do anything
					//---				for reasons of optimization.  This may be a bad assumption however...time will tell.
					
					bhU_Debug.ASSERT(this.getCode(bhE_CodeType.SPLASH).isStandInFor(bhE_CodeType.COMPILED), "onFocusGained1");
				}
				else
				{
					m_visualization.setCode(code, this.getIdClass());
				}
			}
			else if( compiledStatus == bhE_CodeStatus.GET_ERROR )
			{
				m_visualization.onError(type);
			}
			else if( compiledStatus == bhE_CodeStatus.WAITING_ON_CODE )
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
				//---	NOTE: This above problem might be because I was an idiot and didn't have a WAITING_ON_CODE case defined...now there is one.
				
				bhE_CodeStatus splashStatus = this.getStatus(bhE_CodeType.SPLASH);
				bhCode code = this.getCode(type);
				if( splashStatus == bhE_CodeStatus.HAS_CODE && code != null && code.isStandInFor(bhE_CodeType.COMPILED))
				{
					m_visualization.setCode(code, this.getIdClass());
					
					bhU_Debug.ASSERT(compiledStatus == bhE_CodeStatus.WAITING_ON_CODE, "onFocusGained2 " + compiledStatus);
				}
				else
				{
					m_visualization.onError(type);

					bhU_Debug.ASSERT(compiledStatus == bhE_CodeStatus.WAITING_ON_CODE, "onFocusGained3 " + compiledStatus);
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
			
			bhE_CodeStatus status = getStatus(bhE_CodeType.SPLASH);
			if( status == bhE_CodeStatus.HAS_CODE )
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
					if( getStatus(bhE_CodeType.COMPILED) == bhE_CodeStatus.HAS_CODE )
					{
						if( getCode(bhE_CodeType.COMPILED).isStandInFor(bhE_CodeType.SPLASH) )
						{
							//--- DRK > NOTE: This is the corollary to the note in onFocusGained.
							//---				It's an optimization, but could be a bad one.
							setCodeVisualization = false;
						}
					}
				}*/
				
				if( setCodeVisualization )
				{
					m_visualization.setCode(this.getCode(bhE_CodeType.SPLASH), this.getIdClass());
				}
			}
			else if( status == bhE_CodeStatus.WAITING_ON_CODE )
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
	
	public void onGetResponseError(bhE_CodeType eCodeType)
	{
		this.setCode(eCodeType, null);

		m_codeStatus[eCodeType.ordinal()] = bhE_CodeStatus.GET_ERROR;
		
		if( m_visualization != null )
		{
			if( m_isFocused )
			{
				if( eCodeType == bhE_CodeType.COMPILED )
				{
					m_visualization.onError(eCodeType);
				}
			}
			else
			{
				if( eCodeType == bhE_CodeType.SPLASH )
				{
					m_visualization.onError(eCodeType);
				}
			}
		}
	}
	
	private void setStatusAll(bhE_CodeStatus eStatus)
	{
		for( int i = 0; i < m_codeStatus.length; i++ )
		{
			m_codeStatus[i] = eStatus;
		}
	}
	
	public bhE_CodeStatus getStatus(bhE_CodeType eType)
	{
		return m_codeStatus[eType.ordinal()];
	}
	
	private String getIdClass()
	{
		return this.getCoordinate().writeString() + bhS_App.CAJA_NAMESPACE_SUFFIX;
	}
	
	@Override
	public void setCode(bhE_CodeType eType, bhCode code)
	{
		this.setCode_private(eType, code, true);
	}
	
	private void setCode_private(bhE_CodeType eType, bhCode code, boolean updateVisualization)
	{
		super.setCode(eType, code);
		
		if( code == null )  return;
		
		this.m_codeStatus[eType.ordinal()] = bhE_CodeStatus.HAS_CODE;
		
		if( m_visualization != null )
		{
			if( m_isFocused )
			{
				//--- DRK > If focused, only COMPILED can be set on the visualization.
				if( eType == bhE_CodeType.COMPILED )
				{
					if( updateVisualization )
					{
						m_visualization.setCode(code, this.getIdClass());
					}
				}
			}
			else
			{
				//--- DRK > If not focused, only SPLASH_SCREEN can be set on the visualization.
				if( eType == bhE_CodeType.SPLASH )
				{
					if( updateVisualization )
					{
						m_visualization.setCode(code, this.getIdClass());
					}
				}
			}
		}
	}
	
	public void setVisualization(bhI_BufferCellListener visualization)
	{
		m_visualization = visualization;
	}
	
	public bhI_BufferCellListener getVisualization()
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
	
	public boolean isTouchingPoint(bhPoint point)
	{
		double cellPixelCount = bhS_App.CELL_PLUS_SPACING_PIXEL_COUNT;
		
		if ( point.getZ() != 0 )  return false;
		
		this.getCoordinate().calcPoint(s_utilPoint, 1);
		
		if
		(
			bhU_Math.isWithin(point.getX(), s_utilPoint.getX(), s_utilPoint.getX() + cellPixelCount) &&
			bhU_Math.isWithin(point.getY(), s_utilPoint.getY(), s_utilPoint.getY() + cellPixelCount)
		)
		{
			return true;
		}
		
		return false;
	}
	
	public void onServerRequestCancelled(bhE_CodeType eType)
	{
		if( eType == bhE_CodeType.COMPILED )
		{
			m_visualization.clearLoading();
		}
		
		if( this.getCode(eType) != null )
		{
			this.m_codeStatus[eType.ordinal()] = bhE_CodeStatus.HAS_CODE;
		}
		else
		{
			this.m_codeStatus[eType.ordinal()] = bhE_CodeStatus.NEEDS_CODE;
		}
	}
	
	private void clear_private()
	{
		super.clearCode();
		
		this.setStatusAll(bhE_CodeStatus.NEEDS_CODE);
		
		bhU_Debug.ASSERT(!m_isFocused);
		
		m_isFocused = false;
		m_address = null;
		m_hasBeenPreviewed = false;
	}
	
	public void clear()
	{
		this.clear_private();
		
		this.m_visualization = null;
	}
}