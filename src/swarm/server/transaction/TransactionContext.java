package swarm.server.transaction;

import java.util.ArrayList;
import java.util.HashMap;

import swarm.shared.structs.OptHashMap;
import swarm.shared.transaction.I_RequestPath;
import swarm.shared.transaction.TransactionRequest;
import swarm.shared.transaction.TransactionResponse;

public class TransactionContext
{
	private final class PathContext
	{
		private int m_requestCount;
		private Object m_userData;
	}
	
	private final boolean m_isInBatch;
	
	private final HashMap<Integer, PathContext> m_pathContexts;
	
	private TransactionBatch m_deferredBatch;
	
	private final TransactionBatch m_batch;
	
	private final Object m_nativeContext;
	
	TransactionContext(boolean isInBatch, Object nativeContext)
	{
		m_isInBatch = isInBatch;
		
		m_batch = new TransactionBatch();
		
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
	
	TransactionBatch getBatch()
	{
		return m_batch;
	}
	
	boolean isInBatch()
	{
		return m_isInBatch;
	}
	
	void addTransaction(TransactionRequest request, TransactionResponse response)
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
	
	public void setUserData(I_RequestPath path, Object userData)
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
	
	public Object getUserData(I_RequestPath path)
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
	
	private PathContext getPathContext(I_RequestPath path, boolean forceCreate)
	{
		PathContext pathContext = m_pathContexts.get(path.getId());
		
		if( pathContext == null && forceCreate )
		{
			pathContext = new PathContext();
			m_pathContexts.put(path.getId(), pathContext);
		}
		
		return pathContext;
	}
	
	public int getRequestCount(I_RequestPath path)
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
	
	TransactionBatch getDeferredBatch()
	{
		return m_deferredBatch;
	}
	
	int getDeferredCount()
	{
		return m_deferredBatch != null ? m_deferredBatch.getCount() : 0;
	}
	
	void queueDeferredTransaction(TransactionRequest request, TransactionResponse response)
	{
		m_deferredBatch = m_deferredBatch != null ? m_deferredBatch : new TransactionBatch();
		
		m_deferredBatch.add(request, response);
	}
}
