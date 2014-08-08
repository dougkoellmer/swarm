package swarm.shared.structs;

import swarm.shared.utils.U_Bits;
import swarm.shared.app.BaseAppContext;
import swarm.shared.debugging.U_Debug;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class BitArray extends A_JsonEncodable
{
	private static final int BLOCK_SIZE = 32;
	
	private static final long BLOCK_SIZE_LONG = BLOCK_SIZE;
	private static final long BLOCK_MASK = 0xFFFFFFFF;
	private static final long BLOCK_MASK_SHIFTED =  BLOCK_MASK << BLOCK_SIZE_LONG;
	private static final long BLOCK_MASK_SHIFTED_NOT = ~BLOCK_MASK_SHIFTED;
	
	protected int[] m_blocks;
	protected int m_bitCount;
	
	public BitArray()
	{
		m_bitCount = 0;
		m_blocks = null;
	}
	
	public BitArray clone()
	{
		BitArray cloned = new BitArray();
		cloned.m_bitCount = this.m_bitCount;
		
		if( this.m_blocks == null )
		{
			cloned.m_blocks = null;
		}
		else
		{
			cloned.m_blocks = new int[this.m_blocks.length];
			
			for( int i = 0; i < cloned.m_blocks.length; i++ )
			{
				cloned.m_blocks[i] = this.m_blocks[i];
			}
		}
		
		return cloned;
	}
	
	public int[] getRawBlocks()
	{
		return m_blocks;
	}
	
	public BitArray(int bitCount)
	{
		init(bitCount);
	}
	
	public int getBitCount()
	{
		return m_bitCount;
	}
	
	public int getByteCount()
	{
		return m_blocks.length;
	}
	
	public Integer findIndexOfFreeBit(int blockIndex)
	{
		int block = m_blocks[blockIndex];
		if( block == BLOCK_MASK )
		{
			return null;
		}
		else
		{
			int startingBitIndex = blockIndex*BLOCK_SIZE;
			int limit = Math.min(startingBitIndex + BLOCK_SIZE, m_bitCount);
			for( int i = startingBitIndex; i < limit; i++ )
			{
				if( !this.isSet(i) )
				{
					return i;
				}
			}
			
			return null;
		}
	}
	
	protected void init(int bitCount)
	{
		m_bitCount = bitCount;
		
		int bitIndexIntoBlock = calcBitIndexModBlock(m_bitCount);
		int blockCount = calcBlockIndex(m_bitCount, bitIndexIntoBlock);
		
		if( bitIndexIntoBlock > 0 )
		{
			blockCount++;
		}
		
		m_blocks = new int[blockCount];
	}
	
	private static int calcBitIndexModBlock(int bitIndex)
	{
		return bitIndex % BLOCK_SIZE;
	}
	
	private static int calcBlockIndex(int bitIndex, int bitIndexModBlock)
	{
		return (bitIndex - bitIndexModBlock) / BLOCK_SIZE;
	}
	
	public void or(BitArray oldArray, int columnBitCountOfOldArray, int columnBitCountOfNewArray)
	{
		int[] oldBlocks = oldArray.m_blocks;
		int[] newBlocks = this.m_blocks;
		int oldBitLength = oldArray.m_bitCount;
		int newBlockCount = newBlocks.length;
		
		int rowCountOfOldArray = oldBitLength / columnBitCountOfOldArray;
		
		for( int row = 0; row < rowCountOfOldArray; row++ )
		{
			int oldStartBitIndex = row * columnBitCountOfOldArray;
			int oldStartBitIndexIntoBlock = calcBitIndexModBlock(oldStartBitIndex);
			int oldStartBlockIndex = calcBlockIndex(oldStartBitIndex, oldStartBitIndexIntoBlock);
			
			int oldEndBitIndex = oldStartBitIndex + columnBitCountOfOldArray - 1; // inclusive
			int oldEndBitIndexIntoBlock = calcBitIndexModBlock(oldEndBitIndex);
			int oldEndBlockIndex = calcBlockIndex(oldEndBitIndex, oldEndBitIndexIntoBlock);
			
			int newStartBitIndex = row * columnBitCountOfNewArray;
			int newStartBitIndexIntoBlock = calcBitIndexModBlock(newStartBitIndex);
			int newStartBlockIndex = calcBlockIndex(newStartBitIndex, newStartBitIndexIntoBlock);
			
			int offset = newStartBitIndexIntoBlock - oldStartBitIndexIntoBlock;
			
			for( int col_oldBlocks = oldStartBlockIndex, col_newBlocks = newStartBlockIndex; col_oldBlocks <= oldEndBlockIndex; col_oldBlocks++, col_newBlocks++ )
			{
				long oldDoubleBlock = oldBlocks[col_oldBlocks];
				//oldDoubleBlock &= BLOCK_MASK_SHIFTED_NOT;
				
				boolean breakOut = false;
				
				//--- DRK > Strip off first part of the current old block.
				if( oldStartBitIndexIntoBlock > 0 )
				{
					int mask = U_Bits.calcMaskBeforeBit(oldStartBitIndexIntoBlock);
					oldDoubleBlock &= ~mask;
				}
				
				//--- DRK > Strip off last part of the last old block if necessary.
				if( col_oldBlocks == oldEndBlockIndex )
				{
					if( oldEndBitIndexIntoBlock < (BLOCK_SIZE_LONG-1) )
					{
						int mask = U_Bits.calcMaskAfterBit(oldEndBitIndexIntoBlock);
						oldDoubleBlock &= ~mask;
						oldDoubleBlock &= BLOCK_MASK_SHIFTED_NOT;
					}
				}
				else if( col_oldBlocks < oldEndBlockIndex )
				{
					if( oldStartBitIndexIntoBlock > 0 )
					{
						int maskStart;
						if( col_oldBlocks == oldEndBlockIndex-1 )
						{
							if( oldEndBitIndexIntoBlock >= oldStartBitIndexIntoBlock )
							{
								maskStart = oldStartBitIndexIntoBlock-1;
							}
							else
							{
								maskStart = oldEndBitIndexIntoBlock;
								breakOut = true;
							}
						}
						else
						{
							maskStart = oldStartBitIndexIntoBlock-1;
						}
					
						long oldDoubleBlockEnd = oldBlocks[col_oldBlocks+1];
						oldDoubleBlock |= (oldDoubleBlockEnd<<BLOCK_SIZE_LONG);
						long mask = U_Bits.calcMaskAfterBit(maskStart);
						oldDoubleBlock &= ~(mask<<BLOCK_SIZE_LONG);
					}
				}
				else
				{
					assert(false);//, "Unreachable case...well apparently not.");
				}
				
				//--- DRK > Can't logically shift by negative numbers because java
				//---		doesn't have signed/unsigned representations, hence the if/else.
				if( offset < 0 )
				{
					oldDoubleBlock >>>= (-offset);
				}
				else
				{
					oldDoubleBlock <<= offset;
				}
				
				newBlocks[col_newBlocks] |= (BLOCK_MASK & oldDoubleBlock);
				
				if( col_newBlocks+1 < newBlockCount )
				{
					newBlocks[col_newBlocks+1] |= ((BLOCK_MASK_SHIFTED & oldDoubleBlock) >> BLOCK_SIZE_LONG);
				}
				
				if( breakOut )
				{
					break;
				}
			}
		}
	}
	
	public void set(int bitIndex, boolean value)
	{
		int bitIndexIntoBlock = calcBitIndexModBlock(bitIndex);
		int blockIndex = calcBlockIndex(bitIndex, bitIndexIntoBlock);
		
		if( blockIndex >= m_blocks.length )
		{
			int i = 0;
		}
		
		int block = m_blocks[blockIndex];
		int blockBit = U_Bits.calcOrdinalBit(bitIndexIntoBlock);
		
		if( value )
		{
			block |= blockBit;
		}
		else
		{
			block &= ~blockBit;
		}
		
		
		m_blocks[blockIndex] = block;
	}
	
	public boolean isSet(int bitIndex)
	{
		int bitIndexIntoBlock = calcBitIndexModBlock(bitIndex);
		int blockIndex = calcBlockIndex(bitIndex, bitIndexIntoBlock);
		int block = m_blocks[blockIndex];
		int blockBit = U_Bits.calcOrdinalBit(bitIndexIntoBlock);
		
		if( (block & blockBit) != 0 )
		{
			return true;
		}
		
		return false;
	}

	@Override
	public void writeJson(I_JsonObject json_out, A_JsonFactory factory)
	{
		if( m_blocks != null )
		{
			I_JsonArray blocksAsJson = factory.createJsonArray();
			for( int i = 0; i < m_blocks.length; i++ )
			{
				blocksAsJson.addInt(m_blocks[i]);
			}
			
			factory.getHelper().putJsonArray(json_out, E_JsonKey.bitArray, blocksAsJson);
			factory.getHelper().putInt(json_out, E_JsonKey.bitArrayLength, m_bitCount);
		}		
	}

	@Override
	public void readJson(I_JsonObject json, A_JsonFactory factory)
	{
		Integer bitCount = factory.getHelper().getInt(json, E_JsonKey.bitArrayLength);
		
		if( bitCount == null )  return;
		
		this.init(bitCount);
		
		I_JsonArray blocksAsJson = factory.getHelper().getJsonArray(json, E_JsonKey.bitArray);
		for( int i = 0; i < blocksAsJson.getSize(); i++ )
		{
			m_blocks[i] = blocksAsJson.getInt(i);
		}
	}
}
