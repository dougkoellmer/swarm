package b33hive.client.entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import b33hive.client.managers.bhCellCodeManager;
import b33hive.client.structs.bhI_LocalCodeRepository;
import b33hive.shared.app.bhS_App;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.entities.bhA_Cell;
import b33hive.shared.entities.bhE_CodeSafetyLevel;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.entities.bhA_User;
import b33hive.shared.json.bhA_JsonFactory;
import b33hive.shared.json.bhE_JsonKey;
import b33hive.shared.json.bhI_JsonArray;
import b33hive.shared.json.bhI_JsonObject;
import b33hive.shared.json.bhJsonHelper;
import b33hive.shared.structs.bhCellAddress;
import b33hive.shared.structs.bhCellAddressMapping;
import b33hive.shared.structs.bhCode;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.structs.bhPoint;


/**
 * ...
 * @author 
 */
public class bhA_ClientUser extends bhA_User implements bhI_LocalCodeRepository
{
	private static final Logger s_logger = Logger.getLogger(bhCellCodeManager.class.getName());
	
	private class bhCellIterator implements Iterator<bhUserCell>
	{
		private ArrayList<bhUserCell> m_cells = null;
		private int m_currentIndex = 0;
		
		private bhCellIterator(ArrayList<bhUserCell> cells)
		{
			m_cells = cells;
		}
		
		@Override
		public boolean hasNext()
		{
			return m_currentIndex < m_cells.size();
		}

		@Override
		public bhUserCell next()
		{
			bhUserCell next = m_cells.get(m_currentIndex);
			m_currentIndex++;
			return next;
		}

		@Override
		public void remove()
		{
			bhU_Debug.ASSERT(false, "Can't remove.");
		}
	}
	
	private final bhPoint m_lastPosition = new bhPoint();
	
	private final ArrayList<bhUserCell> m_cells = new ArrayList<bhUserCell>();
	
	private boolean m_isPopulated;
	
	public bhA_ClientUser()
	{
		
	}
	
	public bhCode getCode(bhGridCoordinate coord, bhE_CodeType eType)
	{
		bhUserCell cell = getCellStrict(coord);
		
		if( cell != null )
		{
			if( cell.m_hasChangedCode && eType == bhE_CodeType.SOURCE )
			{
				return cell.getChangedCode();
			}
			else
			{
				return cell.getCode(eType);
			}
		}
		
		return null;
	}
	
	public bhCellAddress getCellAddress(bhCellAddressMapping mapping)
	{
		for( int i = 0; i < m_cells.size(); i++ )
		{
			bhUserCell cell = m_cells.get(i);

			bhGridCoordinate coordinate = cell.getCoordinate();
			
			if( coordinate.isEqualTo(mapping.getCoordinate()) )
			{
				bhCellAddress ithAddress = cell.getAddress();

				return ithAddress;
			}
		}
		
		return null;
	}
	
	public bhCellAddressMapping getCellAddressMapping(bhCellAddress address)
	{
		for( int i = 0; i < m_cells.size(); i++ )
		{
			bhUserCell cell = m_cells.get(i);

			bhCellAddress ithAddress = cell.getAddress();
			
			if( ithAddress != null )
			{
				if( ithAddress.isEqualTo(address) )
				{
					return new bhCellAddressMapping(cell.getCoordinate());
				}
			}
		}
		
		return null;
	}
	
	@Override
	public boolean tryPopulatingCell(bhGridCoordinate coord, bhE_CodeType eType, bhA_Cell outCell)
	{
		bhUserCell thisCell = getCellLenient(coord);
		
		if( thisCell != null )
		{
			bhCode toReturn = null;
			
			if( thisCell.m_hasChangedCode && eType == bhE_CodeType.SOURCE )
			{
				toReturn = thisCell.m_changedCode;
			}
			else
			{
				bhCode code = thisCell.getCode(eType);
				
				if( code != null )
				{
					toReturn = code;
				}
				else
				{
					toReturn = thisCell.getStandInCode(eType);
				}
			}
			
			if( toReturn != null )
			{
				if( eType == bhE_CodeType.SOURCE || toReturn.getSafetyLevel() == bhE_CodeSafetyLevel.REQUIRES_DYNAMIC_SANDBOX )
				{
					outCell.getCodePrivileges().copy(thisCell.getCodePrivileges());
				}
				
				outCell.setCode(eType, toReturn);
				
				return true;
			}
		}
		
		return false;
	}
	
	//TODO(DRK) Returning a boolean is kinda ghetto here.
	public boolean setInitialCellData(bhGridCoordinate coord, bhA_Cell newCellData)
	{
		bhUserCell thisCell = getCellStrict(coord);
		
		if( thisCell != null )
		{
			if( newCellData.getCodePrivileges() != null )
			{
				thisCell.getCodePrivileges().copy(newCellData.getCodePrivileges());
			}
			
			for( int i = 0; i < bhE_CodeType.values().length; i++ )
			{
				bhE_CodeType eType = bhE_CodeType.values()[i];
				bhCode newCode = newCellData.getCode(eType);
				
				if( newCode != null )
				{
					bhCode existingCode = thisCell.getCode(eType);
					
					if( existingCode == null )
					{
						thisCell.setCode(eType, newCode);
					}
					else
					{
						//--- DRK > Future fringe case could trip this, if compiled can stand in for source,
						//---		or vice versa...for now, they can't.
						boolean isSource = eType == bhE_CodeType.SOURCE;
						if( isSource )
						{
							bhU_Debug.ASSERT(false, "Didn't expect source in setInitialData");
							
							return false;
						}
								
						//--- DRK > Valid fringe case can trip this assert if we didn't check for non-zero-length code.
						//---		(1) User goes to a cell they own that has no code.
						//---		(2) They have the editing code state closed...they hit refresh, which nukes all code.
						//---		(3) They then immediately open the editing code state, so two transactions are out at once.
						//---		(4) Because of the stand-in code system, the first transaction that comes back will populate
						//---			the cell with all three code types (an empty string)...the second transaction will try
						//---			to do the same.
						if( existingCode.getRawCodeLength() != 0 || newCode.getRawCodeLength() != 0 )
						{
							//--- DRK > A fringe case that gets through to here is:
							//---		(1) User hovers over cell.
							//---		(2) While the request is out for splash code we snap to the cell.
							//---		(3) We now have two requests out, one for splash, one for compiled.
							//---		(4) splash comes back, with stand-in compiled, then compiled, with stand-in splash.
							
							boolean isOverlap = existingCode.isStandInFor(bhE_CodeType.SPLASH) &&
												existingCode.isStandInFor(bhE_CodeType.COMPILED) &&
												newCode.isStandInFor(bhE_CodeType.SPLASH) &&
												newCode.isStandInFor(bhE_CodeType.COMPILED);
							
							// Fringe case can trip this assert...
							bhU_Debug.ASSERT(isOverlap, "Expected code for type " + eType + " to be null in setInitialCode.");
							
							return false;
						}
					}
				}
			}
		}
		else
		{
			bhU_Debug.ASSERT(false, "Expected non-null cell in setInitialCode");
		}
		
		return true;
	}
	
	public boolean isEditable(bhGridCoordinate coord)
	{
		bhUserCell cell = getCellLenient(coord);

		if( cell != null )
		{			
			bhCode code = cell.getCode(bhE_CodeType.SOURCE);
			
			//--- DRK > Not a huge fan of this method of seeing if a cell is editable, 
			//---		but we'll see if it remains "good enough".
			//---		Null code means we have never been able to retrieve the source for this cell yet.
			//---		It's a rule that we need the current source saved on server before being able to edit it.
			if( code != null )
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isSourceCodeChanged(bhGridCoordinate coord)
	{
		bhUserCell cell = getCellStrict(coord);

		return cell.m_hasChangedCode;
	}
	
	public void onSourceCodeChanged(bhGridCoordinate coord, String sourceCode)
	{
		bhUserCell cell = getCellStrict(coord);

		cell.setChangedCode(sourceCode);
	}
	
	public void onSyncResponseError(bhGridCoordinate coord)
	{
		bhUserCell cell = getCellStrict(coord);

		if( cell != null )
		{
			cell.onSyncResponseError();
		}
	}
	
	public void onSyncSuccess(bhGridCoordinate coord, bhCode splashScreenCode, bhCode compiledCode)
	{
		bhUserCell cell = getCellStrict(coord);

		if( cell != null )
		{
			cell.onSyncResponseSuccess(splashScreenCode, compiledCode);
		}
	}
	
	public void onSyncStart(bhGridCoordinate coord, bhCode compiledCode)
	{
		bhUserCell cell = getCellStrict(coord);

		if( cell != null )
		{
			cell.onSyncStart(compiledCode);
		}
	}
	
	public bhUserCell getCell(bhGridCoordinate coord)
	{
		return getCellLenient(coord);
	}
	
	protected bhUserCell getCellLenient(bhGridCoordinate coord)
	{
		for( int i = 0; i < m_cells.size(); i++ )
		{
			bhUserCell cell = m_cells.get(i);
			
			if( cell.getCoordinate().isEqualTo(coord) )
			{
				return cell;
			}
		}
		
		return null;
	}

	private bhUserCell getCellStrict(bhGridCoordinate coord)
	{
		bhUserCell cell = getCellLenient(coord);

		bhU_Debug.ASSERT(cell != null, "Expected user cell to not be null in getCell.");
		
		return cell;
	}
	
	@Override
	public bhPoint getLastPosition()
	{
		return m_lastPosition;
	}

	public boolean isPopulated()
	{
		return m_isPopulated;
	}
	
	@Override
	public void readJson(bhI_JsonObject json)
	{
		m_cells.clear();
		
		super.readJson(json);
		
		m_isPopulated = true;
	}
	
	public void clearAllLocalChanges()
	{
		for( int i = 0; i < m_cells.size(); i++ )
		{
			bhUserCell cell = m_cells.get(i);
			
			cell.setChangedCode(null);
		}
	}

	public void onSignOut()
	{
		m_cells.clear();
		
		m_isPopulated = false;
	}
	
	public Iterator<? extends bhUserCell> getCells()
	{
		return new bhCellIterator(m_cells);
	}

	@Override
	protected void justReadMappingFromJson(bhCellAddressMapping mapping)
	{
		bhUserCell newCell = new bhUserCell(mapping.getCoordinate());
		m_cells.add(newCell);
	}

	@Override
	public boolean isCellOwner(bhGridCoordinate coordinate)
	{
		return this.getCellLenient(coordinate) != null;
	}
	
	@Override
	public void writeJson(bhI_JsonObject json)
	{
		throw new RuntimeException("Client user should never write json.");
	}
}