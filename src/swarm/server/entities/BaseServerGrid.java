package swarm.server.entities;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.BitSet;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.I_BlobKey;
import swarm.server.data.blob.U_Blob;
import swarm.server.data.blob.U_Serialization;
import swarm.server.structs.ServerBitArray;
import swarm.server.structs.SerializableDate;
import swarm.server.structs.ServerGridCoordinate;
import swarm.shared.app.S_CommonApp;
import swarm.shared.entities.A_Grid;
import swarm.shared.structs.BitArray;
import swarm.shared.structs.GridCoordinate;

/**
 * ...
 * @author 
 */
public class BaseServerGrid extends A_Grid implements I_Blob
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
	
	private static final Logger s_logger = Logger.getLogger(BaseServerGrid.class.getName());
	
	private static final int EXTERNAL_VERSION = 2;
	
	private final SerializableDate m_lastUpdated = new SerializableDate();
	
	public BaseServerGrid()
	{
		
	}
	
	public void markCoordinateAvailable(GridCoordinate coordinate)
	{
		int bitIndex = coordinate.calcArrayIndex(m_width);
		m_ownership.set(bitIndex, false);
	}
	
	public void markCoordinateTaken(GridCoordinate coordinate)
	{
		int bitIndex = coordinate.calcArrayIndex(m_width);
		m_ownership.set(bitIndex, true);
	}
	
	public ServerGridCoordinate findFreeCoordinate(int expansionDelta, GridCoordinate preference) throws GridException
	{
		return this.private_findFreeCoordinate(expansionDelta, preference);
	}
	
	private ServerGridCoordinate private_findFreeCoordinate(int expansionDelta, GridCoordinate preference) throws GridException
	{
		int currentGridWidth = this.getWidth();
		int currentGridHeight = this.getHeight();
		
		Integer freeIndex = null;
		
		if( preference != null )
		{
			if( preference.getM() >= currentGridWidth || preference.getN() >= currentGridHeight )
			{
				int expansionDeltaX = (preference.getM()+1) - currentGridWidth;
				int expansionDeltaY = (preference.getN()+1) - currentGridHeight;
				
				int remainderX = expansionDeltaX % expansionDelta;
				expansionDeltaX -= remainderX;
				if( remainderX > 0 )
				{
					expansionDeltaX += expansionDelta;
				}
				
				int remainderY = expansionDeltaY % expansionDelta;
				expansionDeltaY -= remainderY;
				if( remainderY > 0 )
				{
					expansionDeltaY += expansionDelta;
				}
				
				expansionDeltaX = expansionDeltaX < 0 ? 0 : expansionDeltaX;
				expansionDeltaY = expansionDeltaY < 0 ? 0 : expansionDeltaY;
				
				int newWidth = currentGridWidth + expansionDeltaX;
				int newHeight = currentGridHeight + expansionDeltaY;
				this.expandToSize(newWidth, newHeight);
				
				return this.private_findFreeCoordinate(expansionDelta, preference);
			}
			else
			{
				int coordIndex = preference.calcArrayIndex(currentGridWidth);
				
				if( this.isTaken(coordIndex) )
				{
					throw new GridException(GridException.Reason.PREFERENCE_TAKEN, "Preference is taken.");
				}
				else
				{
					freeIndex = coordIndex;
				}
			}
		}
		
		if( freeIndex == null && currentGridWidth > 0 && currentGridHeight > 0 )
		{
			//--- DRK > Figure out a random index where we should start looking from.
			//---		Note that Math.random() is synchronized, so if there were a ton
			//---		of people signing up at once, there would be contention.
			//---		I doubt this is really an issue, especially since the DB contention
			//---		would be much more of a problem, but slightly more proper would
			//---		be for the grid to serialize its own random number generator.
			int byteCount = m_ownership.getByteCount();
			double rand = Math.random();
			int randomIndex = (int) (rand * byteCount);
			
			//--- DRK > Start looking backwards from random position.
			for( int i = randomIndex; i >= 0; i-- )
			{
				Integer indexOfFreeBit = m_ownership.findIndexOfFreeBit(i);
				
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
					Integer indexOfFreeBit = m_ownership.findIndexOfFreeBit(i);
					
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
			int newWidth = currentGridWidth + expansionDelta;
			int newHeight = currentGridHeight + expansionDelta;
			this.expandToSize(newWidth, newHeight);
			
			//--- DRK > Recurse into this function.
			return this.private_findFreeCoordinate(expansionDelta, null);
		}
		
		/*if( freeIndex == null )
		{
			//--- DRK > Should only get thrown if there's a problem with the above algorithm which needs fixing.
			throw new GridException(GridException.Reason.ALGORITHMIC, "Couldn't find a free cell for whatever reason.");
		}*/
		
		//--- DRK > Mark the free index as taken and return a coordinate.
		m_ownership.set(freeIndex, true);
		int m = freeIndex % this.getWidth();
		int n = (freeIndex - m) / this.getWidth();
		
		return new ServerGridCoordinate(m, n);
	}
	
	@Override
	protected BitArray createBitArray()
	{
		return new ServerBitArray();
	}
	
	@Override
	protected BitArray createBitArray(int bitCount)
	{
		return new ServerBitArray(bitCount);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		out.writeInt(this.getWidth());
		out.writeInt(this.getHeight());
		out.writeInt(this.getCellWidth());
		out.writeInt(this.getCellHeight());
		out.writeInt(this.getCellPadding());
		
		U_Serialization.writeNullableObject((ServerBitArray)m_ownership, out);
		
		m_lastUpdated.writeExternal(out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		if( externalVersion > 1 )
		{
			m_width = in.readInt();
			m_height = in.readInt();
			m_cellWidth = in.readInt();
			m_cellHeight = in.readInt();
			m_cellPadding = in.readInt();
		}
		
		m_ownership = U_Serialization.readNullableObject(ServerBitArray.class, in);
		
		m_lastUpdated.readExternal(in);
	}

	@Override
	public String getKind()
	{
		return "sm_grid";
	}
	
	@Override
	public E_BlobCacheLevel getMaximumCacheLevel()
	{
		return E_BlobCacheLevel.LOCAL;
	}

	@Override
	public Map<String, Object> getQueryableProperties()
	{
		return null;
	}
}