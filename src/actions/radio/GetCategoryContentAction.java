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

public class GetCategoryContentAction extends JSONResponseAction 
{
	
	private final String categoryId;
	private final String regionId;
	private final String iso_code;
	private String offset;
	private String count;
	private String sqlLimit;
	
	public GetCategoryContentAction(ActionDelegate delegate, Map<String, String> parameters) 
	{
		super(delegate, parameters);

		this.categoryId = parameters.get("categoryId");
		this.regionId = parameters.get("region_id");
		this.iso_code = parameters.get("iso_code");
		this.offset = parameters.get("offset"); 
		this.count = parameters.get("count"); 
		
		sqlLimit = " ";
		if (!((offset == null) && (count == null))) sqlLimit = " limit "+offset+ ", "+count;
		
		app.TVRadioServerMain.logger.info("------------ GET PARAM: "+parameters.toString());
		
	}

	@Override
	protected String getCacheKey() 
	{
		// don't cache favorites
		if(categoryId.equals("favorites"))
			return null;
		
	//	return this.getClass().getName() + categoryId;
		return null;
	}

	private String getSQLQuery()
	{
		String sql;
		
		String userId = delegate.getUserIdForAction(this);
		
		// all stations
		if(categoryId.equals(CATEGORY_ID_FOR_ALL_STATIONS))
		{
			sql =   " SELECT `stations`.*, " +
					" (SELECT GROUP_CONCAT(`urls`.`url`) FROM `urls` WHERE `stations`.`id`=`urls`.`station_id`)  AS `links`, if(uf.station_id is NULL, 0, 1) as is_favorite  "+
					" FROM `stations` "+
					" left join users_favorites uf on uf.station_id = stations.id and uf.user_id="+userId+
					" ORDER BY `stations`.`title`"+
					sqlLimit;
		}
		// last added
		else if(categoryId.equals(CATEGORY_ID_FOR_LAST50_STATIONS))
		{
			sql =   " SELECT `stations`.*, "+
					" (SELECT GROUP_CONCAT(`urls`.`url`) FROM  `urls` WHERE `stations`.`id`=`urls`.`station_id`)  AS `links`,  if(uf.station_id is NULL, 0, 1) as is_favorite  "+
					" FROM `stations` " +
					" left join users_favorites uf on uf.station_id = stations.id and uf.user_id="+userId+
					" ORDER BY `stations`.`id` DESC "+
					" LIMIT 50";
		}
		// top popular station
		else if(categoryId.equals(CATEGORY_ID_FOR_TOP50_STATIONS))
		{
			sql =   " SELECT `stations`.*, " +
					" (SELECT GROUP_CONCAT(`urls`.`url`) FROM  `urls` WHERE `stations`.`id`=`urls`.`station_id`)  AS `links`,  if(uf.station_id is NULL, 0, 1) as is_favorite " +
					" FROM `station_listening_time` " +
					" LEFT JOIN `stations` ON `stations`.`id`=`station_listening_time`.`station_id` " +
					" left join users_favorites uf on uf.station_id = stations.id and uf.user_id="+userId+
					" WHERE `station_listening_time`.`date` = DATE(DATE_SUB(NOW(), INTERVAL 1 day)) " + 
					" ORDER BY `station_listening_time`.`time` DESC " +
					" LIMIT 50";
		}
		// favorites
		else if(categoryId.equals(CATEGORY_ID_FOR_FAVORITE_STATIONS))
		{
		
			sql =   " SELECT `stations`.*, '1' as is_favorite, "+
					" (SELECT GROUP_CONCAT(`urls`.`url`) FROM  `urls` WHERE `stations`.`id`=`urls`.`station_id`)  AS `links` "+
					" FROM `stations` " +
					" WHERE `stations`.`id` IN (SELECT `station_id` FROM `users_favorites` WHERE `user_id` = '" + userId + "') "+
					" ORDER BY `stations`.`title`"+
					sqlLimit;
		}
		// other
		else
		{	
			if (regionId.equals(REGION_ID_FOR_ALL_REGIONS_OF_COUNTRY))
			{
		       sql =	" select distinct s.*, (SELECT GROUP_CONCAT(`urls`.`url`) FROM `urls` WHERE s.`id`=`urls`.`station_id`)  AS `links` , if(uf.station_id is NULL, 0, 1) as is_favorite"+
			            " from countries c join regions r on r.country_id=c.id"+
			            				 " join station_region sr on sr.region_id=r.id"+
			            				 " JOIN stations s on sr.station_id= s.id"+
			            				 " join categories_stations cs on s.id = cs.station_id"+ 
			            			" left join users_favorites uf on uf.station_id = s.id and uf.user_id='"+userId+"'"+
		     	        " where c.iso_code='"+ iso_code + "'" +
		     	        " and cs.category_id='"+categoryId+"'"+
		     	        sqlLimit;
			}
			else
			{
				  sql = " select s.*, (SELECT GROUP_CONCAT(`urls`.`url`) FROM `urls` WHERE s.`id`=`urls`.`station_id`)  AS `links`,  if(uf.station_id is NULL, 0, 1) as is_favorite"+ 
						" from stations s join `categories_stations` cs on s.`id`=cs.`station_id`"+
										" join station_region sr on s.id= sr.station_id"+ 
								   " left join users_favorites uf on uf.station_id = s.id and uf.user_id='"+userId+"'"+
						" where sr.`region_id` = '"+ regionId+"'"+
						" and cs.category_id='"+categoryId+"'"+
						sqlLimit;
			}
		}

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
