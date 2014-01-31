package swarm.client.structs;

import java.util.ArrayList;

import swarm.client.entities.BufferCell;
import swarm.shared.entities.A_Cell;
import swarm.shared.entities.E_CodeType;
import swarm.shared.structs.GridCoordinate;

public class LocalCodeRepositoryWrapper implements I_LocalCodeRepository
{
	private final ArrayList<I_LocalCodeRepository> m_sources = new ArrayList<I_LocalCodeRepository>();
	
	public void addSource(I_LocalCodeRepository source)
	{
		m_sources.add(source);
	}
	
	public void removeAllSources()
	{
		m_sources.clear();
	}
	
	@Override
	public boolean tryPopulatingCell(GridCoordinate coordinate, E_CodeType eType, A_Cell cell_out)
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
