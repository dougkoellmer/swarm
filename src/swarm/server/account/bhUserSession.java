package swarm.server.account;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import swarm.server.data.blob.bhE_BlobCacheLevel;
import swarm.server.data.blob.bhI_Blob;
import swarm.server.data.blob.bhI_BlobKey;
import swarm.server.data.blob.bhU_Blob;
import swarm.server.data.blob.bhU_Serialization;

import swarm.server.session.bhS_Session;
import swarm.server.structs.bhDate;

public class bhUserSession implements bhI_Blob, bhI_BlobKey
{
	private static final int EXTERNAL_VERSION = 1;
	
	private bhE_Role m_role 	= null;
	private int m_accountId		= -1;
	private String m_username	= null;
	
	private final bhDate m_lastTouched = new bhDate();
	
	public bhUserSession(int accountId, String username, bhE_Role role)
	{
		m_accountId = accountId;
		m_username = username;
		m_role = role;
	}
	
	public bhUserSession()
	{
	}
	
	public bhE_Role getRole()
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
		bhU_Serialization.writeNullableEnum(m_role, out);
		out.writeUTF(m_username);

		m_lastTouched.writeExternal(out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		m_accountId = in.readInt();
		m_role = bhU_Serialization.readNullableEnum(bhE_Role.values(), in);
		m_username = in.readUTF();

		m_lastTouched.readExternal(in);
	}

	@Override
	public String createBlobKey(bhI_Blob blob)
	{
		return bhU_Blob.generateKey(blob, getAccountIdString());
	}

	@Override
	public bhE_BlobCacheLevel getMaximumCacheLevel()
	{
		return bhE_BlobCacheLevel.LOCAL;
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
		values.put(bhS_Session.ACCOUNT_ID_PROPERTY, this.getAccountId());
		values.put(bhS_Session.DATE_PROPERTY, this.m_lastTouched.convertToMilliseconds());
		
		return values;
	}
}
