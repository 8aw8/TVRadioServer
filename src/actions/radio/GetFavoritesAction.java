package actions.radio;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import database.Database;
import database.helpers.ResultSetEnumerationHandler;

import actions.ActionDelegate;
import actions.JSONResponseAction;

public class GetFavoritesAction extends JSONResponseAction {

	public GetFavoritesAction(ActionDelegate delegate, Map<String, String> parameters)
	{
		super(delegate, parameters);
		
	}
	@Override
	protected String getCacheKey() 
	{
		// don't cache favorites
		
	//	return this.getClass().getName() + categoryId;
		return null;
	}

	private String getSQLQuery()
	{
		String sql="";
		String userId = delegate.getUserIdForAction(this);
		
		sql = " select s.*, (SELECT GROUP_CONCAT(`urls`.`url`) FROM `urls` WHERE s.`id`=`urls`.`station_id`)  AS `links`, '1' as is_favorite"+
		      " from users_favorites uf join stations s on uf.station_id=s.id"+
		      " where uf.user_id="+userId ;
		return sql;
	}

	@Override
	protected Map<String, Object> getResponse()
	{
		final ArrayList<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
		
		// fetch from db
		Database.executeQueryAndEnumerateResultSet(getSQLQuery(), new ResultSetEnumerationHandler()
		{
			public void onNext(ResultSet resultSet) throws Exception
			{				
				String id 			= resultSet.getString("id");
				String title 		= resultSet.getString("title");
				String cover_link 	= resultSet.getString("image_url");
				String links 		= resultSet.getString("links");
				String isFavorite   = resultSet.getString("is_favorite");
				
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
		if (isFavorite.equals("1")) 
					itemInfo.put("is_favorite", isFavorite);
					
					array.add(itemInfo);
				}
			}
		
			public void onError(Exception e)
			{ 
				e.printStackTrace(); 
			}
		});
		
		Map<String, Object> responseInfo = new HashMap<String, Object>();
		responseInfo.put("response", array);
		
		return responseInfo;
	}
}
