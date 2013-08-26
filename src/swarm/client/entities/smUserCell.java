package swarm.client.entities;

import swarm.shared.entities.smA_Cell;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smMutableCode;

public class smUserCell extends smA_Cell
{
	final smMutableCode m_changedCode = new smMutableCode(smE_CodeType.SOURCE);
	
	boolean m_hasChangedCode = false;
	
	private smCellAddress m_address = null;
	
	public smUserCell(smGridCoordinate coordinate)
	{
		super(coordinate);
	}
	
	smCellAddress getAddress()
	{
		return m_address;
	}
	
	public void setAddress(smCellAddress address)
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
	public void setCode(smE_CodeType type, smCode code_nullable)
	{
		super.setCode(type, code_nullable);
	}
	
	void onSyncStart(smCode compiledCode)
	{
		bhCode sourceCode = new smCode(this.getChangedCode().getRawCode(), smE_CodeType.SOURCE);
		
		this.setCode(smE_CodeType.SOURCE, sourceCode);
		this.setCode(smE_CodeType.COMPILED, compiledCode);

		this.setChangedCode(null);
	}
	
	void onSyncResponseSuccess(smCode spashScreenCode, smCode compiledCode)
	{		
		this.setCode(smE_CodeType.SPLASH, spashScreenCode);
		this.setCode(smE_CodeType.COMPILED, compiledCode);
	}
	
	void onSyncResponseError()
	{
		this.setChangedCode(this.getCode(smE_CodeType.SOURCE).getRawCode());
	}
}
