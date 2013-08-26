package swarm.client.structs;

import java.util.ArrayList;

import swarm.client.entities.smBufferCell;
import swarm.shared.entities.smA_Cell;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.structs.smGridCoordinate;

public class smLocalCodeRepositoryWrapper implements smI_LocalCodeRepository
{
	private final ArrayList<smI_LocalCodeRepository> m_sources = new ArrayList<smI_LocalCodeRepository>();
	
	public void addSource(smI_LocalCodeRepository source)
	{
		m_sources.add(source);
	}
	
	@Override
	public boolean tryPopulatingCell(smGridCoordinate coordinate, smE_CodeType eType, smA_Cell cell_out)
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
