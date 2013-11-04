package actions.radio;

import actions.Action;
import database.Database;

public class AddNewDeviceForExistingClientAction extends Action
{
	private final String userId;
	private final String deviceId;
	private final String deviceFamily;
	private final String osVersion;
	private final float  appVersion;
	private final long 	 createdTimestamp;

	public AddNewDeviceForExistingClientAction(String userId, String deviceId, String deviceFamily, String osVersion, float appVersion)
	{
		super(null, null);
		
		this.userId				= userId;
		this.deviceId			= deviceId;
		this.deviceFamily 		= deviceFamily;
		this.osVersion			= osVersion;
		this.appVersion			= appVersion;
		this.createdTimestamp	= System.currentTimeMillis();
	}
	
	@Override
	public void start() 
	{
		Database.executeSQLUpdate
		(
				"INSERT INTO tbl_users (user_id,device_id,device_family,os_version,app_version,created_at) "+
				"VALUES ('"+userId+"', '"+deviceId+"', '"+deviceFamily+"', '"+osVersion+"', '"+appVersion+"', '"+createdTimestamp/1000+"')" +
				"ON DUPLICATE KEY UPDATE user_id='"+userId+"',device_family='"+deviceId+"',device_family='"+deviceFamily+"',os_version='"+osVersion+"',app_version='"+appVersion+"';"
		);
	}
}
