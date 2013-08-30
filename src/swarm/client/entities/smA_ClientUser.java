package swarm.client.entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import swarm.client.managers.smCellCodeManager;
import swarm.client.structs.smI_LocalCodeRepository;
import swarm.shared.app.smS_App;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smA_Cell;
import swarm.shared.entities.smE_CodeSafetyLevel;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.entities.smA_User;
import swarm.shared.json.smA_JsonFactory;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCellAddressMapping;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;


/**
 * ...
 * @author 
 */
public class smA_ClientUser extends smA_User implements smI_LocalCodeRepository
{
	private static final Logger s_logger = Logger.getLogger(smCellCodeManager.class.getName());
	
	private class smCellIterator implements Iterator<smUserCell>
	{
		private ArrayList<smUserCell> m_cells = null;
		private int m_currentIndex = 0;
		
		private smCellIterator(ArrayList<smUserCell> cells)
		{
			m_cells = cells;
		}
		
		@Override
		public boolean hasNext()
		{
			return m_currentIndex < m_cells.size();
		}

		@Override
		public smUserCell next()
		{
			smUserCell next = m_cells.get(m_currentIndex);
			m_currentIndex++;
			return next;
		}

		@Override
		public void remove()
		{
			smU_Debug.ASSERT(false, "Can't remove.");
		}
	}
	
	private final smPoint m_lastPosition = new smPoint();
	
	private final ArrayList<smUserCell> m_cells = new ArrayList<smUserCell>();
	
	private boolean m_isPopulated;
	
	public smA_ClientUser()
	{
		
	}
	
	public smCode getCode(smGridCoordinate coord, smE_CodeType eType)
	{
		smUserCell cell = getCellStrict(coord);
		
		if( cell != null )
		{
			if( cell.m_hasChangedCode && eType == smE_CodeType.SOURCE )
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
	
	public smCellAddress getCellAddress(smCellAddressMapping mapping)
	{
		for( int i = 0; i < m_cells.size(); i++ )
		{
			smUserCell cell = m_cells.get(i);

			smGridCoordinate coordinate = cell.getCoordinate();
			
			if( coordinate.isEqualTo(mapping.getCoordinate()) )
			{
				smCellAddress ithAddress = cell.getAddress();

				return ithAddress;
			}
		}
		
		return null;
	}
	
	public smCellAddressMapping getCellAddressMapping(smCellAddress address)
	{
		for( int i = 0; i < m_cells.size(); i++ )
		{
			smUserCell cell = m_cells.get(i);

			smCellAddress ithAddress = cell.getAddress();
			
			if( ithAddress != null )
			{
				if( ithAddress.isEqualTo(address) )
				{
					return new smCellAddressMapping(cell.getCoordinate());
				}
			}
		}
		
		return null;
	}
	
	@Override
	public boolean tryPopulatingCell(smGridCoordinate coord, smE_CodeType eType, smA_Cell outCell)
	{
		smUserCell thisCell = getCellLenient(coord);
		
		if( thisCell != null )
		{
			smCode toReturn = null;
			
			if( thisCell.m_hasChangedCode && eType == smE_CodeType.SOURCE )
			{
				toReturn = thisCell.m_changedCode;
			}
			else
			{
				smCode code = thisCell.getCode(eType);
				
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
				if( eType == smE_CodeType.SOURCE || toReturn.getSafetyLevel() == smE_CodeSafetyLevel.REQUIRES_DYNAMIC_SANDBOX )
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
	public boolean setInitialCellData(smGridCoordinate coord, smA_Cell newCellData)
	{
		smUserCell thisCell = getCellStrict(coord);
		
		if( thisCell != null )
		{
			if( newCellData.getCodePrivileges() != null )
			{
				thisCell.getCodePrivileges().copy(newCellData.getCodePrivileges());
			}
			
			for( int i = 0; i < smE_CodeType.values().length; i++ )
			{
				smE_CodeType eType = smE_CodeType.values()[i];
				smCode newCode = newCellData.getCode(eType);
				
				if( newCode != null )
				{
					smCode existingCode = thisCell.getCode(eType);
					
					if( existingCode == null )
					{
						thisCell.setCode(eType, newCode);
					}
					else
					{
						//--- DRK > Future fringe case could trip this, if compiled can stand in for source,
						//---		or vice versa...for now, they can't.
						boolean isSource = eType == smE_CodeType.SOURCE;
						if( isSource )
						{
							smU_Debug.ASSERT(false, "Didn't expect source in setInitialData");
							
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
							
							boolean isOverlap = existingCode.isStandInFor(smE_CodeType.SPLASH) &&
												existingCode.isStandInFor(smE_CodeType.COMPILED) &&
												newCode.isStandInFor(smE_CodeType.SPLASH) &&
												newCode.isStandInFor(smE_CodeType.COMPILED);
							
							// Fringe case can trip this assert...
							smU_Debug.ASSERT(isOverlap, "Expected code for type " + eType + " to be null in setInitialCode.");
							
							return false;
						}
					}
				}
			}
		}
		else
		{
			smU_Debug.ASSERT(false, "Expected non-null cell in setInitialCode");
		}
		
		return true;
	}
	
	public boolean isEditable(smGridCoordinate coord)
	{
		smUserCell cell = getCellLenient(coord);

		if( cell != null )
		{			
			smCode code = cell.getCode(smE_CodeType.SOURCE);
			
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
	
	public boolean isSourceCodeChanged(smGridCoordinate coord)
	{
		smUserCell cell = getCellStrict(coord);

		return cell.m_hasChangedCode;
	}
	
	public void onSourceCodeChanged(smGridCoordinate coord, String sourceCode)
	{
		smUserCell cell = getCellStrict(coord);

		cell.setChangedCode(sourceCode);
	}
	
	public void onSyncResponseError(smGridCoordinate coord)
	{
		smUserCell cell = getCellStrict(coord);

		if( cell != null )
		{
			cell.onSyncResponseError();
		}
	}
	
	public void onSyncSuccess(smGridCoordinate coord, smCode splashScreenCode, smCode compiledCode)
	{
		smUserCell cell = getCellStrict(coord);

		if( cell != null )
		{
			cell.onSyncResponseSuccess(splashScreenCode, compiledCode);
		}
	}
	
	public void onSyncStart(smGridCoordinate coord, smCode compiledCode)
	{
		smUserCell cell = getCellStrict(coord);

		if( cell != null )
		{
			cell.onSyncStart(compiledCode);
		}
	}
	
	public smUserCell getCell(smGridCoordinate coord)
	{
		return getCellLenient(coord);
	}
	
	protected smUserCell getCellLenient(smGridCoordinate coord)
	{
		for( int i = 0; i < m_cells.size(); i++ )
		{
			smUserCell cell = m_cells.get(i);
			
			if( cell.getCoordinate().isEqualTo(coord) )
			{
				return cell;
			}
		}
		
		return null;
	}

	private smUserCell getCellStrict(smGridCoordinate coord)
	{
		smUserCell cell = getCellLenient(coord);

		smU_Debug.ASSERT(cell != null, "Expected user cell to not be null in getCell.");
		
		return cell;
	}
	
	@Override
	public smPoint getLastPosition()
	{
		return m_lastPosition;
	}

	public boolean isPopulated()
	{
		return m_isPopulated;
	}
	
	@Override
	public void readJson(smA_JsonFactory factory, smI_JsonObject json)
	{
		m_cells.clear();
		
		super.readJson(factory, json);
		
		m_isPopulated = true;
	}
	
	public void clearAllLocalChanges()
	{
		for( int i = 0; i < m_cells.size(); i++ )
		{
			smUserCell cell = m_cells.get(i);
			
			cell.setChangedCode(null);
		}
	}

	public void onSignOut()
	{
		m_cells.clear();
		
		m_isPopulated = false;
	}
	
	public Iterator<? extends smUserCell> getCells()
	{
		return new smCellIterator(m_cells);
	}

	@Override
	protected void justReadMappingFromJson(smCellAddressMapping mapping)
	{
		smUserCell newCell = new smUserCell(mapping.getCoordinate());
		m_cells.add(newCell);
	}

	@Override
	public boolean isCellOwner(smGridCoordinate coordinate)
	{
		return this.getCellLenient(coordinate) != null;
	}
	
	@Override
	public void writeJson(smA_JsonFactory factory, smI_JsonObject json_out)
	{
		throw new RuntimeException("Client user should never write json.");
	}
}