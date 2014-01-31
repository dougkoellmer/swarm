package swarm.server.entities;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.U_Serialization;
import swarm.server.structs.SerializableDate;
import swarm.server.structs.ServerCellAddress;
import swarm.server.structs.ServerCodePrivileges;
import swarm.server.structs.ServerCode;
import swarm.shared.entities.A_Cell;
import swarm.shared.entities.E_CodeType;
import swarm.shared.structs.CodePrivileges;
import swarm.shared.structs.Code;


/**
 * ...
 * @author
 */
public class ServerCell extends A_Cell implements I_Blob
{
	private static final int STARTING_CODE_VERSION = 0;
	
	private static final int EXTERNAL_VERSION = 2;
	
	private final ArrayList<ServerCellAddress> m_addresses = new ArrayList<ServerCellAddress>();
	
	private final SerializableDate m_lastUpdated = new SerializableDate();
	
	private int m_codeVersion = STARTING_CODE_VERSION;
	
	public ServerCell()
	{
		super(new ServerCodePrivileges());
	}
	
	public ServerCell(ServerCellAddress ... addresses)
	{
		this(new ServerCodePrivileges(), addresses);
	}
	
	public ServerCell(ServerCodePrivileges privileges, ServerCellAddress ... addresses)
	{
		super(privileges);

		for( int i = 0; i < addresses.length; i++ )
		{
			m_addresses.add(addresses[i]);
		}
	}
	
	@Override
	public void setCode(E_CodeType eType, Code code_nullable)
	{
		super.setCode(eType, code_nullable);
		
		if( eType == E_CodeType.SOURCE )
		{
			m_codeVersion++;
		}
	}
	
	public Iterator<ServerCellAddress> getAddresses()
	{
		return m_addresses.iterator();
	}
	
	public ServerCellAddress getPrimaryAddress()
	{
		return m_addresses.size() > 0 ? m_addresses.get(0) : null;
	}
	
	public void setAddresses(ServerCellAddress ... addresses)
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
	
	public ServerCode getServerCode(E_CodeType eType)
	{
		return (ServerCode) this.getCode(eType);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		U_Serialization.writeNullableObject(this.getServerCode(E_CodeType.SOURCE), out);
		U_Serialization.writeNullableObject(this.getServerCode(E_CodeType.SPLASH), out);
		U_Serialization.writeNullableObject(this.getServerCode(E_CodeType.COMPILED), out);

		U_Serialization.writeArrayList(m_addresses, out);
		
		m_lastUpdated.writeExternal(out);
		
		out.writeInt(m_codeVersion);
		
		((ServerCodePrivileges)m_codePrivileges).writeExternal(out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();

		ServerCode code = null;
		
		code = U_Serialization.readNullableObject(ServerCode.class, in);
		this.setCode(E_CodeType.SOURCE, code);
		
		code = U_Serialization.readNullableObject(ServerCode.class, in);
		this.setCode(E_CodeType.SPLASH, code);
		
		code = U_Serialization.readNullableObject(ServerCode.class, in);
		this.setCode(E_CodeType.COMPILED, code);

		if( externalVersion == 1 )
		{
			ServerCellAddress primaryAddress = new ServerCellAddress();
			primaryAddress.readExternal(in);
			m_addresses.add(primaryAddress);
		}
		else if( externalVersion > 1 )
		{
			U_Serialization.readArrayList(m_addresses, ServerCellAddress.class, in);
		}
		
		m_lastUpdated.readExternal(in);
		
		m_codeVersion = in.readInt();
		
		((ServerCodePrivileges)m_codePrivileges).readExternal(in);
	}

	@Override
	public String getKind()
	{
		return "sm_cell";
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