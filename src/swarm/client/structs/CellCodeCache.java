package swarm.client.structs;

import swarm.client.entities.BufferCell;
import swarm.shared.app.S_CommonApp;
import swarm.shared.entities.A_Cell;
import swarm.shared.entities.E_CodeSafetyLevel;
import swarm.shared.entities.E_CodeType;
import swarm.shared.memory.LRUCache;
import swarm.shared.structs.Code;
import swarm.shared.structs.CodePrivileges;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.time.I_TimeSource;

public class CellCodeCache implements I_LocalCodeRepository
{
	private static final class CacheCell extends A_Cell
	{
		CacheCell()
		{
		}
	}
	
	//private final smLRUCache m_cache = new smLRUCache(smS_ClientApp.CODE_CACHE_SIZE, smS_ClientApp.CODE_CACHE_EXPIRATION, smClientApp.getInstance());
	private final LRUCache m_cache;
	
	public CellCodeCache(int cacheSize, double cacheExpiration, I_TimeSource timeSource) 
	{
		m_cache = new LRUCache(cacheSize, cacheExpiration, timeSource);
	}
	
	public void cacheCell(A_Cell cell)
	{
		CacheCell cachedCell = this.getOrCreateCell(cell.getCoordinate(), true);
		if( cell.getCodePrivileges() != null )
		{
			cachedCell.getCodePrivileges().copy(cell.getCodePrivileges());
		}
		
		for( int i = 0; i < E_CodeType.values().length; i++ )
		{
			E_CodeType eType = E_CodeType.values()[i];
			Code code = cell.getCode(eType);
			
			if( code != null )
			{
				cacheCode_private(cachedCell, code, eType);
			}
		}
	}
	
	private CacheCell getOrCreateCell(GridCoordinate coord, boolean forceCreate)
	{
		String coordHash = coord.writeString();
		CacheCell cell = (CacheCell) m_cache.get(coordHash);
		
		if( cell == null && forceCreate )
		{
			cell = new CacheCell();
			m_cache.put(coordHash, cell);
		}
		
		return cell;
	}
	
	public void cacheCode(GridCoordinate coord, Code code, E_CodeType eType)
	{
		CacheCell cell = this.getOrCreateCell(coord, true);
		cacheCode_private(cell, code, eType);
	}
	
	private void cacheCode_private(CacheCell cell, Code code, E_CodeType eType)
	{
		cell.setCode(eType, code);
	}
	
	@Override
	public boolean tryPopulatingCell(GridCoordinate coordinate, E_CodeType eType, A_Cell outCell)
	{
		CacheCell cachedCell = this.getOrCreateCell(coordinate, false);
		
		if( cachedCell == null )
		{
			return false;
		}
		
		Code code = cachedCell.getCode(eType);
		
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
			if( eType == E_CodeType.SOURCE || code.getSafetyLevel() == E_CodeSafetyLevel.VIRTUAL_DYNAMIC_SANDBOX )
			{
				outCell.getCodePrivileges().copy(cachedCell.getCodePrivileges());
			}
		}

		return foundCode;
	}
	
	public void clear(GridCoordinate coordinate)
	{
		String hash = coordinate.writeString();
		
		m_cache.remove(hash);
	}
}