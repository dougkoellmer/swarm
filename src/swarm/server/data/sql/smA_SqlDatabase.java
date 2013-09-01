package swarm.server.data.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.account.smAccountDatabase;
import swarm.server.transaction.smI_TransactionScopeListener;

public class smA_SqlDatabase implements smI_TransactionScopeListener
{
	private final static Logger s_logger = Logger.getLogger(smA_SqlDatabase.class.getName());
	
	private final String m_databaseUrl;
	private final String m_databaseName;
	
	private final ThreadLocal<Connection> m_threadLocalConnection = new ThreadLocal<Connection>();
	
	protected smA_SqlDatabase(String databaseUrl, String databaseName)
	{
		m_databaseUrl = databaseUrl + databaseName; // for some reason connection fails without a database name on end of url.
		m_databaseName = databaseName;
	}
	
	public String getDatabaseName()
	{
		return m_databaseName;
	}
	
	protected Connection getConnection() throws SQLException
	{
		Connection connection = m_threadLocalConnection.get();
		
		if( connection != null )
		{
			try
			{
				if( connection.isClosed() )
				{
					connection = null;
				}
			}
			catch(SQLException e)
			{
				connection = null;
				
				s_logger.log(Level.SEVERE, "Tried to see if sql connection was closed.", e);
			}
			
			if( connection != null )
			{
				return connection;
			}
		}
		
		connection = DriverManager.getConnection(m_databaseUrl);
		
		m_threadLocalConnection.set(connection);
		
		return connection;
	}

	private void closeConnection()
	{
		Connection connection = m_threadLocalConnection.get();
		
		if ( connection != null)
		{
			try
			{
				if( connection.isClosed() )
				{
					s_logger.severe("Did not expect sql connection to already be closed.");
					m_threadLocalConnection.remove();
				}
			}
			catch (SQLException e)
			{
				s_logger.log(Level.SEVERE, "Tried to see if sql connection was closed.", e);
			}
			
			try
			{
				connection.close();
				m_threadLocalConnection.remove();
			}
			catch (SQLException ignore)
			{
				s_logger.log(Level.SEVERE, "Could not close connection.", ignore);
				
				try
				{
					if( connection.isClosed() )
					{
						m_threadLocalConnection.remove();
					}
				}
				catch (SQLException e)
				{
					s_logger.log(Level.SEVERE, "Tried to see if sql connection was closed.", e);
				}
			}
		}
	}

	@Override
	public void onEnterScope()
	{
		closeConnection();
	}

	@Override
	public void onBatchStart()
	{
		closeConnection();
	}

	@Override
	public void onBatchEnd()
	{
		closeConnection();
	}

	@Override
	public void onExitScope()
	{
		closeConnection();
	}
}