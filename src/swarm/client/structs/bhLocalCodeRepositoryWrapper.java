package swarm.client.structs;

import java.util.ArrayList;

import swarm.client.entities.bhBufferCell;
import swarm.shared.entities.bhA_Cell;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.structs.bhGridCoordinate;

public class bhLocalCodeRepositoryWrapper implements bhI_LocalCodeRepository
{
	private final ArrayList<bhI_LocalCodeRepository> m_sources = new ArrayList<bhI_LocalCodeRepository>();
	
	public void addSource(bhI_LocalCodeRepository source)
	{
		m_sources.add(source);
	}
	
	@Override
	public boolean tryPopulatingCell(bhGridCoordinate coordinate, bhE_CodeType eType, bhA_Cell cell_out)
	{
		for( int i = 0; i < m_sources.size(); i++ )
		{
			if( m_sources.get(i).tryPopulatingCell(coordinate, eType, cell_out) )
			{
				return true;
			}
		}
		
		return false;
	}
}
