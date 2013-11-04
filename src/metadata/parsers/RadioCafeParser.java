package metadata.parsers;

import java.io.UnsupportedEncodingException;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;

public class RadioCafeParser extends MetadataParser 
{	
	public RadioCafeParser(MetadataParserDelegate delegate)
	{
		super(delegate, "www.radio-cafe.ru", 80);
	}


	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://www.radio-cafe.ru/templates/radio-cafe/songinfo.php HTTP/1.1\r\n" +
				"Host: www.radio-cafe.ru\r\n" +
				"User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B179 Safari/7534.48.3\r\n" +
				"Accept: */*\r\n" +
				"Accept-Language: ru\r\n" +
				"X-Requested-With: XMLHttpRequest\r\n" +
				"\r\n"
						).getBytes();
	}

	@Override
	protected void processPacket(byte[] packet) 
	{
		try {
			String text = new String(packet, "WINDOWS-1251");
			
			String artist 	= null;
			String title 	= null;
			
			int startIndex = text.indexOf("\r\n\r\n");
			if(startIndex >= 0)
			{
				startIndex += "\r\n\r\n".length();
				startIndex = text.indexOf("\r\n", startIndex);
				if(startIndex >= 0)
				{
					startIndex += "\r\n".length();
					int endIndex = text.indexOf("\r\n0", startIndex);
					if(endIndex >= 0)
					{
						String song = text.substring(startIndex, endIndex);

						if(song != null && song.length() > 4)
						{
							String components[] = song.split(" - ", 2);
							if(components != null && components.length == 2)
							{
								artist = components[0];
								title  = components[1];
							}
						}

					}
				}
			}

			// notify delegate
			if(delegate != null)
				delegate.didReceiveMetadataForStation(new Metadata("196", artist, title));
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// close
		this.nioSocket.close();
	}
}
