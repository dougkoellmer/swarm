package b33hive.server.telemetry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import b33hive.server.account.bhS_AccountQuery;
import b33hive.server.data.sql.bhA_SqlDatabase;
import b33hive.server.data.sql.bhI_SqlEncodable;
import b33hive.server.data.sql.bhS_Sql;
import b33hive.shared.structs.bhTuple;
import com.google.protos.cloud.sql.Client.SqlException;

public class bhTelemetryDatabase extends bhA_SqlDatabase
{
	private final static Logger s_logger = Logger.getLogger(bhTelemetryDatabase.class.getName());
	
	public bhTelemetryDatabase(String database)
	{
		super(database);
	}
	
	public void put(bhI_SqlEncodable sqlEncodable)
	{
		try
		{
			Connection connection = this.getConnection();
			
			String query = "INSERT INTO "+sqlEncodable.getTable();
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
