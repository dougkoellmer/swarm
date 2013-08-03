package b33hive.shared.structs;

import java.util.logging.Logger;

public class bhVelocitySmoother
{
	private static final Logger s_logger = Logger.getLogger(bhVelocitySmoother.class.getName());
	
	private final bhVector[] m_vectors;
	private int m_progress;
	
	public bhVelocitySmoother(int sampleCount)
	{
		m_vectors = new bhVector[sampleCount];
		for( int i = 0; i < m_vectors.length; i++ )
		{
			m_vectors[i] = new bhVector();
		}
		m_progress = 0;
	}
	
	public void clear()
	{
		m_progress = 0;
	}
	
	public void addVelocity(bhVector velocity_copied)
	{
		int index = m_progress % m_vectors.length;
		
		m_vectors[index].copy(velocity_copied);
		
		m_progress++;
	}
	
	public void calcVelocity(bhVector vector_out)
	{
		int count = Math.min(m_progress, m_vectors.length);
		vector_out.zeroOut();
		
		if( count > 0 )
		{
			bhVector longest = null;
			for( int i = 0; i < count; i++ )
			{
				if( longest == null || m_vectors[i].calcLengthSquared() > longest.calcLengthSquared() )
				{
					longest = m_vectors[i];
				}
			}
			
			vector_out.copy(longest);
		}
	}
}
