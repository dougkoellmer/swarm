package swarm.server.telemetry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import swarm.server.data.sql.A_SqlDatabase;
import swarm.server.data.sql.I_SqlEncodable;
import swarm.shared.structs.Tuple;
import com.google.protos.cloud.sql.Client.SqlException;

public class TelemetryDatabase extends A_SqlDatabase
{
	private final static Logger s_logger = Logger.getLogger(TelemetryDatabase.class.getName());
	
	public TelemetryDatabase(String databaseUrl, String databaseName)
	{
		super(databaseUrl, databaseName);
	}
	
	public void put(I_SqlEncodable sqlEncodable)
	{
		try
		{
			Connection connection = this.getConnection();
			
			String query = "INSERT INTO "+getDatabaseName()+"."+sqlEncodable.getTable();
			String columnPart = " (";
			String valuePart = " VALUES(";
			
			boolean first = true;
			ArrayList<Object> values = new ArrayList<Object>();
			for( Tuple<String, Object> tuple = null; (tuple = sqlEncodable.nextColumn()) != null; )
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

