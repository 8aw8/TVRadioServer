package metadata.parsers;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;

public class FinamFMParser extends MetadataParser {

	public FinamFMParser(MetadataParserDelegate delegate) 
	{
		super(delegate, "finam.fm", 80);
	}

	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://finam.fm/s/0/onair.js?_="+System.currentTimeMillis()+" HTTP/1.1\r\n" +
				"Host: finam.fm\r\n" +
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
			
			String artist 		= null;
			String song   		= null;
			
			int artistStartIndex = text.indexOf("href=");
			int artistEndIndex   = 0;
			if(artistStartIndex >= 0)
			{
				artistStartIndex += "href=".length();
				artistStartIndex = text.indexOf(">", artistStartIndex);
				if(artistStartIndex >= 0)
				{
					artistStartIndex += ">".length();
					
					artistEndIndex = text.indexOf("<", artistStartIndex);
					if(artistEndIndex >= 0)
						artist = text.substring(artistStartIndex, artistEndIndex);
				}
			}

			int songStartIndex = text.indexOf("href=", artistEndIndex);
			if(songStartIndex >= 0)
			{
				songStartIndex += "href=".length();
				songStartIndex = text.indexOf(">", songStartIndex);
				if(songStartIndex >= 0)
				{
					songStartIndex += ">".length();
					
					int songEndIndex = text.indexOf("<", songStartIndex);
					if(songEndIndex >= 0)
						song = text.substring(songStartIndex, songEndIndex);
				}
			}
			
			// notify delegate
			if(delegate != null)
				delegate.didReceiveMetadataForStation(new Metadata("6", artist, song));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.nioSocket.close();
	}
}
