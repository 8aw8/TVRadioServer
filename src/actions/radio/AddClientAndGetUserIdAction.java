package actions.radio;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import actions.Action;
import database.Database;

public class AddClientAndGetUserIdAction extends Action
{
	private final String deviceId;
	private final String deviceFamily;
	private final String osVersion;
	private final float  appVersion;
	private final long 	 createdTimestamp;

	private String userId = null;

	public AddClientAndGetUserIdAction(String deviceId, String deviceFamily, String osVersion, float appVersion)
	{
		super(null, null);
		
		this.deviceId			= deviceId;
		this.deviceFamily 		= deviceFamily;
		this.osVersion			= osVersion;
		this.appVersion			= appVersion;
		this.createdTimestamp	= System.currentTimeMillis();
	}
	
	@Override
	public void start() 
	{
		Connection db = Database.getDBConnection();
		if (db != null) 
		{
			Statement statement = null;
			
			try 
			{
				statement = db.createStatement();
				
				String 		sql;
				ResultSet 	resultSet;
				
				// 1. insert new device info
				sql = 	"INSERT INTO tbl_users (device_id,device_family,os_version,app_version,created_at) "+
						"VALUES ('"+deviceId+"', '"+deviceFamily+"', '"+osVersion+"', '"+appVersion+"', '"+createdTimestamp/1000+"')" +
						"ON DUPLICATE KEY UPDATE device_id='"+deviceId+"',device_family='"+deviceFamily+"',os_version='"+osVersion+"',app_version='"+appVersion+"';";
				
				statement.executeUpdate(sql);
				
				// 2. fetch userId
				sql =   "SELECT `id` FROM `tbl_users` WHERE `device_id`='"+deviceId+"'";
				
				resultSet = statement.executeQuery(sql);
				
				if(resultSet.next())
				{
					userId = resultSet.getString("id");
					if(userId == null || userId.equalsIgnoreCase("null") || userId.length() == 0)
						userId = null;
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			finally
			{
				if(statement != null) try { statement.close(); } catch (SQLException e) { /* ignore */ }
			}
		}
	}
	
	public String getUserId()
	{
		return userId;
	}
}
