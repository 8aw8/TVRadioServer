package util.writer.json;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonWriter 
{
	/**
	 * Convert an object to JSON text.
	 * <p>
	 * If this object is a Map or a List, and it's also a JSONAware, JSONAware
	 * will be considered firstly.
	 * <p>
	 * DO NOT call this method from toJSONString() of a class that implements
	 * both JSONAware and Map or List with "this" as the parameter, use
	 * JSONObject.toJSONString(Map) or JSONArray.toJSONString(List) instead.
	 * 
	 * @see org.json.simple.JSONObject#toJSONString(Map)
	 * @see org.json.simple.JSONArray#toJSONString(List)
	 * 
	 * @param value
	 * @return JSON text, or "null" if value is null or it's an NaN or an INF
	 *         number.
	 */
	public static String toJSONString(Object value) 
	{
		if (value == null)
			return "null";

		if (value instanceof String)
			return "\"" + escape((String) value) + "\"";

		if (value instanceof Double) 
		{
			if (((Double) value).isInfinite() || ((Double) value).isNaN())
				return "null";
			else
				return value.toString();
		}

		if (value instanceof Float) 
		{
			if (((Float) value).isInfinite() || ((Float) value).isNaN())
				return "null";
			else
				return value.toString();
		}

		if (value instanceof Number)
			return value.toString();

		if (value instanceof Boolean)
			return value.toString();

		if (value instanceof Map)
			return mapToJSONString((Map<?,?>) value);

		if (value instanceof List)
			return listToJSONString((List<?>) value);

		return value.toString();
	}

	//////////////////////////////////////////////////////////////
	// Private
	//////////////////////////////////////////////////////////////
	
	/**
	 * Convert a list to JSON text. The result is a JSON array. If this list is
	 * also a JSONAware, JSONAware specific behaviours will be omitted at this
	 * top level.
	 * 
	 * @see org.json.simple.JSONValue#toJSONString(Object)
	 * 
	 * @param list
	 * @return JSON text, or "null" if list is null.
	 */
	private static String listToJSONString(List<?> list) 
	{
		if (list == null)
			return "null";

		boolean 		first 	= true;
		StringBuffer 	sb 		= new StringBuffer();
		Iterator<?> 	iterator= list.iterator();

		sb.append('[');
		while (iterator.hasNext()) 
		{
			if (first)
				first = false;
			else
				sb.append(',');

			Object value = iterator.next();
			if (value == null) 
			{
				sb.append("null");
				continue;
			}
			
			sb.append(toJSONString(value));
		}
		
		sb.append(']');
		
		return sb.toString();
	}

	/**
	 * Convert a map to JSON text. The result is a JSON object. If this map is
	 * also a JSONAware, JSONAware specific behaviours will be omitted at this
	 * top level.
	 * 
	 * @see org.json.simple.JSONValue#toJSONString(Object)
	 * 
	 * @param map
	 * @return JSON text, or "null" if map is null.
	 */
	private static String mapToJSONString(Map<?,?> map) 
	{
		if (map == null)
			return "null";

		StringBuffer	sb 		 = new StringBuffer();
		boolean 		first 	 = true;
		Iterator<?>		iterator = map.entrySet().iterator();

		sb.append('{');
		
		while (iterator.hasNext()) 
		{
			if (first)
				first = false;
			else
				sb.append(',');

			Map.Entry<?,?> entry = (Map.Entry<?,?>) iterator.next();
			toJSONString(String.valueOf(entry.getKey()), entry.getValue(), sb);
		}
		
		sb.append('}');
		
		return sb.toString();
	}

	private static String toJSONString(String key, Object value, StringBuffer sb) 
	{
		sb.append('\"');
		
		if (key == null)
			sb.append("null");
		else
			escape(key, sb);
		
		sb.append('\"').append(':');

		sb.append(toJSONString(value));

		return sb.toString();
	}

	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters
	 * (U+0000 through U+001F).
	 * 
	 * @param s
	 * @return
	 */
	private static String escape(String s) 
	{
		if (s == null)
			return null;
		
		StringBuffer sb = new StringBuffer();
		escape(s, sb);
		return sb.toString();
	}

	/**
	 * @param s
	 *            - Must not be null.
	 * @param sb
	 */
	private static void escape(String s, StringBuffer sb) 
	{
		for (int i = 0; i < s.length(); i++) 
		{
			char ch = s.charAt(i);
			switch (ch) {
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '/':
				sb.append("\\/");
				break;
			default:
				// Reference: http://www.unicode.org/versions/Unicode5.1.0/
				if ((ch >= '\u0000' && ch <= '\u001F')
						|| (ch >= '\u007F' && ch <= '\u009F')
						|| (ch >= '\u2000' && ch <= '\u20FF')) {
					String ss = Integer.toHexString(ch);
					sb.append("\\u");
					for (int k = 0; k < 4 - ss.length(); k++) {
						sb.append('0');
					}
					sb.append(ss.toUpperCase());
				} else {
					sb.append(ch);
				}
			}
		}
	}
}