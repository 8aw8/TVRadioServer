package actions.radio;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import actions.ActionDelegate;
import actions.JSONResponseAction;

import database.Database;
import database.helpers.ResultSetEnumerationHandler;

public class GetPlayedSongsForStationAction extends JSONResponseAction 
{
	public GetPlayedSongsForStationAction(ActionDelegate delegate, Map<String, String> parameters) 
	{
		super(delegate, parameters);
	}

	@Override
	protected String getCacheKey() 
	{
		return this.getClass().getName()+parameters.get("stationId");
	}
	
	protected long getCacheExpireTime()
	{
		return System.currentTimeMillis() + 3 * 60 * 1000; // 3 min
	}

	@Override
	protected Map<String, Object> getResponse()
	{
		// 0. Check input parameters
		String stationId = parameters.get("stationId");
		if(!util.Util.notEmpty(stationId))
			return null;

		String offset = parameters.get("offset"); try { Integer.parseInt(offset); } catch(Exception e) { offset = "0"; }
		String count  = parameters.get("count");  try { Integer.parseInt(count); }  catch(Exception e) { count  = "20"; }

		// 1. fetch 
		final ArrayList<Map<String, Object>> array = new ArrayList<Map<String, Object>>();

//		String sql =   	"SELECT `songs`.`id`, `songs`.`title`, `songs`.`artist`, `songs`.`cover_url`, `genres`.`name` AS 'genre', `songs_to_stations`.`timestamp` " +
//						"FROM `stations` " +
//						"LEFT JOIN `songs_to_stations` ON `stations`.`id`=`songs_to_stations`.`station_id` " +
//						"LEFT JOIN `songs` ON `songs`.`id`=`songs_to_stations`.`song_id` " +
//						"LEFT JOIN `songs_to_genres` ON `songs`.`id`=`songs_to_genres`.`song_id` " +
//						"LEFT JOIN `genres` ON `songs_to_genres`.`genre_id`=`genres`.`id` " +
//						"WHERE `stations`.`id`=" +stationId+" " +
//						"ORDER BY timestamp DESC " +
//						"LIMIT "+offset+" , "+count;
		
		String sql = 	"SELECT `songs`.`id`, `songs`.`title`, `songs`.`artist`, `songs`.`cover_url`, `songs_to_stations`.`timestamp` " +
						"FROM `songs`, `songs_to_stations` " +
						"WHERE `songs`.`id` =  `songs_to_stations`.`song_id` AND `songs_to_stations`.`station_id`=" + stationId + " " +
						"ORDER BY `songs_to_stations`.`timestamp` DESC " +
						"LIMIT "+offset+" , "+count;

		Database.executeQueryAndEnumerateResultSet(sql, new ResultSetEnumerationHandler()
		{
			public void onNext(ResultSet resultSet) throws Exception
			{
				String title 	= resultSet.getString("title");
				String artist 	= resultSet.getString("artist");
				String cover 	= resultSet.getString("cover_url");
//				String genre 	= resultSet.getString("genre");
				
				Map<String, Object> itemInfo = new HashMap<String, Object>();
				
				if(title!=null)
					itemInfo.put("title", title);
				
				if(artist!=null)
					itemInfo.put("artist", artist);
				
				if(cover!=null)
					itemInfo.put("cover_url", cover);
				
//				if(genre!=null)
//					itemInfo.put("genre",genre);
//				
				Timestamp timestamp = resultSet.getTimestamp("timestamp");
				if(timestamp!=null)
					itemInfo.put("timestamp", 	Long.toString(timestamp.getTime() / 1000));
				
				array.add(itemInfo);
			}
		
			public void onError(Exception e)
			{ 
				e.printStackTrace(); 
			}
		});

		final Map<String, Object> responseInfo = new HashMap<String, Object>();
		responseInfo.put("response", array);
		
		// 2. total count
		String sql2 =   "SELECT COUNT(*) AS 'total_count' " +
						"FROM `songs_to_stations` "+
						"WHERE `songs_to_stations`.`station_id` =" +stationId;

		Database.executeQueryAndEnumerateResultSet(sql2, new ResultSetEnumerationHandler()
		{
			public void onNext(ResultSet resultSet) throws Exception
			{
				responseInfo.put("total_count", resultSet.getString("total_count"));
			}
		
			public void onError(Exception e)
			{ 
				e.printStackTrace(); 
			}
		});

		
		return responseInfo;
	}
}
