package swarm.client.entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import swarm.client.managers.CellCodeManager;
import swarm.client.structs.I_LocalCodeRepository;
import swarm.shared.app.S_CommonApp;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.A_Cell;
import swarm.shared.entities.E_CodeSafetyLevel;
import swarm.shared.entities.E_CodeType;
import swarm.shared.entities.A_User;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.CellAddressMapping;
import swarm.shared.structs.Code;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.Point;


/**
 * ...
 * @author 
 */
public class A_ClientUser extends A_User implements I_LocalCodeRepository
{
	private static final Logger s_logger = Logger.getLogger(CellCodeManager.class.getName());
	
	private class smCellIterator implements Iterator<UserCell>
	{
		private ArrayList<UserCell> m_cells = null;
		private int m_currentIndex = 0;
		
		private smCellIterator(ArrayList<UserCell> cells)
		{
			m_cells = cells;
		}
		
		@Override
		public boolean hasNext()
		{
			return m_currentIndex < m_cells.size();
		}

		@Override
		public UserCell next()
		{
			UserCell next = m_cells.get(m_currentIndex);
			m_currentIndex++;
			return next;
		}

		@Override
		public void remove()
		{
			U_Debug.ASSERT(false, "Can't remove.");
		}
	}
	
	private final Point m_lastPosition = new Point();
	
	private final ArrayList<UserCell> m_cells = new ArrayList<UserCell>();
	
	private boolean m_isPopulated;
	
	public A_ClientUser()
	{
		
	}
	
	public Code getCode(GridCoordinate coord, E_CodeType eType)
	{
		UserCell cell = getCellStrict(coord);
		
		if( cell != null )
		{
			if( cell.m_hasChangedCode && eType == E_CodeType.SOURCE )
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
	
	public CellAddress getCellAddress(CellAddressMapping mapping)
	{
		for( int i = 0; i < m_cells.size(); i++ )
		{
			UserCell cell = m_cells.get(i);

			GridCoordinate coordinate = cell.getCoordinate();
			
			if( coordinate.isEqualTo(mapping.getCoordinate()) )
			{
				CellAddress ithAddress = cell.getAddress();

				return ithAddress;
			}
		}
		
		return null;
	}
	
	public CellAddressMapping getCellAddressMapping(CellAddress address)
	{
		for( int i = 0; i < m_cells.size(); i++ )
		{
			UserCell cell = m_cells.get(i);

			CellAddress ithAddress = cell.getAddress();
			
			if( ithAddress != null )
			{
				if( ithAddress.isEqualTo(address) )
				{
					return new CellAddressMapping(cell.getCoordinate());
				}
			}
		}
		
		return null;
	}
	
	@Override
	public boolean tryPopulatingCell(GridCoordinate coord, E_CodeType eType, A_Cell outCell)
	{
		UserCell thisCell = getCellLenient(coord);
		
		if( thisCell != null )
		{
			Code toReturn = null;
			
			if( thisCell.m_hasChangedCode && eType == E_CodeType.SOURCE )
			{
				toReturn = thisCell.m_changedCode;
			}
			else
			{
				Code code = thisCell.getCode(eType);
				
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
				if( eType == E_CodeType.SOURCE || toReturn.getSafetyLevel() == E_CodeSafetyLevel.VIRTUAL_DYNAMIC_SANDBOX )
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
	public boolean setInitialCellData(GridCoordinate coord, A_Cell newCellData)
	{
		UserCell thisCell = getCellStrict(coord);
		
		if( thisCell != null )
		{
			if( newCellData.getCodePrivileges() != null )
			{
				thisCell.getCodePrivileges().copy(newCellData.getCodePrivileges());
			}
			
			for( int i = 0; i < E_CodeType.values().length; i++ )
			{
				E_CodeType eType = E_CodeType.values()[i];
				Code newCode = newCellData.getCode(eType);
				
				if( newCode != null )
				{
					Code existingCode = thisCell.getCode(eType);
					
					if( existingCode == null )
					{
						thisCell.setCode(eType, newCode);
					}
					else
					{
						//--- DRK > Future fringe case could trip this, if compiled can stand in for source,
						//---		or vice versa...for now, they can't.
						boolean isSource = eType == E_CodeType.SOURCE;
						if( isSource )
						{
							U_Debug.ASSERT(false, "Didn't expect source in setInitialData");
							
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
							
							boolean isOverlap = existingCode.isStandInFor(E_CodeType.SPLASH) &&
												existingCode.isStandInFor(E_CodeType.COMPILED) &&
												newCode.isStandInFor(E_CodeType.SPLASH) &&
												newCode.isStandInFor(E_CodeType.COMPILED);
							
							// Fringe case can trip this assert...
							U_Debug.ASSERT(isOverlap, "Expected code for type " + eType + " to be null in setInitialCode.");
							
							return false;
						}
					}
				}
			}
		}
		else
		{
			U_Debug.ASSERT(false, "Expected non-null cell in setInitialCode");
		}
		
		return true;
	}
	
	public boolean isEditable(GridCoordinate coord)
	{
		UserCell cell = getCellLenient(coord);

		if( cell != null )
		{			
			Code code = cell.getCode(E_CodeType.SOURCE);
			
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
	
	public boolean isSourceCodeChanged(GridCoordinate coord)
	{
		UserCell cell = getCellStrict(coord);

		return cell.m_hasChangedCode;
	}
	
	public void onSourceCodeChanged(GridCoordinate coord, String sourceCode)
	{
		UserCell cell = getCellStrict(coord);

		cell.setChangedCode(sourceCode);
	}
	
	public void onSyncResponseError(GridCoordinate coord)
	{
		UserCell cell = getCellStrict(coord);

		if( cell != null )
		{
			cell.onSyncResponseError();
		}
	}
	
	public void onSyncSuccess(GridCoordinate coord, Code splashScreenCode, Code compiledCode)
	{
		UserCell cell = getCellStrict(coord);

		if( cell != null )
		{
			cell.onSyncResponseSuccess(splashScreenCode, compiledCode);
		}
	}
	
	public void onSyncStart(GridCoordinate coord, Code compiledCode)
	{
		UserCell cell = getCellStrict(coord);

		if( cell != null )
		{
			cell.onSyncStart(compiledCode);
		}
	}
	
	public UserCell getCell(GridCoordinate coord)
	{
		return getCellLenient(coord);
	}
	
	protected UserCell getCellLenient(GridCoordinate coord)
	{
		for( int i = 0; i < m_cells.size(); i++ )
		{
			UserCell cell = m_cells.get(i);
			
			if( cell.getCoordinate().isEqualTo(coord) )
			{
				return cell;
			}
		}
		
		return null;
	}

	private UserCell getCellStrict(GridCoordinate coord)
	{
		UserCell cell = getCellLenient(coord);

		U_Debug.ASSERT(cell != null, "Expected user cell to not be null in getCell.");
		
		return cell;
	}
	
	@Override
	public Point getLastPosition()
	{
		return m_lastPosition;
	}

	public boolean isPopulated()
	{
		return m_isPopulated;
	}
	
	@Override
	public void readJson(A_JsonFactory factory, I_JsonObject json)
	{
		m_cells.clear();
		
		super.readJson(factory, json);
		
		m_isPopulated = true;
	}
	
	public void clearAllLocalChanges()
	{
		for( int i = 0; i < m_cells.size(); i++ )
		{
			UserCell cell = m_cells.get(i);
			
			cell.setChangedCode(null);
		}
	}

	public void onSignOut()
	{
		m_cells.clear();
		
		m_isPopulated = false;
	}
	
	public Iterator<? extends UserCell> getCells()
	{
		return new smCellIterator(m_cells);
	}

	@Override
	protected void justReadMappingFromJson(CellAddressMapping mapping)
	{
		UserCell newCell = new UserCell(mapping.getCoordinate());
		m_cells.add(newCell);
	}

	@Override
	public boolean isCellOwner(GridCoordinate coordinate)
	{
		return this.getCellLenient(coordinate) != null;
	}
	
	@Override
	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
	{
		throw new RuntimeException("Client user should never write json.");
	}
}