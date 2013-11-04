package metadata.parsers;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;

public class UnistarParser extends MetadataParser {

	public UnistarParser(MetadataParserDelegate delegate) 
	{
		super(delegate, "unistar.by", 80);
	}

	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://unistar.by/include/now_play.php HTTP/1.1\r\n" +
				"Host: unistar.by\r\n" +
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
			String text = new String(packet, "WINDOWS-1251");
			
			String artist 		= null;
			String song   		= null;
			
			int artistStartIndex = text.indexOf("<strong class=\"title\">");
			int artistEndIndex   = 0;
			if(artistStartIndex >= 0)
			{
				artistStartIndex += "<strong class=\"title\">".length();
				artistEndIndex = text.indexOf("<", artistStartIndex);
				if(artistEndIndex >= 0)
					artist = text.substring(artistStartIndex, artistEndIndex);
			}

			int songStartIndex = text.indexOf("<br />", artistEndIndex);
			if(songStartIndex >= 0)
			{
				songStartIndex += "<br />".length();
				int songEndIndex = text.indexOf("<", songStartIndex);
				if(songEndIndex >= 0)
					song = text.substring(songStartIndex, songEndIndex);
			}
			
			// notify delegate
			if(delegate != null)
				delegate.didReceiveMetadataForStation(new Metadata("345", artist, song));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.nioSocket.close();
	}
}
