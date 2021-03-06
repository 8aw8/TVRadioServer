package metadata.parsers;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;

public class MoskvaFMParser extends MetadataParser
{
	private static final int PARSE_STATE_NONE 					= 0;
	private static final int PARSE_STATE_START 					= 1;
	private static final int PARSE_STATE_OBJECT_FIRST_LINE 		= 2;
	private static final int PARSE_STATE_OBJECT_SECOND_LINE 	= 3;
	
	private int state;
	
	private String time;
	private String name;
	private String freq;
	private String artist;
	private String title;
	
	private Map<String,String> stationIdentificators;


	public MoskvaFMParser(MetadataParserDelegate delegate) 
	{
		super(delegate, "m.moskva.fm", 80);
		
		stationIdentificators = new HashMap<String, String>();
		stationIdentificators.put("Ретро FM", 			"188");
		stationIdentificators.put("Шансон", 			"189");
		stationIdentificators.put("Кекс FM", 			"190");
		stationIdentificators.put("Радио Romantika", 	"191");
		stationIdentificators.put("Восток FM", 			"202");
		stationIdentificators.put("Maximum", 			"176");
		stationIdentificators.put("Comedy Radio", 		"178");
		stationIdentificators.put("Radio Chocolate", 	"170");
		stationIdentificators.put("Мегаполис FM", 		"206");
		stationIdentificators.put("Милицейская волна", 	"23");
		stationIdentificators.put("ЮFM", 				"113");
		stationIdentificators.put("Серебряный дождь", 	"1");
		stationIdentificators.put("Весна FM", 			"244");
		stationIdentificators.put("Эхо Москвы", 		"164");
	}

	public void start()
	{
		super.start();
		this.nioSocket.setPacketReader(new naga.packetreader.AsciiLinePacketReader());
	}

	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://m.moskva.fm/onair HTTP/1.1\r\n" +
				"Host: m.moskva.fm\r\n" +
				"User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B179 Safari/7534.48.3\r\n" +
				"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
				"Cache-Control: max-age=0\r\n" +
				"Accept-Language: ru\r\n" +
				"Pragma: no-cache\r\n" +
				"Connection: keep-alive\r\n" +
				"Proxy-Connection: keep-alive\r\n" +
				"\r\n"
				).getBytes();
	}

	@Override
	protected void processPacket(byte[] packet) 
	{
		try {
			String line = new String(packet, "UTF-8").trim();
			
//			TVRadioServerMain.logger.info(line);
			
			switch(state)
			{
				case PARSE_STATE_NONE:
					if(line.startsWith("<h1>Сейчас в эфире</h1>"))
						state = PARSE_STATE_START;
					break;
					
				case PARSE_STATE_START:
					if(line.startsWith("<ul class=\"tabs primary stationlist\">"))
						state = PARSE_STATE_OBJECT_FIRST_LINE;
					break;

				case PARSE_STATE_OBJECT_FIRST_LINE:
					if(line.startsWith("<a href="))
					{
						state = PARSE_STATE_OBJECT_SECOND_LINE;
						
						int startIndex;
						int endIndex;
						
						time = null;
						name = null;
						freq = null;
						
						// get time
						startIndex 	= line.indexOf('>') + 1;
						endIndex 	= line.indexOf(' ', startIndex);
						
						if(startIndex > 0 && endIndex > -1)
						{
							time = line.substring(startIndex, endIndex);
						
							// get radio name
							startIndex 	= line.indexOf("<span class=\"station\">");
							if(startIndex > -1)
							{
								startIndex 	+= "<span class=\"station\">".length();
								endIndex	= line.indexOf("</span>", startIndex);
								
								if(startIndex > 0 && endIndex > -1)
									name = line.substring(startIndex, endIndex);
							
								// get radio frequency
								startIndex 	= line.indexOf("<span class=\"meta\">");
								if(startIndex > -1)
								{
									startIndex 	+= "<span class=\"meta\">".length();
									endIndex	= line.indexOf("</span>", startIndex);
									
									if(endIndex > -1)
										freq = line.substring(startIndex, endIndex);
								}
							}
						}
					}
					break;

				case PARSE_STATE_OBJECT_SECOND_LINE:
					if(line.startsWith("<span class=\"song\">"))
					{
						state = PARSE_STATE_START;
						
						int startIndex;
						int endIndex;
						
						artist = null;
						title  = null;
						
						// get artist name
						startIndex 	= line.indexOf("<span class=\"song\">");
						if(startIndex > -1)
						{
							startIndex  += "<span class=\"song\"><b>".length();
							endIndex	= line.indexOf("</b>", startIndex);
							
							if(startIndex>0 && endIndex>-1)
								artist = line.substring(startIndex, endIndex);
							
							// get song name
							startIndex 	= line.indexOf("<br /> ", endIndex);
							if(startIndex>-1)
							{
								startIndex 	+= "<br /> ".length();
								endIndex	= line.indexOf("</span>", startIndex);
								
								if(startIndex>0 && endIndex>-1)
									title = line.substring(startIndex, endIndex);
							}
						}
						
				//		TVRadioServerMain.logger.info("{"+time+", "+name+", "+freq+", "+artist+", "+title+"}");
						this.didReceiveStationInfo();
					}
					break;
			}	
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
			// can't parse current item, skip to next item
			state = PARSE_STATE_START;
			
//			TVRadioServerMain.logger.info("SKIPPING LINE...");

		}
	}

	
	private void didReceiveStationInfo()
	{
		// getting time
		Calendar calendar = Calendar.getInstance();
		
		// (!!!) this is not work
		//calendar.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
		calendar.add(Calendar.HOUR, TIMEZONE_HOUR_DELTA);

		Date now = calendar.getTime();

		String components[] = time.split(":");
		if(components.length >= 2)
		{
			int h = Integer.parseInt(components[0]);
			int m = Integer.parseInt(components[1]);
			
			calendar.add(Calendar.HOUR,   calendar.get(Calendar.HOUR_OF_DAY) - h);
			calendar.add(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - m);
		}
		
		Date endDate = calendar.getTime();
		
//		TVRadioServerMain.logger.info("now   "+now);
//		TVRadioServerMain.logger.info("end   "+endDate);


		if(endDate.getTime() - now.getTime() < 8 * 60 * 1000) // 8 mins
		{
			// print
//			TVRadioServerMain.logger.info("{"+time+", "+name+", "+freq+", "+artist+", "+title+"}");
			
			String stationId = stationIdentificators.get(name);
			if(stationId!=null)
				// notify delegate
				if(delegate != null)
					delegate.didReceiveMetadataForStation(new Metadata(stationId, artist, title));
		}
		else
			this.nioSocket.close();

	}
}
