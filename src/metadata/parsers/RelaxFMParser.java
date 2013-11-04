package metadata.parsers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;
import util.reader.xml.XmlReader;
import util.reader.xml.XmlReaderDelegate;

public class RelaxFMParser extends MetadataParser implements XmlReaderDelegate {	
	private XmlReader xmlReader;
	
	private boolean isParsing;

	public RelaxFMParser(MetadataParserDelegate delegate)
	{
		super(delegate, "relax-fm.ru", 80);

		xmlReader = new XmlReader(this);
	}


	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://www.relax-fm.ru/xml/playlist/current_song.xml HTTP/1.1\r\n" +
				"Host: relax-fm.ru\r\n" +
				"User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B179 Safari/7534.48.3\r\n" +
				"Accept: */*\r\n" +
				"Accept-Language: ru\r\n" +
				"\r\n"
						).getBytes();
	}

	@Override
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
				xmlReader.processCP1251Byte(packet[i]);
		} catch (Exception e) {
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
//		TVRadioServerMain.logger.info(elementName + " " + attributes + " " + reader.getText());
		if(elementName.equals("result") && attributes.get("name").equals("song"))
		{
			isParsing = false;
			
			String artist 		= attributes.get("artist");
			String title 		= attributes.get("title");
			String startDateStr = attributes.get("start");
			String durationStr 	= attributes.get("length");
			
			if(startDateStr != null && durationStr != null)
			{
				try {
					Date startDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(startDateStr);
					
					Calendar calendar = Calendar.getInstance();
					
					
					// (!!!) this is not work
					//calendar.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
					calendar.add(Calendar.HOUR, TIMEZONE_HOUR_DELTA);

					
					Date now = calendar.getTime();

					/*
					 * ������. 
					 * ������������ � ������� ������� ��� ���������� ������ � ������ � ��� ���.
					 */
					String components[] = durationStr.split(":");
					if(components.length >= 3)
					{
						int h = Integer.parseInt(components[0]);
						int m = Integer.parseInt(components[1]);
						int s = Integer.parseInt(components[2]);
						
						calendar.setTime(startDate);
						
						calendar.add(Calendar.HOUR_OF_DAY,   h);
						calendar.add(Calendar.MINUTE, m);
						calendar.add(Calendar.SECOND, s);
					}
					
					Date endDate = calendar.getTime();
					
//					TVRadioServerMain.logger.info("now   "+now);
//					TVRadioServerMain.logger.info("start "+startDate);
//					TVRadioServerMain.logger.info("end   "+endDate);

					if(now.after(endDate))
					{
						artist = null;
						title  = null;
						lastFetchInterval = 15*1000;
					}
					else
					{
						lastFetchInterval = Math.min(endDate.getTime() - now.getTime() + 10 * 1000, 5*60*1000);
					}
				} 
				catch (ParseException e) { /* ignore */ }
			}

			// notify delegate
			if(delegate != null)
				delegate.didReceiveMetadataForStation(new Metadata("172", artist, title));

			// close
			this.nioSocket.close();
		}
	}

	@Override
	public void readerDidReadText(XmlReader reader, String text) { /* ignore */ }

	@Override
	public void readerDidEndElement(XmlReader reader, String elementName, String text) { /* ignore */ } 
}
