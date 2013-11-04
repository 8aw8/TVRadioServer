package actions.radio;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import actions.ActionDelegate;
import actions.JSONResponseAction;
import app.TVRadioServerMain;

import database.Database;
import database.helpers.ResultSetEnumerationHandler;

public class GetSupportedRegionsAction extends JSONResponseAction 
{
	private String countryId;
	
	public GetSupportedRegionsAction(ActionDelegate delegate, Map<String, String> parameters) 
	{
		super(delegate, parameters);
		
		 this.countryId = parameters.get("country_iso_code");
		 if (countryId == null) countryId="RU"; 
		
	TVRadioServerMain.logger.info("PARAMETERS>> "+parameters.toString());

		
	//	this.countryId = "US";
		
	}

	private String getSQLQuery()
	{
	//	return  "SELECT `id`,`name` FROM `kladr_regions` " +
	//			"WHERE `country_id` IN (SELECT `id` FROM `kladr_countries` WHERE `iso_code`= '"+countryId+"') AND `is_supported` = '1' " +
	//			"ORDER BY `name`";
	//	return " select distinct r.id, r.name_ru as name" + 
	//	       " from regions r join countries c on r.country_id=c.id" +
	//	                      " JOIN stations s on s.regions_id=r.id"+
	//	       " where c.iso_code='"+countryId+"' order by name";
		
		return " select distinct r.id, r.name_ru as name"+
               " from countries c join regions r on r.country_id=c.id"+
		       "                  join station_region sr on r.id=sr.region_id"+
               "                  JOIN stations s on s.id=sr.station_id"+
               " where c.iso_code='"+countryId+"'"+
               " order by name";
	}

	@Override
	protected String getCacheKey() 
	{
		return this.getClass().getName() + "country_iso_code="+ countryId;
	}

	@Override
	protected Map<String, Object> getResponse()
	{
		final ArrayList<Map<String, String>> array = new ArrayList<Map<String, String>>();
			
		// Перед всеми регионами из БД добаляет виртуальный регион  'rx0001' - "все регионы".
		// В БД его не должно быть
				
				Map<String, String> itemInfo = new HashMap<String, String>();
				itemInfo.put("name", "Все регионы");
				itemInfo.put("id", "rx0001");
			
				array.add(itemInfo);
				
		// fetch from db
		Database.executeQueryAndEnumerateResultSet(getSQLQuery(), new ResultSetEnumerationHandler()
		{
			public void onNext(ResultSet resultSet) throws Exception
			{
				Map<String, String> itemInfo = new HashMap<String, String>();
				itemInfo.put("name", resultSet.getString("name"));
				itemInfo.put("id", resultSet.getString("id"));
				
				array.add(itemInfo);
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
