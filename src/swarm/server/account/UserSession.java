package swarm.server.account;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import swarm.server.data.blob.E_BlobCacheLevel;
import swarm.server.data.blob.I_Blob;
import swarm.server.data.blob.I_BlobKey;
import swarm.server.data.blob.U_Blob;
import swarm.server.data.blob.U_Serialization;

import swarm.server.session.S_Session;
import swarm.server.structs.SerializableDate;

public class UserSession implements I_Blob, I_BlobKey
{
	private static final int EXTERNAL_VERSION = 1;
	
	private E_Role m_role 	= null;
	private int m_accountId		= -1;
	private String m_username	= null;
	
	private final SerializableDate m_lastTouched = new SerializableDate();
	
	public UserSession(int accountId, String username, E_Role role)
	{
		m_accountId = accountId;
		m_username = username;
		m_role = role;
	}
	
	public UserSession()
	{
	}
	
	public E_Role getRole()
	{
		return m_role;
	}
	
	public int getAccountId()
	{
		return m_accountId;
	}
	
	public String getAccountIdString()
	{
		return m_accountId + "";
	}
	
	public String getUsername()
	{
		return m_username;
	}
	
	public boolean isExpired(long expirationInSeconds)
	{
		Date currentDate = new Date();
		long currentDateInSeconds = currentDate.getTime() / 1000;
		if( (currentDateInSeconds - m_lastTouched.convertToSeconds()) > expirationInSeconds )
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		out.writeInt(m_accountId);
		U_Serialization.writeNullableEnum(m_role, out);
		out.writeUTF(m_username);

		m_lastTouched.writeExternal(out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		m_accountId = in.readInt();
		m_role = U_Serialization.readNullableEnum(E_Role.values(), in);
		m_username = in.readUTF();

		m_lastTouched.readExternal(in);
	}

	@Override
	public String createBlobKey(I_Blob blob)
	{
		return U_Blob.generateKey(blob, getAccountIdString());
	}

	@Override
	public E_BlobCacheLevel getMaximumCacheLevel()
	{
		return E_BlobCacheLevel.LOCAL;
	}

	@Override
	public String getKind()
	{
		return "sm_sesh";
	}
	
	@Override
	public Map<String, Object> getQueryableProperties()
	{
		HashMap<String, Object> values = new HashMap<String, Object>();
		values.put(S_Session.ACCOUNT_ID_PROPERTY, this.getAccountId());
		values.put(S_Session.DATE_PROPERTY, this.m_lastTouched.convertToMilliseconds());
		
		return values;
	}
}
