package actions;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import cache.LRUSampleCache;

public class JSONResponseAction extends Action 
{
	public static final String CATEGORY_ID_FOR_ALL_STATIONS 		= "11";
	public static final String CATEGORY_ID_FOR_LAST50_STATIONS 		= "13";
	public static final String CATEGORY_ID_FOR_TOP50_STATIONS 		= "1";
	public static final String CATEGORY_ID_FOR_FAVORITE_STATIONS 	= "favorites";
	public static final String REGION_ID_FOR_ALL_REGIONS_OF_COUNTRY = "rx0001";
	
	private static final LRUSampleCache<String, Object> cache = new LRUSampleCache<String, Object>(500);
    private final String actionId;
	
	public JSONResponseAction(ActionDelegate delegate, Map<String,String> parameters) 
	{
		super(delegate, parameters);
		
		this.actionId = parameters == null ? null : parameters.get("id");
	}

	@Override
	public void start() 
	{
		// notify delegate
		if(delegate != null)
			delegate.didFinishAction(this);
	}
	
	protected String getCacheKey()
	{
		return null;
	}
	
	protected Map<String, Object> getResponse()
	{
		return null;
	}

	protected long getCacheExpireTime()
	{
		return System.currentTimeMillis() + 30 * 60 * 1000; // 30 min
	}
	
	@SuppressWarnings("unchecked")
	public byte[] getJSONResponse()
	{
		Map<String, Object> response;
		
		// get response data
		String cacheKey = getCacheKey();
		if(cacheKey != null)
		{
		//	app.TVRadioServerMain.logger.info("------------GET IN CACHE for key: "+cacheKey);
			response = (Map<String, Object>) cache.get(cacheKey);
			if(response == null)
			{
			//	app.TVRadioServerMain.logger.info("NO CACHED OBJECT for key: "+cacheKey);
				response = getResponse();
				cache.put(cacheKey, response, getCacheExpireTime());
			}
		}
		else
		{
		//	app.TVRadioServerMain.logger.info("------------!!! GET IN DATABASE for key: "+cacheKey);
			response = getResponse();
		}
		
		if(response == null)
			return null;
		
		// add actionId
		if(actionId != null)
			response.put("id", this.actionId);
		
		byte[] result = null;
		try {
			app.TVRadioServerMain.logger.info(util.writer.json.JsonWriter.toJSONString(response));
			result = util.writer.json.JsonWriter.toJSONString(response).getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result; 
	}
}
