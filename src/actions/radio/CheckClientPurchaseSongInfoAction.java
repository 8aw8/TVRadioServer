package actions.radio;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import actions.ActionDelegate;
import actions.JSONResponseAction;

import database.Database;

public class CheckClientPurchaseSongInfoAction extends JSONResponseAction 
{
	private final static ArrayList<String> PURCHASED_CLIENTS_ARRAY = new ArrayList<String>();
	private final String deviceId;
	
	public CheckClientPurchaseSongInfoAction(ActionDelegate delegate, String deviceId) 
	{
		super(delegate, null);
		
		this.deviceId = deviceId;
	}

	@Override
	protected String getCacheKey() 
	{
		return null;
	}

	private String getSQLQuery()
	{
		return "SELECT `device_id` FROM `users` WHERE `users`.`device_id`='"+deviceId+"'";
	}

	@Override
	protected Map<String, Object> getResponse()
	{
		boolean isPurchased = false;

		// already sent info about purchase
		if(PURCHASED_CLIENTS_ARRAY.contains(deviceId))
			return null;
		else
		{
			Connection db = Database.getDBConnection();
			if (db != null) 
			{
				Statement statement = null;
				try
				{
					statement = db.createStatement();

					ResultSet resultSet = statement.executeQuery(getSQLQuery());
					
					if(resultSet.next())
					{
						if(resultSet.getString("device_id").equals(deviceId))
						{
							// add to cache
							PURCHASED_CLIENTS_ARRAY.add(deviceId);
							
							isPurchased = true;
						}
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
		
		if(isPurchased)
		{
			Map<String, Object> deviceInfo = new HashMap<String, Object>();
			deviceInfo.put("deviceId", deviceId+"#");
			
			Map<String, Object> responseInfo = new HashMap<String, Object>();
			responseInfo.put("info", deviceInfo);
			
			return responseInfo;
		}
		
		return null;
	}
}
