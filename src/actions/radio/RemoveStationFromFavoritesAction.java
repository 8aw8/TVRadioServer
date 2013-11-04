package actions.radio;

import java.util.Map;

import actions.Action;
import actions.ActionDelegate;
import database.Database;

public class RemoveStationFromFavoritesAction extends Action
{
	public RemoveStationFromFavoritesAction(ActionDelegate delegate, Map<String, String> parameters) 
	{
		super(delegate, parameters);
	}
	
	@Override
	public void start() 
	{
		String stationId = parameters.get("stationId");
		String userId 	 = delegate.getUserIdForAction(this);
		
		Database.executeSQLUpdate("DELETE FROM users_favorites WHERE user_id='"+userId+"' AND station_id='"+stationId+"'");
	}
}
