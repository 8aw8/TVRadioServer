package actions.radio;

import java.util.Map;

import actions.Action;
import actions.ActionDelegate;
import database.Database;

public class AddStationToFavoritesAction extends Action
{
	public AddStationToFavoritesAction(ActionDelegate delegate, Map<String, String> parameters) 
	{
		super(delegate, parameters);
	}

	@Override
	public void start() 
	{
		String stationId 	= parameters.get("stationId");
		String userId 		= delegate.getUserIdForAction(this);
		
		Database.executeSQLUpdate("INSERT IGNORE INTO users_favorites (user_id,station_id) VALUES ('"+userId+"', '"+stationId+"');");
	}
}
