package com.b33hive.client.structs;

import com.b33hive.client.app.bhClientApp;
import com.b33hive.client.app.bhS_ClientApp;
import com.b33hive.client.entities.bhBufferCell;
import com.b33hive.shared.app.bhS_App;
import com.b33hive.shared.entities.bhA_Cell;
import com.b33hive.shared.entities.bhE_CodeSafetyLevel;
import com.b33hive.shared.entities.bhE_CodeType;
import com.b33hive.shared.memory.bhLRUCache;
import com.b33hive.shared.structs.bhCode;
import com.b33hive.shared.structs.bhCodePrivileges;
import com.b33hive.shared.structs.bhGridCoordinate;

public class bhCellCodeCache implements bhI_LocalCodeRepository
{
	private static final class bhCacheCell extends bhA_Cell
	{
		bhCacheCell()
		{
		}
	}
	
	private static final bhCellCodeCache s_instance = new bhCellCodeCache();
	
	private final bhLRUCache m_cache = new bhLRUCache(bhS_ClientApp.CODE_CACHE_SIZE, bhS_ClientApp.CODE_CACHE_EXPIRATION, bhClientApp.getInstance());
	
	private bhCellCodeCache() 
	{
	}
	
	public static bhCellCodeCache getInstance()
	{
		return s_instance;
	}
	
	public void cacheCell(bhA_Cell cell)
	{
		bhCacheCell cachedCell = this.getOrCreateCell(cell.getCoordinate(), true);
		if( cell.getCodePrivileges() != null )
		{
			cachedCell.getCodePrivileges().copy(cell.getCodePrivileges());
		}
		
		for( int i = 0; i < bhE_CodeType.values().length; i++ )
		{
			bhE_CodeType eType = bhE_CodeType.values()[i];
			bhCode code = cell.getCode(eType);
			
			if( code != null )
			{
				cacheCode_private(cachedCell, code, eType);
			}
		}
	}
	
	private bhCacheCell getOrCreateCell(bhGridCoordinate coord, boolean forceCreate)
	{
		String coordHash = coord.writeString();
		bhCacheCell cell = (bhCacheCell) m_cache.get(coordHash);
		
		if( cell == null && forceCreate )
		{
			cell = new bhCacheCell();
			m_cache.put(coordHash, cell);
		}
		
		return cell;
	}
	
	public void cacheCode(bhGridCoordinate coord, bhCode code, bhE_CodeType eType)
	{
		bhCacheCell cell = this.getOrCreateCell(coord, true);
		cacheCode_private(cell, code, eType);
	}
	
	private void cacheCode_private(bhCacheCell cell, bhCode code, bhE_CodeType eType)
	{
		cell.setCode(eType, code);
	}
	
	@Override
	public boolean tryPopulatingCell(bhGridCoordinate coordinate, bhE_CodeType eType, bhA_Cell outCell)
	{
		bhCacheCell cachedCell = this.getOrCreateCell(coordinate, false);
		
		if( cachedCell == null )
		{
			return false;
		}
		
		bhCode code = cachedCell.getCode(eType);
		
		boolean foundCode = false;
		
		if( code != null )
		{
			outCell.setCode(eType, code);
			
			foundCode = true;
		}
		else
		{
			code = cachedCell.getStandInCode(eType);
			
			if( code != null )
			{
				outCell.setCode(eType, code);
				
				foundCode = true;
			}
		}
		
		if( foundCode )
		{
			if( eType == bhE_CodeType.SOURCE || code.getSafetyLevel() == bhE_CodeSafetyLevel.REQUIRES_DYNAMIC_SANDBOX )
			{
				outCell.getCodePrivileges().copy(cachedCell.getCodePrivileges());
			}
		}

		return foundCode;
	}
	
	public void clear(bhGridCoordinate coordinate)
	{
		String hash = coordinate.writeString();
		
		m_cache.remove(hash);
	}
}