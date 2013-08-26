package swarm.shared.structs;

import swarm.shared.utils.smU_BitTricks;
import swarm.shared.app.sm;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class smBitArray extends smA_JsonEncodable
{
	private static final int BLOCK_SIZE = 32;
	
	private static final long BLOCK_SIZE_LONG = BLOCK_SIZE;
	private static final long BLOCK_MASK = 0xFFFFFFFF;
	private static final long BLOCK_MASK_SHIFTED =  BLOCK_MASK << BLOCK_SIZE_LONG;
	private static final long BLOCK_MASK_SHIFTED_NOT = ~BLOCK_MASK_SHIFTED;
	
	protected int[] m_blocks;
	protected int m_bitCount;
	
	public smBitArray()
	{
		m_bitCount = 0;
		m_blocks = null;
	}
	
	public int[] getRawBlocks()
	{
		return m_blocks;
	}
	
	public smBitArray(int bitCount)
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
	
	protected void init(int length)
	{
		m_bitCount = length;
		
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
	
	public void or(smBitArray oldArray, int columnBitCountOfOldArray, int columnBitCountOfNewArray)
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
					int mask = bhU_BitTricks.calcMaskBeforeBit(oldStartBitIndexIntoBlock);
					oldDoubleBlock &= ~mask;
				}
				
				//--- DRK > Strip off last part of the last old block if necessary.
				if( col_oldBlocks == oldEndBlockIndex )
				{
					if( oldEndBitIndexIntoBlock < (BLOCK_SIZE_LONG-1) )
					{
						int mask = bhU_BitTricks.calcMaskAfterBit(oldEndBitIndexIntoBlock);
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
						long mask = bhU_BitTricks.calcMaskAfterBit(maskStart);
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
		int block = m_blocks[blockIndex];
		int blockBit = bhU_BitTricks.calcOrdinalBit(bitIndexIntoBlock);
		
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
		int blockBit = bhU_BitTricks.calcOrdinalBit(bitIndexIntoBlock);
		
		if( (block & blockBit) != 0 )
		{
			return true;
		}
		
		return false;
	}

	@Override
	public void writeJson(smI_JsonObject json)
	{
		if( m_blocks != null )
		{
			smI_JsonArray blocksAsJson = sm.jsonFactory.createJsonArray();
			for( int i = 0; i < m_blocks.length; i++ )
			{
				blocksAsJson.addInt(m_blocks[i]);
			}
			
			sm.jsonFactory.getHelper().putJsonArray(json, smE_JsonKey.bitArray, blocksAsJson);
			sm.jsonFactory.getHelper().putInt(json, smE_JsonKey.bitArrayLength, m_bitCount);
		}		
	}

	@Override
	public void readJson(smI_JsonObject json)
	{
		Integer bitCount = sm.jsonFactory.getHelper().getInt(json, smE_JsonKey.bitArrayLength);
		
		if( bitCount == null )  return;
		
		this.init(bitCount);
		
		smI_JsonArray blocksAsJson = sm.jsonFactory.getHelper().getJsonArray(json, smE_JsonKey.bitArray);
		for( int i = 0; i < blocksAsJson.getSize(); i++ )
		{
			m_blocks[i] = blocksAsJson.getInt(i);
		}
	}
}
