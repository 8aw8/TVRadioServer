package actions.radio;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import actions.ActionDelegate;
import actions.JSONResponseAction;

import database.Database;
import database.helpers.ResultSetEnumerationHandler;

import app.TVRadioServerMain;

public class GetCategoriesAction extends JSONResponseAction 
{
	private String regionId;
	private String country_iso_code;
	private String userLocale;
	private String offset;
	private String count;
	private String sqlLimit;
	private Map<String, String> param;
	private ArrayList<Map<String, String>> array; 
	
	public GetCategoriesAction(ActionDelegate delegate, Map<String, String> parameters) 
	{
		super(delegate, parameters);
		
		this.regionId = parameters.get("region_id"); if (regionId == null) regionId="";
	//	this.country_iso_code = parameters.get("country_iso_code");  if (country_iso_code == null) country_iso_code="";
		this.country_iso_code = parameters.get("iso_code"); if (country_iso_code == null) country_iso_code="";
		this.userLocale = parameters.get("locale"); if (userLocale == null) userLocale="EN";
		this.offset = parameters.get("offset"); 
		this.count = parameters.get("count"); 
		if (!((offset == null) && (count == null))) sqlLimit = " limit "+offset+ ", "+count; else sqlLimit="";
		param = parameters;
	  //  TVRadioServerMain.logger.info("cache key >> "+getCacheKey());	
	}

	private String getCategoriesSQLQuery()
	{	   
	    String sql= "";
	 	
   		if (regionId.equals("rx0001") ) // Все станции страны "все регионы"
   		{
 /*   	sql =   " select c.id, ifnull(lc.title, c.title_default) as title, count(1) as count"+
    			" from  categories_stations cs join categories c on cs.category_id=c.id"+
    			"						 join stations s on cs.station_id=s.id"+  
    			" left join locale_categories lc on c.id=lc.categories_id and lc.locale_iso_code='"+userLocale+"'"+ 
    			" where exists (select 1 from stations s1 join regions r on r.id=s1.regions_id"+
    			"			                        join countries co on r.country_id=co.id"+
    			" where s1.id=s.id and co.iso_code = '"+country_iso_code+"')"+
    			" group by c.id, c.title"+
    			sqlLimit;
 */  	
			sql =   " select c.id, ifnull(lc.title, c.title_default) as title, (select count(distinct s.id) from station_region sr join stations s on sr.station_id=s.id) as count, -100 as srt"+
			        "	from categories c left join locale_categories lc on c.id=lc.categories_id and lc.locale_iso_code='"+userLocale+"'"+
			        "	where c.id=11"+
			        "	 union"+
			        "	select c.id, ifnull(lc.title, c.title_default) as title,"+
			        " (select count(distinct sr.station_id)"+
			        "	from countries co join regions r on co.id=r.country_id"+
			    					" join station_region sr on r.id= sr.region_id"+
			        " where co.iso_code = '"+country_iso_code+"') as count, -90 as srt"+
			        " from categories c left join locale_categories lc on c.id=lc.categories_id and lc.locale_iso_code='"+userLocale+"'"+
			        " where c.id=24"+
			    	" union "+
			    	" select c.id, ifnull(lc.title, c.title_default) as title, 50 as count, -80 as srt"+
			    	" from categories c left join locale_categories lc on c.id=lc.categories_id and lc.locale_iso_code='"+userLocale+"'"+
			    	" where c.id=1"+
			    	" union"+
			    	" select c.id, ifnull(lc.title, c.title_default) as title, 50 as count, -70 as srt"+
			    	" from categories c left join locale_categories lc on c.id=lc.categories_id and lc.locale_iso_code='"+userLocale+"'"+
			    	" where c.id=13"+
			    	" union"+
			    	" select c.id, ifnull(lc.title, c.title_default) as title, count(distinct s.id) as count, 1 as srt"+
			    	" from countries co join regions r on co.id=r.country_id"+
			    	                  " join station_region sr on r.id= sr.region_id"+
			    					  " join stations s on s.id = sr.station_id"+
			    	                  " join categories_stations cs on s.id=cs.station_id"+
			    	                  " join categories c on c.id = cs.category_id"+ 
			    				 " left join locale_categories lc on c.id=lc.categories_id and lc.locale_iso_code='"+userLocale+"'"+
			    	" where  c.id not in (1,11,13,24) and co.iso_code = '"+country_iso_code+"'"+
			    	" group by c.id"+
			    	" order by srt, title"+
			    	sqlLimit;
   		}	
   		else 
   		{
 /*  			sql = " select cs.category_id as id, ifnull(lc.title, c.title_default) as title, count(1) as count"+	
   				  " from  categories_stations cs join categories c on cs.category_id=c.id"+
   			      						  " left join locale_categories lc on c.id=lc.categories_id and lc.locale_iso_code='"+userLocale+"'"+
   			      " where cs.station_id in (select sr.station_id"+
   										 " from station_region sr "+
   			                             " where sr.region_id='"+regionId+"')"+
   			      " group by cs.category_id"+
   			      sqlLimit;
 */
			sql = "	select c.id, ifnull(lc.title, c.title_default) as title, (select count(distinct s.id) from station_region sr join stations s on sr.station_id=s.id) as count, -100 as srt"+
				"	from categories c left join locale_categories lc on c.id=lc.categories_id and lc.locale_iso_code='"+userLocale+"'"+
				"	where c.id=11"+
				"	 union"+
				"	select c.id, ifnull(lc.title, c.title_default) as title, (select count(distinct sr.station_id) from station_region sr where sr.region_id='"+regionId+"') as count, -90 as srt"+
				"	from categories c left join locale_categories lc on c.id=lc.categories_id and lc.locale_iso_code='"+userLocale+"'"+
				"	where c.id=24"+
				"	 union"+
				"	select c.id, ifnull(lc.title, c.title_default) as title, 50 as count, -80 as srt"+
				"	from categories c left join locale_categories lc on c.id=lc.categories_id and lc.locale_iso_code='"+userLocale+"'"+
				"	where c.id=1"+
				"	 union"+
				"	select c.id, ifnull(lc.title, c.title_default) as title, 50 as count, -70 as srt"+
				"	from categories c left join locale_categories lc on c.id=lc.categories_id and lc.locale_iso_code='"+userLocale+"'"+
				"	where c.id=13"+
				"	 union"+
				"	select cs.category_id, ifnull(lc.title, c.title_default) as title, count(distinct cs.station_id) as count, 1 as srt"+	
				"	from  categories_stations cs join station_region sr on cs.station_id=sr.station_id"+
				"	                             join categories c on cs.category_id=c.id"+
				"	                        left join locale_categories lc on c.id=lc.categories_id and lc.locale_iso_code='"+userLocale+"'"+
				"	where c.id not in (1,11,13,24) and sr.region_id='"+regionId+"'"+
				"	group by cs.category_id"+
				"	order by srt, title"+
		    	sqlLimit;
   		}
   		
   		TVRadioServerMain.logger.info("---------------SQL >> "+sql);	
   		
   		return 	sql;
	}
	
	private String getGenresSQLQuery()
	{
		String sql= "";
		
		if (regionId.equals("rx0001") ) // Все станции страны "все регионы"
   		{
			
    	sql = " select g.id, ifnull(lg.title, g.name_default) as title, count(1) as count"+
    	      " from genres g join stations s on s.`genre_id`= g.id"+
    	      "          left join locale_genres lg on lg.`genres_id`=g.id and lg.`locale_iso_code`='"+userLocale+ "'"+
    	      " where exists (select 1 from stations s1 join regions r on r.id=s1.regions_id"+
    	 	  "	                                        join countries co on r.country_id=co.id"+
    	      " where s1.id=s.id and co.iso_code = '"+country_iso_code+"')"+
    	      " group by g.id"+
    	      " order by title";
   		}	
   		else 
   		{
   			sql = " select g.id,  ifnull(lg.title, g.name_default) as title, count(1) as count"+
   			      " from genres g join stations s on s.`genre_id`= g.id"+
   			      "          left join locale_genres lg on lg.`genres_id`=g.id and lg.`locale_iso_code`='"+userLocale+ "'"+ 
   			      " where s.`regions_id`='"+regionId+"'"+ 
   			      " group by g.id"+
   			      " order by title";
   			
   		}
		
		return sql;
	}

	private String getMoodsSQLQuery()
	{
		String sql = "";
		
		if (regionId.equals("rx0001") ) // Все станции страны "все регионы"
   		{
	  sql = " select m.id, ifnull(lm.title, m.title_default) as title, count(1) as count"+
			" from stations s join moods m on s.moods_id=m.id"+
			"            left join locale_moods lm on lm.`moods_id`=m.id and lm.`locale_iso_code`='"+userLocale+"'"+
			" where exists (select 1 from stations s1 join regions r on r.id=s1.regions_id"+
			"  										join countries co on r.country_id=co.id"+
			" where s1.id=s.id and co.iso_code = '"+country_iso_code+"')"+
			" group by m.id"+
			" order by title";
   		}	
   		else 
   		{
   			sql = " select m.id, ifnull(lm.title, m.title_default) as title, count(1) as count"+
   			      " from stations s join moods m on s.moods_id=m.id"+
   			      "            left join locale_moods lm on lm.`moods_id`=m.id and lm.`locale_iso_code`='"+userLocale+"'"+
   			      " where s.regions_id = '"+regionId+"'"+ 
   			      " group by m.id"+
   			      " order by title";
   		}
		
		return sql;
	}
	@Override
	protected String getCacheKey() 
	{
		String cacheKey = "method="+param.get("method")+
				          ",iso_code="+param.get("iso_code")+
				          ",region_id="+param.get("region_id")+
				          ",userLocale="+param.get("userLocale");
		//"region_id="+parameters.get("region_id")
		//	return this.getClass().getName();
		return cacheKey;
	//	return null;
	}
	
	private ArrayList<Map<String, String>> getQueryFetchFromDB(String sqlQuery)
	{
		array = new ArrayList<Map<String, String>>();
		// fetch from db
				Database.executeQueryAndEnumerateResultSet(sqlQuery, new ResultSetEnumerationHandler()
				{
					public void onNext(ResultSet resultSet) throws Exception
					{
							Map<String, String> itemInfo = new HashMap<String, String>();
							itemInfo.put("album_id", resultSet.getString("id"));
							itemInfo.put("title", resultSet.getString("title"));
							itemInfo.put("count", resultSet.getString("count"));
							
							array.add(itemInfo);
					}
				
					public void onError(Exception e)
					{ 
						e.printStackTrace(); 
					}
				});
		
		return array;
	}
	

	@Override
	protected Map<String, Object> getResponse()
	{
	//	Все станции", "Все станции региона", "50 самых популярных", "50 последних добавленных"
		
	//	public static final String CATEGORY_ID_FOR_ALL_STATIONS 		= "11";
	//	public static final String CATEGORY_ID_FOR_LAST50_STATIONS 		= "13";
	//	public static final String CATEGORY_ID_FOR_TOP50_STATIONS 		= "1";
	//	public static final String CATEGORY_ID_FOR_FAVORITE_STATIONS 	= "favorites";
	//	public static final String REGION_ID_FOR_ALL_REGIONS_OF_COUNTRY = "rx0001";
		
	
		ArrayList<Map<String, String>> arrayCategories = getQueryFetchFromDB(getCategoriesSQLQuery());
		
	//	Map<String, String> itemInfo = new HashMap<String, String>();
	//	itemInfo.put("album_id", CATEGORY_ID_FOR_ALL_STATIONS);
	//	itemInfo.put("title", resultSet.getString("title"));
	//	itemInfo.put("count", resultSet.getString("count"));
		
	//	arrayCategories.add(0, itemInfo);
		
		
		ArrayList<Map<String, String>> arrayGenres     = getQueryFetchFromDB(getGenresSQLQuery());  
		ArrayList<Map<String, String>> arrayMoods      = getQueryFetchFromDB(getMoodsSQLQuery());		

		Map<String, Object> responseInfo = new HashMap<String, Object>();
		
		Map<String, Object> responseTypeArray = new HashMap<String, Object>();
		
		
		responseTypeArray.put("moods", arrayMoods);
		responseTypeArray.put("genres", arrayGenres);
		responseTypeArray.put("categories", arrayCategories);
		
		//responseInfo.put("response", array);
      responseInfo.put("response", responseTypeArray);
		
	//	responseInfo.put("response", arrayCategories);
      TVRadioServerMain.logger.info("--------------------------------------------------------");
      TVRadioServerMain.logger.info("---------- arrayCategories= "+arrayCategories.toString());
      TVRadioServerMain.logger.info("--------------------------------------------------------");
		
		return responseInfo;
	}
}
