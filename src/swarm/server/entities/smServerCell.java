package swarm.server.entities;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
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
	
	private static final int EXTERNAL_VERSION = 2;
	
	private final ArrayList<smServerCellAddress> m_addresses = new ArrayList<smServerCellAddress>();
	
	private final smDate m_lastUpdated = new smDate();
	
	private int m_codeVersion = STARTING_CODE_VERSION;
	
	public smServerCell()
	{
		super(new smServerCodePrivileges());
	}
	
	public smServerCell(smServerCellAddress ... addresses)
	{
		this(new smServerCodePrivileges(), addresses);
	}
	
	public smServerCell(smServerCodePrivileges privileges, smServerCellAddress ... addresses)
	{
		super(privileges);

		for( int i = 0; i < addresses.length; i++ )
		{
			m_addresses.add(addresses[i]);
		}
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
	
	public Iterator<smServerCellAddress> getAddresses()
	{
		return m_addresses.iterator();
	}
	
	public smServerCellAddress getPrimaryAddress()
	{
		return m_addresses.size() > 0 ? m_addresses.get(0) : null;
	}
	
	public void setAddresses(smServerCellAddress ... addresses)
	{
		m_addresses.clear();
		
		for( int i = 0; i < addresses.length; i++ )
		{
			m_addresses.add(addresses[i]);
		}
		
	}
	
	/*public void setAddress(smServerCellAddress address)
	{
		m_address = address;
	}*/
	
	public smServerCode getServerCode(smE_CodeType eType)
	{
		return (smServerCode) this.getCode(eType);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		smU_Serialization.writeNullableObject(this.getServerCode(smE_CodeType.SOURCE), out);
		smU_Serialization.writeNullableObject(this.getServerCode(smE_CodeType.SPLASH), out);
		smU_Serialization.writeNullableObject(this.getServerCode(smE_CodeType.COMPILED), out);

		smU_Serialization.writeArrayList(m_addresses, out);
		
		m_lastUpdated.writeExternal(out);
		
		out.writeInt(m_codeVersion);
		
		((smServerCodePrivileges)m_codePrivileges).writeExternal(out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();

		smServerCode code = null;
		
		code = smU_Serialization.readNullableObject(smServerCode.class, in);
		this.setCode(smE_CodeType.SOURCE, code);
		
		code = smU_Serialization.readNullableObject(smServerCode.class, in);
		this.setCode(smE_CodeType.SPLASH, code);
		
		code = smU_Serialization.readNullableObject(smServerCode.class, in);
		this.setCode(smE_CodeType.COMPILED, code);

		if( externalVersion == 1 )
		{
			smServerCellAddress primaryAddress = new smServerCellAddress();
			primaryAddress.readExternal(in);
			m_addresses.add(primaryAddress);
		}
		else if( externalVersion > 1 )
		{
			smU_Serialization.readArrayList(m_addresses, smServerCellAddress.class, in);
		}
		
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