package swarm.server.structs;

import swarm.shared.structs.bhBitArray;
import swarm.shared.utils.bhU_BitTricks;
import swarm.shared.debugging.bhU_Debug;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.logging.Logger;

public class bhServerBitArray extends bhBitArray implements Externalizable
{
	private static final int EXTERNAL_VERSION = 2;
	
	private static final Logger s_logger = Logger.getLogger(bhServerBitArray.class.getName());
	
	public bhServerBitArray()
	{
		super();
	}
	
	public bhServerBitArray(int bitCount)
	{
		super(bitCount);
	}
	
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		out.writeInt(m_bitCount);
		
		for( int i = 0; i < m_blocks.length; i++ )
		{
			out.writeInt(m_blocks[i]);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		m_bitCount = in.readInt();
		
		init(m_bitCount);

		for( int i = 0; i < m_blocks.length; i++ )
		{
			m_blocks[i] = in.readInt();
		}
	}
}
