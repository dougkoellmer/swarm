package swarm.client.entities;

import swarm.shared.entities.A_Cell;
import swarm.shared.entities.E_CodeType;
import swarm.shared.structs.CellAddress;
import swarm.shared.structs.Code;
import swarm.shared.structs.GridCoordinate;
import swarm.shared.structs.MutableCode;

public class UserCell extends A_Cell
{
	final MutableCode m_changedCode = new MutableCode(E_CodeType.SOURCE);
	
	boolean m_hasChangedCode = false;
	
	private CellAddress m_address = null;
	
	public UserCell(GridCoordinate coordinate)
	{
		super(coordinate);
	}
	
	CellAddress getAddress()
	{
		return m_address;
	}
	
	public void setAddress(CellAddress address)
	{
		m_address = address;
	}
	
	Code getChangedCode()
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
	public void setCode(E_CodeType type, Code code_nullable)
	{
		super.setCode(type, code_nullable);
	}
	
	void onSyncStart(Code compiledCode)
	{
		Code sourceCode = new Code(this.getChangedCode().getRawCode(), E_CodeType.SOURCE);
		
		this.setCode(E_CodeType.SOURCE, sourceCode);
		this.setCode(E_CodeType.COMPILED, compiledCode);

		this.setChangedCode(null);
	}
	
	void onSyncResponseSuccess(Code spashScreenCode, Code compiledCode)
	{		
		this.setCode(E_CodeType.SPLASH, spashScreenCode);
		this.setCode(E_CodeType.COMPILED, compiledCode);
	}
	
	void onSyncResponseError()
	{
		this.setChangedCode(this.getCode(E_CodeType.SOURCE).getRawCode());
	}
}
