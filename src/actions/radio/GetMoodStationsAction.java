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

public class GetMoodStationsAction extends JSONResponseAction {

	private  String regionId;
	private  String country_iso_code;
	private  String moodId;
	private String offset;
	private String count;
	private String sqlLimit;
	
	public GetMoodStationsAction(ActionDelegate delegate, Map<String, String> parameters) {
		super(delegate, parameters);
		this.regionId = parameters.get("region_id");
		this.country_iso_code = parameters.get("country_iso_code");
		this.moodId  =  parameters.get("moodId");
		
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
		
		
	//	return this.getClass().getName() + categoryId;
		return null;
	}

	private String getSQLQuery()
	{
		String sql="";
		String userId = delegate.getUserIdForAction(this);
		
			if (regionId.equals("rx0001")) 
			{
				sql =  " select distinct s.*, (SELECT GROUP_CONCAT(`urls`.`url`) FROM `urls` WHERE s.`id`=`urls`.`station_id`)  AS `links` , if(uf.station_id is NULL, 0, 1) as is_favorite, -1 as srt"+
					   " from countries c join regions r on r.country_id=c.id"+
								" join station_region sr on sr.region_id=r.id"+
				                " JOIN stations s on sr.station_id= s.id"+
				                " join moods m on m.id = s.moods_id"+
					       " left join users_favorites uf on uf.station_id = s.id and uf.user_id='"+userId+"'"+
					   " where c.iso_code='"+country_iso_code+"'"+
				       " and m.id = '"+moodId+"'"+
				       " union "+
				       " select distinct s.*, (SELECT GROUP_CONCAT(`urls`.`url`) FROM `urls` WHERE s.`id`=`urls`.`station_id`)  AS `links` , if(uf.station_id is NULL, 0, 1) as is_favorite, 1 as srt"+
				       " from countries c join regions r on r.country_id=c.id"+
										" join station_region sr on sr.region_id=r.id"+
										" JOIN stations s on sr.station_id= s.id"+
										" join moods m on m.id = s.moods_id"+
								   " left join users_favorites uf on uf.station_id = s.id and uf.user_id='"+userId+"'"+
						" where c.iso_code<>'"+country_iso_code+"'"+
						" and m.id = '"+moodId+"'"+
						" order by srt"+
						sqlLimit;
		    }
			else
			{
				  sql = " select s.*, (SELECT GROUP_CONCAT(`urls`.`url`) FROM `urls` WHERE s.`id`=`urls`.`station_id`)  AS `links`,  if(uf.station_id is NULL, 0, 1) as is_favorite, -1 as srt"+
						" from stations s join moods m on  m.id = s.moods_id"+
						                " join station_region sr on s.id=sr.station_id"+
			                       " left join users_favorites uf on uf.station_id = s.id and uf.user_id='"+userId+"'"+
			            " where sr.`region_id` = '"+regionId +"'"+
			            " and m.id = '"+moodId+"'"+
			            " union "+
						" select s.*, (SELECT GROUP_CONCAT(`urls`.`url`) FROM `urls` WHERE s.`id`=`urls`.`station_id`)  AS `links`,  if(uf.station_id is NULL, 0, 1) as is_favorite, 1 as srt"+
			            " from stations s join moods m on  m.id = s.moods_id"+
						  				" join station_region sr on s.id=sr.station_id"+
						  		   " left join users_favorites uf on uf.station_id = s.id and uf.user_id='"+userId+"'"+
			            " where sr.`region_id` <> '"+regionId +"'"+
			            " and m.id = '"+moodId+"'"+
			            " order by srt"+
			            sqlLimit;
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
