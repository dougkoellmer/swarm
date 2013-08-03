package b33hive.server.entities;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.BitSet;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import b33hive.server.app.bhS_ServerApp;
import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhI_Blob;
import b33hive.server.data.blob.bhI_BlobKeySource;
import b33hive.server.data.blob.bhU_Blob;
import b33hive.server.data.blob.bhU_Serialization;
import b33hive.server.structs.bhBitArray;
import b33hive.server.structs.bhDate;
import b33hive.server.structs.bhServerGridCoordinate;
import b33hive.shared.app.bhS_App;
import b33hive.shared.entities.bhA_Grid;
import b33hive.shared.structs.bhGridCoordinate;

/**
 * ...
 * @author 
 */
public class bhServerGrid extends bhA_Grid implements bhI_Blob
{
	public static final class GridException extends Exception
	{
		public static enum Reason
		{
			ALGORITHMIC,
			PREFERENCE_TAKEN
		};
		
		private final Reason m_reason;
		
		public GridException(Reason reason, String message)
		{
			super(message);
			
			m_reason = reason;
		}
	}
	
	private static final Logger s_logger = Logger.getLogger(bhServerGrid.class.getName());
	
	private static final int EXTERNAL_VERSION = 1;
	
	private bhBitArray m_bitArray = null;
	
	private final bhDate m_lastUpdated = new bhDate();
	
	public bhServerGrid()
	{
		
	}
	
	public boolean isTaken(bhGridCoordinate coordinate)
	{
		int bitIndex = coordinate.calcArrayIndex(m_size);
		return m_bitArray.isSet(bitIndex);
	}
	
	public void markCoordinateAvailable(bhGridCoordinate coordinate)
	{
		int bitIndex = coordinate.calcArrayIndex(m_size);
		m_bitArray.set(bitIndex, false);
	}
	
	public bhServerGridCoordinate findFreeCoordinate(int expansionDelta, bhGridCoordinate preference) throws GridException
	{
		return this.private_findFreeCoordinate(expansionDelta, preference);
	}
	
	private bhServerGridCoordinate private_findFreeCoordinate(int expansionDelta, bhGridCoordinate preference) throws GridException
	{
		int currentGridSize = this.getSize();
		Integer freeIndex = null;
		
		if( preference != null )
		{
			int sizeRequiredForPreference = Math.max(preference.getM(), preference.getN()) + 1;
			if( sizeRequiredForPreference > currentGridSize )
			{
				int expansionDeltaModified = sizeRequiredForPreference - currentGridSize;
				int remainder = expansionDeltaModified % expansionDelta;
				expansionDeltaModified -= remainder;
				if( remainder > 0 )
				{
					expansionDeltaModified += expansionDelta;
				}
				
				int newSize = currentGridSize + expansionDeltaModified;
				this.expandToSize(newSize);
				
				return this.private_findFreeCoordinate(expansionDeltaModified, preference);
			}
			else
			{
				int coordIndex = preference.calcArrayIndex(currentGridSize);
				
				if( m_bitArray.isSet(coordIndex) )
				{
					throw new GridException(GridException.Reason.PREFERENCE_TAKEN, "Preference is taken.");
				}
				else
				{
					freeIndex = coordIndex;
				}
			}
		}
		
		if( freeIndex == null && currentGridSize > 0 )
		{
			//--- DRK > Figure out a random index where we should start looking from.
			//---		Note that Math.random() is synchronized, so if there were a ton
			//---		of people signing up at once, there would be contention.
			//---		I doubt this is really an issue, especially since the DB contention
			//---		would be much more of a problem, but slightly more proper would
			//---		be for the grid to serialize its own random number generator.
			int byteCount = m_bitArray.getByteCount();
			double rand = Math.random();
			int randomIndex = (int) (rand * byteCount);
			
			//--- DRK > Start looking backwards from random position.
			for( int i = randomIndex; i >= 0; i-- )
			{
				Integer indexOfFreeBit = m_bitArray.findIndexOfFreeBit(i);
				
				if( indexOfFreeBit != null )
				{
					freeIndex = indexOfFreeBit;
					break;
				}
			}
			
			//--- DRK > If the backwards search came up empty, look forwards.
			if( freeIndex == null )
			{
				for( int i = randomIndex+1; i < byteCount; i++ )
				{
					Integer indexOfFreeBit = m_bitArray.findIndexOfFreeBit(i);
					
					if( indexOfFreeBit != null )
					{
						freeIndex = indexOfFreeBit;
						break;
					}
				}
			}
		}
		
		//--- DRK > Entering this if-block means that the entire grid is taken up.
		//---		As a result, we increase its size so we have more free space.
		if( freeIndex == null )
		{
			int newSize = currentGridSize + expansionDelta;
			this.expandToSize(newSize);
			
			//--- DRK > Recurse into this function.
			return this.private_findFreeCoordinate(expansionDelta, null);
		}
		
		/*if( freeIndex == null )
		{
			//--- DRK > Should only get thrown if there's a problem with the above algorithm which needs fixing.
			throw new GridException(GridException.Reason.ALGORITHMIC, "Couldn't find a free cell for whatever reason.");
		}*/
		
		//--- DRK > Mark the free index as taken and return a coordinate.
		m_bitArray.set(freeIndex, true);
		int m = freeIndex % this.getSize();
		int n = (freeIndex - m) / this.getSize();
		return new bhServerGridCoordinate(m, n);
	}
	
	private void expandToSize(int size)
	{
		int oldSize = this.getSize();

		m_size = size;
		
		bhBitArray oldArray = m_bitArray;
		
		m_bitArray = new bhBitArray(size*size);
		
		if( oldArray != null )
		{
			m_bitArray.or(oldArray, oldSize, size);
		}
		
		//bhT_Grid.validateExpansion(m_bitArray, oldSize, m_size);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		out.writeInt(this.getSize());
		
		bhU_Serialization.writeNullableObject(m_bitArray, out);
		
		m_lastUpdated.writeExternal(out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		m_size = in.readInt();
		
		m_bitArray = bhU_Serialization.readNullableObject(bhBitArray.class, in);
		
		m_lastUpdated.readExternal(in);
	}

	@Override
	public String getKind()
	{
		return bhS_BlobKeyPrefix.GRID_PREFIX;
	}
	
	@Override
	public bhE_BlobCacheLevel getMaximumCacheLevel()
	{
		return bhE_BlobCacheLevel.LOCAL;
	}

	@Override
	public Map<String, Object> getQueryableProperties()
	{
		return null;
	}
}