package actions.radio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import actions.ActionDelegate;
import actions.JSONResponseAction;

public class AddStationAction extends JSONResponseAction 
{
	public AddStationAction(ActionDelegate delegate, Map<String, String> parameters) 
	{
		super(delegate, parameters);
	}

	@Override
	protected Map<String, Object> getResponse()
	{
		Map<String, String> info = new HashMap<String, String>();
		Map<String, Object> responseInfo = new HashMap<String, Object>();
		responseInfo.put("response", info);
		
		String stationTitle  		= parameters.get("stationTitle");
		String stationCoverLink  	= parameters.get("stationCoverLink");
		String cityId		 		= parameters.get("city_id");
		
		ArrayList<String> urls = new ArrayList<String>();
		Set<Entry<String, String>> entries = parameters.entrySet();
		for(Entry<String, String> entry : entries)
		{
			if(entry.getKey().startsWith("stationURL"))
				urls.add(entry.getValue());
		}
		
		if ( 
				stationTitle != null 		&& stationTitle.length() > 0 		&&
				stationCoverLink != null 	&& stationCoverLink.length() > 0 	&&
				cityId != null 				&& cityId.length() > 0 				&&
				urls.size() > 0
			)
			// all ok, add station
		{
			info.put("addedUserStationId", "1");
		}
		else
			// error on adding station
		{
			info.put("error", "lack the necessary parameter");
		}
		
		return responseInfo;
	}
}
