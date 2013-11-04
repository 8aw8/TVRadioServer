/**
 * 
 */
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

/**
 * @author AW
 *
 */
public class GetGenreStaionsAction extends JSONResponseAction {
	
	private  String regionId;
	private  String country_iso_code;
	private  String genreId;
	private String offset;
	private String count;
	private String sqlLimit;
	
	public GetGenreStaionsAction(ActionDelegate delegate, Map<String, String> parameters) {
		super(delegate, parameters);
		
		regionId = parameters.get("region_id");
		country_iso_code = parameters.get("country_iso_code");if (country_iso_code == null) country_iso_code = " ";
		genreId =  parameters.get("genreId");
		
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
/*		       sql =	" select distinct s.*, (SELECT GROUP_CONCAT(`urls`.`url`) FROM `urls` WHERE s.`id`=`urls`.`station_id`)  AS `links` , if(uf.station_id is NULL, 0, 1) as is_favorite"+
			            " from regions r join countries c on r.country_id=c.id"+
		    	        "                JOIN stations s on s.regions_id=r.id"+
		    	        "                join genres g on g.id = s.genre_id"+
		    	        "           left join users_favorites uf on uf.station_id = s.id and uf.user_id="+userId+
		     	        " where c.iso_code='"+ country_iso_code + "'" +
		     	        " and g.id = '"+genreId+"'";
	     	        
		      sql = " select distinct s.*, (SELECT GROUP_CONCAT(`urls`.`url`) FROM `urls` WHERE s.`id`=`urls`.`station_id`)  AS `links`,  if(uf.station_id is NULL, 0, 1) as is_favorite,  concat('  ',s.title) as srt"+
		            " from regions r join countries c on r.country_id=c.id"+
		       		"	   JOIN stations s on s.regions_id=r.id"+
		            "      join genres g on g.id = s.genre_id"+
		       		" left join users_favorites uf on uf.station_id = s.id and uf.user_id='"+userId+"'"+
		            " where c.iso_code='"+country_iso_code+"'"+
		            " and g.id = '"+genreId+"'"+
		            " union "+
		            " select  s.*, (SELECT GROUP_CONCAT(`urls`.`url`) FROM `urls` WHERE s.`id`=`urls`.`station_id`)  AS `links`, if(uf.station_id is NULL, 0, 1) as is_favorite, s.title as srt"+
		            " from regions r join countries c on r.country_id=c.id"+
		       		"	             JOIN stations s on s.regions_id=r.id"+
		            "                join genres g on g.id = s.genre_id"+
		       		"           left join users_favorites uf on uf.station_id = s.id and uf.user_id='"+userId+"'"+
		            " where c.iso_code<>'"+country_iso_code+"'"+
		            " and g.id = '"+genreId+"'"+
		            sqlLimit;
*/			      
		      sql =  " select distinct s.*, (SELECT GROUP_CONCAT(`urls`.`url`) FROM `urls` WHERE s.`id`=`urls`.`station_id`)  AS `links`,  if(uf.station_id is NULL, 0, 1) as is_favorite,  -1 as srt"+
				     " from countries co join regions r on r.country_id=co.id"+
				                       " join station_region sr on sr.region_id = r.id"+
				      			       " JOIN stations s on sr.station_id=s.id"+
				      			       " join genres g on g.id = s.genre_id"+
				      		      " left join users_favorites uf on uf.station_id = s.id and uf.user_id='"+userId+"'"+
				     " where co.iso_code='"+country_iso_code+"'"+
				     " and g.id = '"+genreId+"'"+
				     "  union "+
				     " select s.*, (SELECT GROUP_CONCAT(`urls`.`url`) FROM `urls` WHERE s.`id`=`urls`.`station_id`)  AS `links`,  if(uf.station_id is NULL, 0, 1) as is_favorite,  1 as srt"+
				     " from countries co join regions r on r.country_id=co.id"+
				                       " join station_region sr on sr.region_id = r.id"+
				      			       " JOIN stations s on sr.station_id=s.id"+
				      			       " join genres g on g.id = s.genre_id"+
				      		      " left join users_favorites uf on uf.station_id = s.id and uf.user_id='"+userId+"'"+
				     " where co.iso_code<>'"+country_iso_code+"'"+
				     " and g.id = '"+genreId+"'"+
				     " order by srt"+
				     sqlLimit;
		    }
			else
			{
				  
/*				sql = " select s.*, (SELECT GROUP_CONCAT(`urls`.`url`) FROM `urls` WHERE s.`id`=`urls`.`station_id`)  AS `links`,  if(uf.station_id is NULL, 0, 1) as is_favorite"+ 
						" from stations s join genres g on  g.id = s.genre_id"+
						" left join users_favorites uf on uf.station_id = s.id and uf.user_id="+userId+
						" where s.`regions_id` = '"+ regionId+"'"+
						" and g.id = '"+genreId+"'";
*/				  
			sql =   " select s.*, (SELECT GROUP_CONCAT(`urls`.`url`) FROM `urls` WHERE s.`id`=`urls`.`station_id`)  AS `links`,  if(uf.station_id is NULL, 0, 1) as is_favorite, -1 as srt"+
					" from stations s join station_region sr on s.id=sr.station_id"+
					"                 join genres g on g.id = s.genre_id"+
					" left join users_favorites uf on uf.station_id = s.id and uf.user_id='"+userId+"'"+
					" where sr.`region_id` = '"+ regionId+"'"+
					" and g.id = '"+genreId+"'"+
					"  union"+
					" select s.*, (SELECT GROUP_CONCAT(`urls`.`url`) FROM `urls` WHERE s.`id`=`urls`.`station_id`)  AS `links`,  if(uf.station_id is NULL, 0, 1) as is_favorite, 1 as srt"+
					" from stations s join station_region sr on s.id=sr.station_id"+
					"                 join genres g on g.id = s.genre_id"+
					" left join users_favorites uf on uf.station_id = s.id and uf.user_id='"+userId+"'"+
					" where sr.`region_id` <> '"+ regionId+"'"+
					" and g.id = '"+genreId+"'"+
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
