package metadata.parsers;

import util.reader.json.JsonReader;
import util.reader.json.JsonReaderDelegate;
import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;

public class AutoradioParser extends MetadataParser implements JsonReaderDelegate 
{
	private JsonReader jsonReader;
	private boolean isParsing;
	private boolean readingTitle;
	private boolean readingStart;
	private boolean readingRest;
	private String title;
	private String start;
	private String rest;

	public AutoradioParser(MetadataParserDelegate delegate) 
	{
		super(delegate, "www.avtoradio.ru", 80);
		jsonReader = new JsonReader(this);
	}

	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://www.avtoradio.ru/x/nowefir_json.php?_="+System.currentTimeMillis()+" HTTP/1.1\r\n" +
				"Host: www.avtoradio.ru\r\n" +
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
			jsonReader.processCP1251Byte(packet[i]);
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
		readingTitle = key.equals("title"); 
		readingStart = key.equals("start"); 
		readingRest  = key.equals("rest"); 
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
				delegate.didReceiveMetadataForStation(new Metadata("8", artist, song));
		}
		else if(readingStart)
		{
			start = (String) value;
		}
		else if(readingRest)
		{
			rest = (String) value;
			
			long st  = Long.parseLong(start);
			long dur = Long.parseLong(rest);
			
			lastFetchInterval = Math.min(Math.max((st + dur)*1000 - System.currentTimeMillis() + 10 * 1000, 15 * 1000), 5*60*1000);
			
			isParsing = false;
			this.nioSocket.close();
		}
	}
	
	
	
	
	
	
	protected void processPacket1(byte[] packet) 
	{
		try {
			String text = new String(packet, "WINDOWS-1251");
			
			String artist 		= null;
			String song   		= null;
			String startTime 	= null;
			String duration 	= null;
			
			int titleStartIndex = text.indexOf("\"title\":\"");
			if(titleStartIndex >= 0)
			{
				titleStartIndex += "\"title\":\"".length();
				
				int titleEndIndex = text.indexOf("\",", titleStartIndex);
				if(titleEndIndex >= 0)
				{
					String title = text.substring(titleStartIndex, titleEndIndex);
					
					String components[] = title.split(" - ");
					if(components.length == 2)
					{
						artist = components[0];
						song = components[1];
					}
					
					// notify delegate
					if(delegate != null)
						delegate.didReceiveMetadataForStation(new Metadata("8", artist, song));
					
					int startTimeIndex = text.indexOf("\"start\":\"", titleEndIndex);
					if(startTimeIndex >= 0)
					{
						startTimeIndex += "\"start\":\"".length();
						
						int startTimeEndIndex = text.indexOf("\",", startTimeIndex);
						if(startTimeEndIndex >= 0)
						{
							startTime = text.substring(startTimeIndex, startTimeEndIndex);
							
							int durationStartIndex = text.indexOf("\"rest\":\"", startTimeEndIndex);
							if(durationStartIndex >= 0)
							{
								durationStartIndex += "\"rest\":\"".length();
								
								int durationEndIndex = text.indexOf("\"", durationStartIndex);
								if(durationEndIndex >= 0)
								{
									duration = text.substring(durationStartIndex, durationEndIndex);
									
									long start = Long.parseLong(startTime);
									long dur = Long.parseLong(duration);
									
									lastFetchInterval = Math.min(Math.max((start + dur)*1000 - System.currentTimeMillis() + 10 * 1000, 15 * 1000), 5*60*1000);

									//TVRadioServerMain.logger.info("\nNOW :"+System.currentTimeMillis()+"\nTIME:"+(start + dur)*1000+"\nFTCH:"+lastFetchInterval);
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
