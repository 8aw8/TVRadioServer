package util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class Util 
{
	public final static boolean notEmpty(String s)
	{
		return (s != null && s.length() > 0);
	}
	
	public final static Map<String, String> getQueryMap(String query) 
			throws UnsupportedEncodingException 
	{
		Map<String, String> map = new HashMap<String, String>(10);
		String[] params = query.split("\\&");
		if (params.length == 1)
			params = query.split("\\,");

		for (String param : params) {
			String[] fields = param.split("=", 2);
			String key = fields[0];
			String value = null;

			if (fields.length > 1) {
				value = URLDecoder.decode(fields[1], "UTF-8");
				if (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("\'") && value.endsWith("\'")) {
					value = value.substring(1, value.length() - 1);
				}
			}

			map.put(key, value);
		}

		return map;
	}
	
	public final static boolean isNumeric(String str)  
	{  
		try  
		{  
			Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe)  
		{  
			return false;  
		}  
		return true;  
	}
	
	public final static float appVersionToFloat(String version)
	{
		float result = .0f;
		if(Util.notEmpty(version))
		{
			String[] components = version.split("\\.");
			for(int i=0; i<components.length; i++)
			{
				int componentValue = 0;
				try{ componentValue = Integer.parseInt(components[i]); }catch(Exception e){ /*ignore*/ }
				result += (float)componentValue/(i==0?1:i*10);
			}
		}
		
		return result;
	}
	

}
