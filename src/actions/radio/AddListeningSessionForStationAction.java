package actions.radio;

import actions.Action;
import database.Database;

public class AddListeningSessionForStationAction extends Action
{
	private final String stationId;
	private final String userId;
	private final long 	 startTime;
	private final long 	 endTime;

	public AddListeningSessionForStationAction(String stationId, String userId, long startTime, long endTime)
	{
		super(null, null);
		
		this.stationId 	= stationId;
		this.userId		= userId;
		this.startTime	= startTime;
		this.endTime	= endTime;
	}
	
	@Override
	public void start() 
	{
		Database.executeSQLUpdate
		(
				"INSERT INTO stations_listening_info (user_id,start_timestamp,end_timestamp,station_id) "+
				" VALUES ('"+userId+"', '"+startTime/1000+"', '"+endTime/1000+"', '"+stationId+"');"
		);
		
		if(startTime > 0 && endTime > 0)
		{
			Database.executeSQLUpdate
			(
					"INSERT INTO station_listening_time (station_id,time,date) VALUES ('"+stationId+"', '"+((endTime - startTime)/1000)+"', NOW())" +
					"ON DUPLICATE KEY UPDATE time=time+'"+((endTime - startTime)/1000)+"';"
			);
		}
	}
}
