package actions.radio;

import java.util.HashMap;
import java.util.Map;

import actions.ActionDelegate;
import actions.JSONResponseAction;


public class GetDefaultCountryAction extends JSONResponseAction 
{
	public GetDefaultCountryAction(ActionDelegate delegate, Map<String, String> parameters) 
	{
		super(delegate, parameters);
	}

	@Override
	protected String getCacheKey() 
	{
		return "GetDefaultCountryAction";
	}

	@Override
	protected Map<String, Object> getResponse()
	{
		Map<String, String> defaultLocaleInfo = new HashMap<String, String>();
		defaultLocaleInfo.put("iso_code", "RU");

		Map<String, Object> responseInfo = new HashMap<String, Object>();
		responseInfo.put("response", defaultLocaleInfo);
		
		return responseInfo;
	}
}
