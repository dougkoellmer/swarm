package com.b33hive.server.transaction;

import java.util.ArrayList;
import java.util.HashMap;

import com.b33hive.shared.structs.bhOptHashMap;
import com.b33hive.shared.transaction.bhI_RequestPath;
import com.b33hive.shared.transaction.bhTransactionRequest;
import com.b33hive.shared.transaction.bhTransactionResponse;

public class bhTransactionContext
{
	private final class PathContext
	{
		private int m_requestCount;
		private Object m_userData;
	}
	
	private final boolean m_isInBatch;
	
	private final HashMap<Integer, PathContext> m_pathContexts;
	
	private bhTransactionBatch m_deferredBatch;
	
	private final bhTransactionBatch m_batch;
	
	private final Object m_nativeContext;
	
	bhTransactionContext(boolean isInBatch, Object nativeContext)
	{
		m_isInBatch = isInBatch;
		
		m_batch = new bhTransactionBatch();
		
		if( m_isInBatch )
		{
			m_pathContexts = new HashMap<Integer, PathContext>();
		}
		else
		{
			m_pathContexts = null;
		}
		
		m_nativeContext = nativeContext;
	}
	
	public Object getNativeContext()
	{
		return m_nativeContext;
	}
	
	bhTransactionBatch getBatch()
	{
		return m_batch;
	}
	
	boolean isInBatch()
	{
		return m_isInBatch;
	}
	
	void addTransaction(bhTransactionRequest request, bhTransactionResponse response)
	{
		m_batch.add(request, response);
		
		if( m_isInBatch )
		{
			if( request.getPath() != null )
			{
				getPathContext(request.getPath(), true).m_requestCount++;
			}
		}
	}
	
	public void setUserData(bhI_RequestPath path, Object userData)
	{
		if( this.getRequestCount(path) > 1 )
		{
			getPathContext(path, true).m_userData = userData;
		}
		else
		{
			throw new RuntimeException("Can't set userdata for the sole request to the path: " + path);
		}
	}
	
	public Object getUserData(bhI_RequestPath path)
	{
		if( this.getRequestCount(path) > 1 )
		{
			return getPathContext(path, true).m_userData;
		}
		else
		{
			throw new RuntimeException("Can't get userdata for the sole request to the path: " + path);
		}
	}
	
	private PathContext getPathContext(bhI_RequestPath path, boolean forceCreate)
	{
		PathContext pathContext = m_pathContexts.get(path.getId());
		
		if( pathContext == null && forceCreate )
		{
			pathContext = new PathContext();
			m_pathContexts.put(path.getId(), pathContext);
		}
		
		return pathContext;
	}
	
	public int getRequestCount(bhI_RequestPath path)
	{
		if( m_isInBatch )
		{
			PathContext pathContext = m_pathContexts.get(path.getId());
			if( pathContext != null)
			{
				return pathContext.m_requestCount;
			}
		}
		else
		{
			if( path == m_batch.getRequest(0).getPath() )
			{
				return 1;
			}
		}
		
		return 0;
	}
	
	bhTransactionBatch getDeferredBatch()
	{
		return m_deferredBatch;
	}
	
	int getDeferredCount()
	{
		return m_deferredBatch != null ? m_deferredBatch.getCount() : 0;
	}
	
	void queueDeferredTransaction(bhTransactionRequest request, bhTransactionResponse response)
	{
		m_deferredBatch = m_deferredBatch != null ? m_deferredBatch : new bhTransactionBatch();
		
		m_deferredBatch.add(request, response);
	}
}
