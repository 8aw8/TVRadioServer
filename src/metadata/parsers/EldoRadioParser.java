package metadata.parsers;

import metadata.MetadataParserDelegate;

public class EldoRadioParser extends Radio7Parser 
{	

	public EldoRadioParser(MetadataParserDelegate delegate)
	{
		super(delegate, "www.eldoradio.ru", 80, "227");
	}

	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://www.eldoradio.ru/online/air/eldo_playing.xml?uniq="+System.currentTimeMillis()+" HTTP/1.1\r\n" +
				"Host: www.eldoradio.ru\r\n" +
				"User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B179 Safari/7534.48.3\r\n" +
				"Accept: */*\r\n" +
				"Content-Type: application/x-www-form-urlencoded\r\n" +
				"Accept-Language: ru\r\n" +
				"X-Requested-With: XMLHttpRequest\r\n" +
				"\r\n"
						).getBytes();
	}
}
