package metadata.parsers;

import java.io.UnsupportedEncodingException;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;

public class PleerDotNasheParser extends MetadataParser {

	private final String stationId;
	private final String ourStationId;

	public PleerDotNasheParser(MetadataParserDelegate delegate, String stationId, String ourStationId) 
	{
		super(delegate, "pleer.nashe.ru", 80);
		
		this.stationId = stationId;
		this.ourStationId = ourStationId;
	}

	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://pleer.nashe.ru/info/"+stationId+".txt HTTP/1.1\r\n" +
				"Host: pleer.nashe.ru\r\n" +
				"Accept: */*\r\n" +
				"Accept-Language: ru\r\n" +
				"Pragma: no-cache\r\n" +
				"Connection: keep-alive\r\n" +
				"User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B179 Safari/7534.48.3\r\n" +
				"\r\n"
				).getBytes();
	}

	@Override
	protected void processPacket(byte[] packet) 
	{
		try {
			String text = new String(packet, "UTF-8");
			
			String artist = null;
			String song   = null;
			
			int artistStartIndex = text.indexOf("{\"artist\":\"");
			if(artistStartIndex >= 0)
			{
				artistStartIndex += "{\"artist\":\"".length();
				
				int artistEndIndex = text.indexOf("\",", artistStartIndex);
				if(artistEndIndex >= 0)
				{
					artist = text.substring(artistStartIndex, artistEndIndex);
					
					int songStartIndex = text.indexOf("\"song\":\"", artistEndIndex);
					if(songStartIndex >= 0)
					{
						songStartIndex += "\"song\":\"".length();
						
						int songEndIndex = text.indexOf("\",", songStartIndex);
						if(songEndIndex >= 0)
						{
							song = text.substring(songStartIndex, songEndIndex);
							
							// notify delegate
							if(delegate != null)
								delegate.didReceiveMetadataForStation(new Metadata(ourStationId, artist, song));
						}
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.nioSocket.close();
	}

}
