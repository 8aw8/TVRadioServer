package actions.radio;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import actions.ActionDelegate;
import actions.JSONResponseAction;
import database.Database;
import database.helpers.ResultSetEnumerationHandler;

public class GetSupportedCountiesAction extends JSONResponseAction 
{
	
	public GetSupportedCountiesAction(ActionDelegate delegate, Map<String, String> parameters) 
	{
		super(delegate, parameters);
		
	}

	private String getSQLQuery()
	{
		// return "SELECT `name`,`iso_code` FROM `kladr_countries` WHERE `is_supported`= 1 ORDER BY `name`";
		// return "select c.`name_ru` as name, c.`iso_code` from countries c order by name_ru"; 
		
		return  " select distinct c.`name_ru` as name, c.`iso_code`"+
				" from countries c join regions r on r.`country_id`=c.`id`"+
                " join station_region sr on sr.region_id=r.id "+
                " join stations s on sr.station_id=s.id "+ 
                " where not(c.iso_code is null)"+
                " order by name";
	}

	@Override
	protected String getCacheKey() 
	{
		return this.getClass().getName();
	}

	@Override
	protected Map<String, Object> getResponse()
	{
		final ArrayList<Map<String, String>> array = new ArrayList<Map<String, String>>();
		
		// fetch from db
		Database.executeQueryAndEnumerateResultSet(getSQLQuery(), new ResultSetEnumerationHandler()
		{
			public void onNext(ResultSet resultSet) throws Exception
			{
				Map<String, String> itemInfo = new HashMap<String, String>();
				itemInfo.put("name", resultSet.getString("name"));
				itemInfo.put("iso_code", resultSet.getString("iso_code"));
				
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
