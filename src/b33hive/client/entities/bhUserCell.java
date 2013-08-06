package b33hive.client.entities;

import b33hive.shared.entities.bhA_Cell;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.structs.bhCellAddress;
import b33hive.shared.structs.bhCode;
import b33hive.shared.structs.bhGridCoordinate;
import b33hive.shared.structs.bhMutableCode;

public class bhUserCell extends bhA_Cell
{
	final bhMutableCode m_changedCode = new bhMutableCode(bhE_CodeType.SOURCE);
	
	boolean m_hasChangedCode = false;
	
	private bhCellAddress m_address = null;
	
	public bhUserCell(bhGridCoordinate coordinate)
	{
		super(coordinate);
	}
	
	bhCellAddress getAddress()
	{
		return m_address;
	}
	
	public void setAddress(bhCellAddress address)
	{
		m_address = address;
	}
	
	bhCode getChangedCode()
	{
		return m_changedCode;
	}
	
	void setChangedCode(String rawCode)
	{
		m_changedCode.setRawCode(rawCode);
		
		if( m_changedCode.getRawCode() == null )
		{
			m_hasChangedCode = false;
		}
		else
		{
			m_hasChangedCode = true;
		}
	}
	
	@Override
	public void setCode(bhE_CodeType type, bhCode code_nullable)
	{
		super.setCode(type, code_nullable);
	}
	
	void onSyncStart(bhCode compiledCode)
	{
		bhCode sourceCode = new bhCode(this.getChangedCode().getRawCode(), bhE_CodeType.SOURCE);
		
		this.setCode(bhE_CodeType.SOURCE, sourceCode);
		this.setCode(bhE_CodeType.COMPILED, compiledCode);

		this.setChangedCode(null);
	}
	
	void onSyncResponseSuccess(bhCode spashScreenCode, bhCode compiledCode)
	{		
		this.setCode(bhE_CodeType.SPLASH, spashScreenCode);
		this.setCode(bhE_CodeType.COMPILED, compiledCode);
	}
	
	void onSyncResponseError()
	{
		this.setChangedCode(this.getCode(bhE_CodeType.SOURCE).getRawCode());
	}
}
