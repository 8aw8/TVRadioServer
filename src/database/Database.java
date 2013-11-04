package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import database.helpers.ResultSetEnumerationHandler;

public class Database
{
	private static final Map<String, Connection> DATABASES = new ConcurrentHashMap<String, Connection>();

	public static final Connection getDBConnection() 
	{
		Connection database = DATABASES.get(Thread.currentThread().getName());
		
	//	System.out.println("+++++++ DATABASES MAP SIZE:"+DATABASES.size()+"   "+DATABASES.toString());
	
		
		
		try 
		{
			if(database.isClosed())
				database = null;
		} 
		catch (Exception e1) {
			database = null;
		}
		
		if (database == null) 
		{
			try 
			{
				// юзер web для подключения отовсюду
			// пароль - DecommissioningGranddaughters		
				
				String uid 		=  "web"; //"radio";
				String pwd 		=  "DecommissioningGranddaughters"; //"BefriendsAutofill";
				String db_name 	= "radio";
				String url 		=  "jdbc:mysql://54.211.245.43/" + db_name;  //"jdbc:mysql://107.22.240.209/" + db_name;
		//		String url 		=  "jdbc:mysql://localhost/" + db_name;
				
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				
				Properties properties = new Properties();
				properties.setProperty("user",				uid);
				properties.setProperty("password",			pwd);
				properties.setProperty("useUnicode",		"true");
				properties.setProperty("characterEncoding",	"UTF-8");
				
				database = DriverManager.getConnection(url, properties);
				
				try {
					database.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
				} catch (Exception e) {
					
					System.err.println("DB error Transaction Isolations : " + e.getMessage());
					// TODO Auto-generated catch block
					// e2.printStackTrace();
				}

				DATABASES.put(Thread.currentThread().getName(), database);
			} 
			catch (Exception e) 
			{
				System.err.println("DB connection failed: " + e.getMessage());
				database = null;
			}
		}

		return database;
	}

	public static final void executeQueryAndEnumerateResultSet(String sqlQuery, ResultSetEnumerationHandler handler)
	{
		Connection db = getDBConnection();

		if (db != null) 
		{
			Statement statement = null;
			ResultSet resultSet = null;
			try 
			{
				statement = db.createStatement();
				resultSet = statement.executeQuery(sqlQuery);

				// enumerate
				while (resultSet.next()) 
					if(handler != null)
						handler.onNext(resultSet);
			} 
			catch (Exception e) 
			{
				if(handler != null)
					handler.onError(e);
			}
			finally
			{
				if(statement != null)
					try { statement.close(); } catch (SQLException e) { e.printStackTrace(); }
			}
		}
	}
	
	public static final boolean executeSQLUpdate(String sql)
	{
		boolean result = true;
		
		Connection db = getDBConnection();

		if (db != null) 
		{
			Statement statement = null;
			
			try
			{
				statement = db.createStatement();
				statement.executeUpdate(sql);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				result = false;
			}
			finally
			{
				if(statement != null) try { statement.close(); } catch (SQLException e) { /* ignore */ }
			}
		}
		
		return result;
	}

}
