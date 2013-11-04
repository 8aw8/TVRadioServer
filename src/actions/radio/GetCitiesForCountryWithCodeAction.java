package actions.radio;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import actions.ActionDelegate;
import actions.JSONResponseAction;

import database.Database;
import database.helpers.ResultSetEnumerationHandler;

public class GetCitiesForCountryWithCodeAction extends JSONResponseAction 
{
	public GetCitiesForCountryWithCodeAction(ActionDelegate delegate, Map<String, String> parameters) 
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
		// 0. Check input parameters
		String countryCode 	= parameters.get("countryCode");
		if(!countryCode.matches("^\\w\\w$"))
			return null;

		String offset = parameters.get("offset"); try { Integer.parseInt(offset); } catch(Exception e) { offset = "0"; }
		String count  = parameters.get("count");  try { Integer.parseInt(count); }  catch(Exception e) { count  = "20"; }

		// 1. get countryId and total count
		String totalCount = "0";
		String countryId  = null;
		
		Connection db = Database.getDBConnection();
		if (db != null) 
		{
			Statement statement = null;
			
			try 
			{
				statement = db.createStatement();
				
				// 1. fetch country id
				String 		sql 		= "SELECT `id` FROM `kladr_countries` WHERE `iso_code`='"+countryCode+"' LIMIT 1";
				ResultSet 	resultSet 	= statement.executeQuery(sql);
				
				resultSet.next();
				countryId 	= resultSet.getString("id");
				
				// 2. fetch total count
				sql =  	"SELECT COUNT(*) AS 'total_count'" +
						"FROM `kladr_regions` " +
						"LEFT JOIN `kladr_cities` on `kladr_cities`.`region_id`=`kladr_regions`.`id` " +
						"WHERE `country_id`='"+countryId+"'";
				
				resultSet = statement.executeQuery(sql);
				
				if(resultSet.next())
					totalCount = resultSet.getString("total_count");
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			finally
			{
				if(statement != null) try { statement.close(); } catch (SQLException e) { /* ignore */ }
			}
		}

		// 2. fetch cities
		String sql =	"SELECT `kladr_cities`.`id`,`kladr_cities`.`name`,`kladr_regions`.`name` AS 'region_name'" +
						"FROM `kladr_regions` " +
						"LEFT JOIN `kladr_cities` on `kladr_cities`.`region_id`=`kladr_regions`.`id` " +
						"WHERE `country_id`='"+countryId+"'" +
						"ORDER BY `kladr_cities`.`name` " +
						"LIMIT "+offset+","+count;
		
		// fetch from db
		final ArrayList<Map<String, String>> array = new ArrayList<Map<String, String>>();
		Database.executeQueryAndEnumerateResultSet(sql, new ResultSetEnumerationHandler()
		{
			public void onNext(ResultSet resultSet) throws Exception
			{
				String id 		= resultSet.getString("id");
				String name 	= resultSet.getString("name");
				String region	= resultSet.getString("region_name");
				
				if(id!=null && name!=null && region!=null)
				{
					Map<String, String> itemInfo = new HashMap<String, String>();
					itemInfo.put("city_id", 	id);
					itemInfo.put("city_name", 	name);
					itemInfo.put("region_name", region);
					
					array.add(itemInfo);
				}
			}
		
			public void onError(Exception e)
			{ 
				e.printStackTrace(); 
			}
		});

		Map<String, Object> responseInfo = new HashMap<String, Object>();
		responseInfo.put("cities", array);
		responseInfo.put("total_count", totalCount);
		
		return responseInfo;
	}
}
