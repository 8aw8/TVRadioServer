package metadata.parsers;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;

public class Radio4DukParser extends MetadataParser {

	public Radio4DukParser(MetadataParserDelegate delegate) 
	{
		super(delegate, "www.4duk.ru", 80);
	}

	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"POST http://www.4duk.ru/4duk/whatsPlaying.action HTTP/1.1\r\n" +
				"Host: www.4duk.ru\r\n" +
				"Accept: */*\r\n" +
				"Accept-Language: ru\r\n" +
				"Pragma: no-cache\r\n" +
				"Connection: keep-alive\r\n" +
				"User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/536.26.17 (KHTML, like Gecko) Version/6.0.2 Safari/536.26.17\r\n" +
				"Cookie: JSESSIONID=071D670921FE5020479A0AE8F5160323; JSESSIONID=D19D6F7F0902C9E40202023890E77054; style_cookie=null; __utma=59122088.1714603844.1362379482.1367352257.1370427166.6; __utmb=59122088.3.10.1370427166; __utmc=59122088; __utmz=59122088.1363097884.2.2.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); chipl_dukh_k=; chipl_dukh_sid=1913bbfb04d1986520887566757d528e; chipl_dukh_u=1\r\n" +
				"Content-Length: 0\r\n" +
				"Origin: http://www.4duk.ru\r\n" +
				"Referer: http://www.4duk.ru/4duk/radio256.html\r\n" +
				"\r\n"
				).getBytes();
	}

	@Override
	protected void processPacket(byte[] packet) 
	{
		try {
			String text = new String(packet, "UTF-8");
			
//			TVRadioServerMain.logger.info(text);
			
			String artist 					= null;
			String title   					= null;
			String millisUntilNextRequest 	= null;
			
			int titleStartIndex = text.indexOf("title : '");
			if(titleStartIndex >= 0)
			{
				titleStartIndex += "title : '".length();
				
				int titleEndIndex = text.indexOf("',", titleStartIndex);
				if(titleEndIndex >= 0)
				{
					title = text.substring(titleStartIndex, titleEndIndex);
					
					int artistIndex = text.indexOf("artist : '", titleEndIndex);
					if(artistIndex >= 0)
					{
						artistIndex += "artist : '".length();
						
						int artistEndIndex = text.indexOf("',", artistIndex);
						if(artistEndIndex >= 0)
						{
							artist = text.substring(artistIndex, artistEndIndex);

							// notify delegate
							if(delegate != null)
								delegate.didReceiveMetadataForStation(new Metadata("316", artist, title));

							int durationStartIndex = text.indexOf("millisUntilNextRequest : ", artistEndIndex);
							if(durationStartIndex >= 0)
							{
								durationStartIndex += "millisUntilNextRequest : ".length();
								
								int durationEndIndex = text.indexOf(",", durationStartIndex);
								if(durationEndIndex >= 0)
								{
									millisUntilNextRequest = text.substring(durationStartIndex, durationEndIndex);
									
									long duration = Long.parseLong(millisUntilNextRequest);
									
									if(duration > 0)
										lastFetchInterval = Math.min(duration, 6*60*1000);

								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.nioSocket.close();
	}
}
