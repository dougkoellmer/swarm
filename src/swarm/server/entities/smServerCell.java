package swarm.server.entities;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import swarm.server.data.blob.smE_BlobCacheLevel;
import swarm.server.data.blob.smI_Blob;
import swarm.server.data.blob.smU_Serialization;
import swarm.server.structs.smDate;
import swarm.server.structs.smServerCellAddress;
import swarm.server.structs.smServerCodePrivileges;
import swarm.server.structs.smServerCode;
import swarm.shared.entities.smA_Cell;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.structs.smCodePrivileges;
import swarm.shared.structs.smCode;


/**
 * ...
 * @author
 */
public class smServerCell extends smA_Cell implements smI_Blob
{
	private static final int STARTING_CODE_VERSION = 0;
	
	private static final int EXTERNAL_VERSION = 1;
	
	private smServerCellAddress m_address;

	private final smDate m_lastUpdated = new smDate();
	
	private int m_codeVersion = STARTING_CODE_VERSION;
	
	public smServerCell()
	{
		super(new smServerCodePrivileges());
		
		m_address = new smServerCellAddress();
	}
	
	public smServerCell(smServerCellAddress address)
	{
		super(new smServerCodePrivileges());
	
		m_address = address;
	}
	
	public smServerCell(smServerCellAddress address, smServerCodePrivileges privileges)
	{
		super(privileges);
		
		m_address = address;
	}
	
	@Override
	public void setCode(smE_CodeType eType, smCode code_nullable)
	{
		super.setCode(eType, code_nullable);
		
		if( eType == smE_CodeType.SOURCE )
		{
			m_codeVersion++;
		}
	}
	
	public smServerCellAddress getAddress()
	{
		return m_address;
	}
	
	public void setAddress(smServerCellAddress address)
	{
		m_address = address;
	}
	
	public smServerCode getServerCode(smE_CodeType eType)
	{
		return (smServerCode) this.getCode(eType);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		bhU_Serialization.writeNullableObject(this.getServerCode(smE_CodeType.SOURCE), out);
		bhU_Serialization.writeNullableObject(this.getServerCode(smE_CodeType.SPLASH), out);
		bhU_Serialization.writeNullableObject(this.getServerCode(smE_CodeType.COMPILED), out);

		m_address.writeExternal(out);
		
		m_lastUpdated.writeExternal(out);
		
		out.writeInt(m_codeVersion);
		
		((smServerCodePrivileges)m_codePrivileges).writeExternal(out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();

		smServerCode code = null;
		
		code = bhU_Serialization.readNullableObject(smServerCode.class, in);
		this.setCode(smE_CodeType.SOURCE, code);
		
		code = bhU_Serialization.readNullableObject(smServerCode.class, in);
		this.setCode(smE_CodeType.SPLASH, code);
		
		code = bhU_Serialization.readNullableObject(smServerCode.class, in);
		this.setCode(smE_CodeType.COMPILED, code);

		m_address.readExternal(in);
		
		m_lastUpdated.readExternal(in);
		
		m_codeVersion = in.readInt();
		
		((smServerCodePrivileges)m_codePrivileges).readExternal(in);
	}

	@Override
	public String getKind()
	{
		return "sm_cell";
	}

	@Override
	public smE_BlobCacheLevel getMaximumCacheLevel()
	{
		return smE_BlobCacheLevel.LOCAL;
	}

	@Override
	public Map<String, Object> getQueryableProperties()
	{
		return null;
	}
}