package actions.tv;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import actions.ActionDelegate;
import actions.JSONResponseAction;

import database.Database;
import database.helpers.ResultSetEnumerationHandler;

public class GetCategoriesAction extends JSONResponseAction 
{
	public GetCategoriesAction(ActionDelegate delegate, Map<String, String> parameters) 
	{
		super(delegate, parameters);
	}

	private String getSQLQuery()
	{
		return "SELECT * FROM `categories` WHERE `categories`.`type`='tv' ORDER BY `categories`.`order` DESC";
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
				itemInfo.put("album_id", resultSet.getString("id"));
				itemInfo.put("title", resultSet.getString("title"));
				
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
