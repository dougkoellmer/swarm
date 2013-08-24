package swarm.server.telemetry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.account.bhS_AccountQuery;
import swarm.server.data.sql.bhA_SqlDatabase;
import swarm.server.data.sql.bhI_SqlEncodable;
import swarm.shared.structs.bhTuple;
import com.google.protos.cloud.sql.Client.SqlException;

public class bhTelemetryDatabase extends bhA_SqlDatabase
{
	private final static Logger s_logger = Logger.getLogger(bhTelemetryDatabase.class.getName());
	
	public bhTelemetryDatabase(String databaseUrl, String databaseName)
	{
		super(databaseUrl, databaseName);
	}
	
	public void put(bhI_SqlEncodable sqlEncodable)
	{
		try
		{
			Connection connection = this.getConnection();
			
			String query = "INSERT INTO "+getDatabaseName()+"."+sqlEncodable.getTable();
			String columnPart = " (";
			String valuePart = " VALUES(";
			
			boolean first = true;
			ArrayList<Object> values = new ArrayList<Object>();
			for( bhTuple<String, Object> tuple = null; (tuple = sqlEncodable.nextColumn()) != null; )
			{
				if( !first )
				{
					columnPart += ",";
					valuePart += ",";
				}

				columnPart += tuple.getFirst();
				valuePart += "?";
				
				values.add(tuple.getSecond());
				
				first = false;
			}
			
			columnPart += ")";
			valuePart += ");";
			
			query += columnPart + valuePart;
			
			PreparedStatement statement = connection.prepareStatement(query);
			
			for( int i = 0; i < values.size(); i++ )
			{
				statement.setObject(i+1, values.get(i));
			}
			
			statement.executeUpdate();
			
		}
		catch(SQLException e)
		{
			s_logger.log(Level.SEVERE, "Could not log telemetry.", e);
		}
	}
}

