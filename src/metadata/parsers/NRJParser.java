package metadata.parsers;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;
import util.reader.json.JsonReader;
import util.reader.json.JsonReaderDelegate;

public class NRJParser extends MetadataParser implements JsonReaderDelegate 
{
	private JsonReader jsonReader;
	private boolean isParsing;
	private boolean readingTitle;
	private boolean readingStart;
	private boolean readingDuration;
	private String title;
	private String start;
	private String duration;

	public NRJParser(MetadataParserDelegate delegate) 
	{
		super(delegate, "www.energyfm.ru", 80);
		jsonReader = new JsonReader(this);
	}

	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://www.energyfm.ru/x/nowefir_json.php?_="+System.currentTimeMillis()+" HTTP/1.1\r\n" +
				"Host: www.energyfm.ru\r\n" +
				"Accept: */*\r\n" +
				"Accept-Language: ru\r\n" +
				"Accept-Encoding: q=1.0, identity;q=0\r\n" +
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
	public void didEndJson() 	 { /*ignore*/ }
	public void didStartObject() { /*ignore*/ }
	public void didEndObject()   { /*ignore*/ }
	public void didStartArray()  { /*ignore*/ }
	public void didEndArray()    { /*ignore*/ }

	public void didReadKey(String key)
	{
		readingTitle 		= key.equals("title"); 
		readingStart 		= key.equals("start"); 
		readingDuration  	= key.equals("duration") || key.equals("rest"); 
	}

	public void didReadValue(Object value) 
	{
		if(readingTitle)
		{
			title = (String) value;
			
			String artist 		= null;
			String song   		= null;
			String components[] = title.split(" - ");
			
			if(components.length == 2)
			{
				artist = components[0];
				song   = components[1];
			}
			
			// notify delegate
			if(delegate != null)
				delegate.didReceiveMetadataForStation(new Metadata("14", artist, song));
		}
		else if(readingStart)
		{
			start = (String) value;
		}
		else if(readingDuration)
		{
			duration = (String) value;
			
			long st  = Long.parseLong(start);
			long dur = Long.parseLong(duration);
			
			lastFetchInterval = Math.min(Math.max((st + dur)*1000 - System.currentTimeMillis() + 10 * 1000, 15 * 1000), 5*60*1000);
			
			isParsing = false;
			this.nioSocket.close();
		}
	}
}
