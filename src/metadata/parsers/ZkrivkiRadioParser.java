package metadata.parsers;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;
import util.reader.json.JsonReader;
import util.reader.json.JsonReaderDelegate;

public class ZkrivkiRadioParser extends MetadataParser implements JsonReaderDelegate 
{
	private JsonReader jsonReader;
	private boolean isParsing;
	private boolean currentFound;
	private boolean readingArtist;
	private boolean readingSong;
	private boolean readingStatus;
	private String artist;
	private String song;
	
	public ZkrivkiRadioParser(MetadataParserDelegate delegate) 
	{
		super(delegate, "mjoy.ua", 80);
		jsonReader = new JsonReader(this);
	}
	
	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://mjoy.ua/radio/station/rzk/playlist.json HTTP/1.1\r\n" +
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
		currentFound = false;
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
	public void didEndObject()   { /*ignore*/ }
	public void didStartArray()  { /*ignore*/ }
	public void didEndArray()    { /*ignore*/ }

	public void didReadKey(String key)
	{
		readingArtist = key.equals("artist"); 
		readingSong   = key.equals("title"); 
		readingStatus = key.equals("status");
	}

	public void didReadValue(Object value) 
	{
		if(readingArtist)
			artist = (String) value;
		else if(readingSong)
			song = (String) value;
		else if(readingStatus)
			currentFound = value.equals("current");

		if(currentFound && artist!=null && song!=null)
		{
			// notify delegate
			if(delegate != null)
				delegate.didReceiveMetadataForStation(new Metadata("349", artist, song));
			
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
