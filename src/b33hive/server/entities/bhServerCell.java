package b33hive.server.entities;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import b33hive.server.data.blob.bhE_BlobCacheLevel;
import b33hive.server.data.blob.bhI_Blob;
import b33hive.server.data.blob.bhU_Serialization;
import b33hive.server.structs.bhDate;
import b33hive.server.structs.bhServerCellAddress;
import b33hive.server.structs.bhServerCodePrivileges;
import b33hive.server.structs.bhServerCode;
import b33hive.shared.entities.bhA_Cell;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.structs.bhCodePrivileges;
import b33hive.shared.structs.bhCode;


/**
 * ...
 * @author
 */
public class bhServerCell extends bhA_Cell implements bhI_Blob
{
	private static final int STARTING_CODE_VERSION = 0;
	
	private static final int EXTERNAL_VERSION = 1;
	
	private bhServerCellAddress m_address;

	private final bhDate m_lastUpdated = new bhDate();
	
	private int m_codeVersion = STARTING_CODE_VERSION;
	
	public bhServerCell()
	{
		super(new bhServerCodePrivileges());
		
		m_address = new bhServerCellAddress();
	}
	
	public bhServerCell(bhServerCellAddress address)
	{
		super(new bhServerCodePrivileges());
	
		m_address = address;
	}
	
	public bhServerCell(bhServerCellAddress address, bhServerCodePrivileges privileges)
	{
		super(privileges);
		
		m_address = address;
	}
	
	@Override
	public void setCode(bhE_CodeType eType, bhCode code_nullable)
	{
		super.setCode(eType, code_nullable);
		
		if( eType == bhE_CodeType.SOURCE )
		{
			m_codeVersion++;
		}
	}
	
	public bhServerCellAddress getAddress()
	{
		return m_address;
	}
	
	public void setAddress(bhServerCellAddress address)
	{
		m_address = address;
	}
	
	public bhServerCode getServerCode(bhE_CodeType eType)
	{
		return (bhServerCode) this.getCode(eType);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		bhU_Serialization.writeNullableObject(this.getServerCode(bhE_CodeType.SOURCE), out);
		bhU_Serialization.writeNullableObject(this.getServerCode(bhE_CodeType.SPLASH), out);
		bhU_Serialization.writeNullableObject(this.getServerCode(bhE_CodeType.COMPILED), out);

		m_address.writeExternal(out);
		
		m_lastUpdated.writeExternal(out);
		
		out.writeInt(m_codeVersion);
		
		((bhServerCodePrivileges)m_codePrivileges).writeExternal(out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();

		bhServerCode code = null;
		
		code = bhU_Serialization.readNullableObject(bhServerCode.class, in);
		this.setCode(bhE_CodeType.SOURCE, code);
		
		code = bhU_Serialization.readNullableObject(bhServerCode.class, in);
		this.setCode(bhE_CodeType.SPLASH, code);
		
		code = bhU_Serialization.readNullableObject(bhServerCode.class, in);
		this.setCode(bhE_CodeType.COMPILED, code);

		m_address.readExternal(in);
		
		m_lastUpdated.readExternal(in);
		
		m_codeVersion = in.readInt();
		
		((bhServerCodePrivileges)m_codePrivileges).readExternal(in);
	}

	@Override
	public String getKind()
	{
		return bhS_BlobKeyPrefix.CELL_PREFIX;
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