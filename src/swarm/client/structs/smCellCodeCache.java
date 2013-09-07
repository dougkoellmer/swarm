package swarm.client.structs;

import swarm.client.entities.smBufferCell;
import swarm.shared.app.smS_App;
import swarm.shared.entities.smA_Cell;
import swarm.shared.entities.smE_CodeSafetyLevel;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.memory.smLRUCache;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smCodePrivileges;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.time.smI_TimeSource;

public class smCellCodeCache implements smI_LocalCodeRepository
{
	private static final class smCacheCell extends smA_Cell
	{
		smCacheCell()
		{
		}
	}
	
	//private final smLRUCache m_cache = new smLRUCache(smS_ClientApp.CODE_CACHE_SIZE, smS_ClientApp.CODE_CACHE_EXPIRATION, smClientApp.getInstance());
	private final smLRUCache m_cache;
	
	public smCellCodeCache(int cacheSize, double cacheExpiration, smI_TimeSource timeSource) 
	{
		m_cache = new smLRUCache(cacheSize, cacheExpiration, timeSource);
	}
	
	public void cacheCell(smA_Cell cell)
	{
		smCacheCell cachedCell = this.getOrCreateCell(cell.getCoordinate(), true);
		if( cell.getCodePrivileges() != null )
		{
			cachedCell.getCodePrivileges().copy(cell.getCodePrivileges());
		}
		
		for( int i = 0; i < smE_CodeType.values().length; i++ )
		{
			smE_CodeType eType = smE_CodeType.values()[i];
			smCode code = cell.getCode(eType);
			
			if( code != null )
			{
				cacheCode_private(cachedCell, code, eType);
			}
		}
	}
	
	private smCacheCell getOrCreateCell(smGridCoordinate coord, boolean forceCreate)
	{
		String coordHash = coord.writeString();
		smCacheCell cell = (smCacheCell) m_cache.get(coordHash);
		
		if( cell == null && forceCreate )
		{
			cell = new smCacheCell();
			m_cache.put(coordHash, cell);
		}
		
		return cell;
	}
	
	public void cacheCode(smGridCoordinate coord, smCode code, smE_CodeType eType)
	{
		smCacheCell cell = this.getOrCreateCell(coord, true);
		cacheCode_private(cell, code, eType);
	}
	
	private void cacheCode_private(smCacheCell cell, smCode code, smE_CodeType eType)
	{
		cell.setCode(eType, code);
	}
	
	@Override
	public boolean tryPopulatingCell(smGridCoordinate coordinate, smE_CodeType eType, smA_Cell outCell)
	{
		smCacheCell cachedCell = this.getOrCreateCell(coordinate, false);
		
		if( cachedCell == null )
		{
			return false;
		}
		
		smCode code = cachedCell.getCode(eType);
		
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
			if( eType == smE_CodeType.SOURCE || code.getSafetyLevel() == smE_CodeSafetyLevel.VIRTUAL_DYNAMIC_SANDBOX )
			{
				outCell.getCodePrivileges().copy(cachedCell.getCodePrivileges());
			}
		}

		return foundCode;
	}
	
	public void clear(smGridCoordinate coordinate)
	{
		String hash = coordinate.writeString();
		
		m_cache.remove(hash);
	}
}