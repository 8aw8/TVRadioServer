package metadata.parsers;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;
import util.reader.json.JsonReader;
import util.reader.json.JsonReaderDelegate;

public class RadioOrfeusOtherParser extends MetadataParser implements JsonReaderDelegate 
{
	private JsonReader jsonReader;
	private final String remoteChannelId;
	private final String stationId;
	private boolean isParsing;
	private boolean readingAuthor;
	private boolean readingArtist;
	private boolean readingTitle;
	private String author;
	private String artist;
	private String title;
	
	public RadioOrfeusOtherParser(MetadataParserDelegate delegate, String remoteChannelId, String stationId) 
	{
		super(delegate, "www.fonotron.ru", 80);
		
		this.remoteChannelId = remoteChannelId;
		this.stationId = stationId;
		
		jsonReader = new JsonReader(this);
	}
	
	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://www.fonotron.ru/ajplayer/?action=currtrack&channel="+remoteChannelId+" HTTP/1.1\r\n" +
				"Host: "+host+"\r\n" +
				"Accept: */*\r\n" +
				"Accept-Language: ru\r\n" +
				"Pragma: no-cache\r\n" +
				"Connection: keep-alive\r\n" +
				"User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B179 Safari/7534.48.3\r\n" +
				"\r\n"
				).getBytes();
	}

	@Override
	public void start()
	{
		super.start();
		jsonReader.reset();
		isParsing = true;
	}

	@Override
	protected void processPacket(byte[] packet) 
	{
		for(int i=0; i<packet.length && isParsing; i++)
			jsonReader.processByte(packet[i]);
	}

	//======================================
	// JsonReader delegate methods
	//======================================

	public void didStartJson() 	 { /*ignore*/ }
	public void didStartObject() { /*ignore*/ }
	public void didStartArray()  { /*ignore*/ }
	public void didEndArray()    { /*ignore*/ }

	public void didReadKey(String key)
	{
		readingAuthor = key.equals("author"); 
		readingArtist = key.equals("artist"); 
		readingTitle  = key.equals("title"); 
	}

	public void didReadValue(Object value) 
	{
		if(readingAuthor) 
			author = (String) value;
			
		if(readingArtist) 
			artist = (String) value;
			
		if(readingTitle) 
			title = (String) value;
	}
	
	public void didEndObject()   
	{
		if(util.Util.notEmpty(author) || util.Util.notEmpty(artist) ||  util.Util.notEmpty(title))
		{
			StringBuffer author_artist = new StringBuffer();
			
			if(util.Util.notEmpty(author) && !"null".equals(author))
				author_artist.append(author);
			
			if(util.Util.notEmpty(artist) && !"null".equals(artist))
			{
				if(util.Util.notEmpty(author) && !"null".equals(author))
					author_artist.append(" / ");
				author_artist.append(artist);
			}
			
			// notify delegate
			if(delegate != null)
				delegate.didReceiveMetadataForStation(new Metadata(stationId, author_artist.toString(), title));
			
			isParsing = false;
			this.nioSocket.close();
		}
	}
	
	public void didEndJson() 	 
	{
		// end
		isParsing = false;
		this.nioSocket.close();
	}
}
