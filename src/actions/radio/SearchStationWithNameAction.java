package actions.radio;


import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import actions.ActionDelegate;
import actions.JSONResponseAction;

import database.Database;
import database.helpers.ResultSetEnumerationHandler;

public class SearchStationWithNameAction extends JSONResponseAction 
{
	public SearchStationWithNameAction(ActionDelegate delegate, Map<String, String> parameters) 
	{
		super(delegate, parameters);
	}

	@Override
	protected String getCacheKey() 
	{
		return null;
	}

	@Override
	protected Map<String, Object> getResponse()
	{
		String offset = parameters.get("offset"); try { Integer.parseInt(offset); } catch(Exception e) { offset = "0"; }
		String count  = parameters.get("count");  try { Integer.parseInt(count); }  catch(Exception e) { count  = "20"; }
		String name   = parameters.get("q");
		

		// 1. fetch 
		final ArrayList<Map<String, Object>> array = new ArrayList<Map<String, Object>>();

		String sql =   	"SELECT `stations`.*, " +
						"(SELECT GROUP_CONCAT(`urls`.`url`) FROM  `urls` WHERE `stations`.`id`=`urls`.`station_id`)  AS `links` "+
						"FROM `stations` "+
						"WHERE `stations`.`title` LIKE '%"+name+"%'" +
						"ORDER BY `stations`.`title` " +
						"LIMIT "+offset+","+count;

		Database.executeQueryAndEnumerateResultSet(sql, new ResultSetEnumerationHandler()
		{
			public void onNext(ResultSet resultSet) throws Exception
			{
				String id 			= resultSet.getString("id");
				String title 		= resultSet.getString("title");
				String cover_link 	= resultSet.getString("image_url");
				String links 		= resultSet.getString("links");
				
				if(id != null && title != null && links != null && cover_link != null)
				{
					String linksArray[]	= links.split(",");
					String url			= (linksArray!=null && linksArray.length>=1) ? linksArray[0] : links;

					Map<String, Object> itemInfo = new HashMap<String, Object>();
					itemInfo.put("aid", 		id);
					itemInfo.put("title", 		title);
					itemInfo.put("url", 		url);
					itemInfo.put("cover_link", 	cover_link);
					itemInfo.put("links", 		Arrays.asList(linksArray));
					
					array.add(itemInfo);
				}
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
						"FROM `stations` "+
						"WHERE `stations`.`title` LIKE '%"+name+"%'";

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
