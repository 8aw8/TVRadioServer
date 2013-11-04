package actions.tv;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import actions.ActionDelegate;
import actions.JSONResponseAction;

import database.Database;
import database.helpers.ResultSetEnumerationHandler;

public class GetCategoryContentAction extends JSONResponseAction 
{
	private final String categoryId;
	
	public GetCategoryContentAction(ActionDelegate delegate, Map<String, String> parameters) 
	{
		super(delegate, parameters);

		this.categoryId = parameters.get("categoryId");
	}
	
	@Override
	protected String getCacheKey() 
	{
		return this.getClass().getName() + categoryId;
	}

	private String getSQLQuery()
	{
		return  "SELECT `channels`.*, "+
				"(SELECT GROUP_CONCAT(`tv_channels_urls`.`url`) FROM `tv_channels_urls` WHERE `channels`.`id`=`tv_channels_urls`.`channel_id`)  AS `links` "+
				"FROM `categories_channels` "+
				"LEFT JOIN `channels` ON `channels`.`id`=`categories_channels`.`channel_id` "+
				"WHERE `categories_channels`.`category_id`="+categoryId +" " +
				"ORDER BY `channels`.`title`";
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
				String description 	= resultSet.getString("description");
				String cover_link 	= resultSet.getString("image_url");
				String links 		= resultSet.getString("links");
				
				if(id != null && title != null && links != null && cover_link != null)
				{
					String linksArray[]	= links.split(",");
					String url			= (linksArray!=null && linksArray.length>=1) ? linksArray[0] : links;

					String types[] = {"mp4_360", "mp4_480", "mp4_720", "mp4_1080"};
					
					Map<String, String> linksInfo = new HashMap<String, String>();
					for(int i = 0; i<linksArray.length && i<types.length; i++) 
						linksInfo.put(types[i], linksArray[i]);
					
					Map<String, Object> itemInfo = new HashMap<String, Object>();
					itemInfo.put("id",	 		id);
					itemInfo.put("title", 		title);
					itemInfo.put("description", description);
					itemInfo.put("url", 		url);
					itemInfo.put("coverLink", 	cover_link);
					itemInfo.put("files", 		linksInfo);
					itemInfo.put("type", 		resultSet.getString("format"));
					
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
