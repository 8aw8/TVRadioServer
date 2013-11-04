package metadata.parsers;

import java.util.Calendar;
import java.util.Hashtable;

import util.reader.xml.XmlReader;
import util.reader.xml.XmlReaderDelegate;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;

public class RadioOrfeusMainParser extends MetadataParser implements XmlReaderDelegate 
{
	private boolean readingTime;
	private String time 	= null;
	private String author 	= null;
	private String title 	= null;

	private XmlReader xmlReader;
	
	private boolean isParsing;
	

	public RadioOrfeusMainParser(MetadataParserDelegate delegate) 
	{
		super(delegate, "www.fonotron.ru", 80);
		xmlReader = new XmlReader(this);
	}

	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://www.fonotron.ru/ HTTP/1.1\r\n" +
				"Host: www.fonotron.ru\r\n" +
				"Accept: */*\r\n" +
				"Accept-Language: ru\r\n" +
				"Pragma: no-cache\r\n" +
				"Connection: keep-alive\r\n" +
				"User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B179 Safari/7534.48.3\r\n" +
				"\r\n"
				).getBytes();
	}
	
	public void start()
	{
		super.start();
		xmlReader.reset();
		isParsing = true;
	}

	@Override
	protected void processPacket(byte[] packet) 
	{
		try {
			for(int i=0; i<packet.length && isParsing; i++)
				xmlReader.processByte(packet[i]);
		} catch (Exception e) {
			e.printStackTrace();
			// close
			this.nioSocket.close();
		}
	}

	//======================================
	// XmlReader delegate methods
	//======================================

	@Override
	public void readerDidStartElement(XmlReader reader, String elementName, Hashtable<String, String> attributes)	
	{
		readingTime = false;
		
		if("span".equals(elementName) && attributes!=null)
		{
			if("time".equals(attributes.get("class")))
				readingTime = true;
			else if("auth".equals(attributes.get("class")))
				author = attributes.get("title");
			else if("opuscont".equals(attributes.get("class")))
				title = attributes.get("title");
		}
	}

	@Override
	public void readerDidReadText(XmlReader reader, String text) { /* ignore */ }

	@Override
	public void readerDidEndElement(XmlReader reader, String elementName, String text)
	{ 	
		if(readingTime)
		{
			time = text;
			
			// getting time
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR_OF_DAY, TIMEZONE_HOUR_DELTA);

			String components[] = time.split(":");
			if(components.length >= 2)
			{
				int songHour   		= Integer.parseInt(components[0]);
				int songMinute 		= Integer.parseInt(components[1]);
				
				int currentHour 	= calendar.get(Calendar.HOUR_OF_DAY);
				int currentMinute 	= calendar.get(Calendar.MINUTE);
				
				if((currentHour == songHour && currentMinute < songMinute) || currentHour < songHour)
				{
					if (author!=null )  author = author.trim().replace(" \n", ",").replace("\n", "").replace("\r", "").replace("\t", "");
					if (title!=null )   title  = title.trim().replace(" \n", ",").replace("\n", "").replace("\r", "").replace("\t", "");
					
					// notify delegate
					if(delegate != null)
						delegate.didReceiveMetadataForStation(new Metadata("171", author, title));
					
					this.nioSocket.close();

					isParsing = false;
				}
			}

		}
	}
}
