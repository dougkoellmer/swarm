package com.b33hive.server.structs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

public class bhDate implements Externalizable
{
	private static final int EXTERNAL_VERSION = 1;
	
	private long m_date = 0;
	
	private void update()
	{
		Date date = new Date();
		m_date = date.getTime();
	}
	
	public long convertToMilliseconds()
	{
		return m_date;
	}
	
	public long convertToSeconds()
	{
		return m_date / 1000;
	}
	
	public Date convertToNative()
	{
		return new Date(m_date);
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(EXTERNAL_VERSION);
		
		update();
	
		out.writeLong(m_date);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		int externalVersion = in.readInt();
		
		m_date = in.readLong();
	}
}
