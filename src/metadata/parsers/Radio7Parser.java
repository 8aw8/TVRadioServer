package metadata.parsers;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;
import util.reader.xml.XmlReader;
import util.reader.xml.XmlReaderDelegate;

public class Radio7Parser extends MetadataParser implements XmlReaderDelegate 
{	
	private String lastArtistName;
	private String lastSongName;
	private String lastStartTime;
	private String lastDuration;
	private String lastType;
	private String ourStationId;
	
	private boolean isParsing;
		
	private XmlReader xmlReader;

	public Radio7Parser(MetadataParserDelegate delegate)
	{
		this(delegate, "radio7.ru", 80, "9");
	}
	
	protected Radio7Parser(MetadataParserDelegate delegate, String host, int port, String ourStationId)
	{
		super(delegate, host, port);

		this.xmlReader 		= new XmlReader(this);
		this.ourStationId 	= ourStationId;
	}

	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://radio7.ru/online/air/cur_playing.xml?uniq="+System.currentTimeMillis()+" HTTP/1.1\r\n" +
				"Host: radio7.ru\r\n" +
				"User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B179 Safari/7534.48.3\r\n" +
				"Accept: */*\r\n" +
				"Content-Type: application/x-www-form-urlencoded\r\n" +
				"Accept-Language: ru\r\n" +
				"X-Requested-With: XMLHttpRequest\r\n" +
				"\r\n"
						).getBytes();
	}


	@Override
	protected void processPacket(byte[] packet) 
	{
		try {
			for(int i=0; i<packet.length; i++)
				xmlReader.processCP1251Byte(packet[i]);
		} catch (Exception e) {
			// close
			this.nioSocket.close();
		}
	}

	@Override
	public void start()
	{
		super.start();
		xmlReader.reset();
	}
	
	//======================================
	// XmlReader delegate methods
	//======================================

	@Override
	public void readerDidStartElement(XmlReader reader, String elementName, Hashtable<String, String> attributes) 
	{
		//TVRadioServerMain.logger.info(elementName + " " + attributes + " " + reader.getText());
		if(elementName.equals("ELEM") && attributes.get("STATUS").equals("playing"))
		{
			lastArtistName 	= null;
			lastSongName 	= null;
			lastStartTime	= null;
			lastDuration	= null;
			
			isParsing = true;
		}
	}

	@Override
	public void readerDidReadText(XmlReader reader, String text) { /* ignore */ }

	@Override
	public void readerDidEndElement(XmlReader reader, String elementName, String text) 
	{		
//		TVRadioServerMain.logger.info(elementName + " " + reader.getText());
		if(isParsing)
		{
			if(elementName.equals("START_TIME"))
				lastStartTime = reader.getText();
			else if(elementName.equals("NAME"))
			{
				lastSongName = reader.getText();
				if(lastSongName != null)
					lastSongName = lastSongName.replace('_', ' ');
			}
			else if(elementName.equals("ARTIST"))
			{
				lastArtistName = reader.getText();
				if(lastArtistName != null)
					lastArtistName = lastArtistName.replace('_', ' ');
			}
			else if(elementName.equals("DURATION"))
				lastDuration = reader.getText();
			else if(elementName.equals("TYPE"))
				lastType = reader.getText();
			// end
			else if(elementName.equals("ELEM"))
			{
				isParsing = false;
				
				// send null for jingles
				if((lastArtistName == null || lastArtistName.length() == 0) && lastSongName.startsWith("JINGLE"))// && !lastType.equals("М"))
				{
					lastArtistName = null;
					lastSongName = null;
				}
								
				// calculate next fetch time
				if(lastStartTime != null && lastDuration != null)
				{
					try {
						Calendar calendar = Calendar.getInstance();
						
						
						// (!!!) this is not work
						//calendar.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
						calendar.add(Calendar.HOUR, TIMEZONE_HOUR_DELTA);

						
						Date now = calendar.getTime();

						/*
						 * ������. 
						 * ������������ � ������� ������� ��� ���������� ������ � ������ � ��� ���.
						 */
						String components[] = lastStartTime.split(":");
						if(components.length >= 3)
						{
							int h = Integer.parseInt(components[0]);
							int m = Integer.parseInt(components[1]);
							int s = Integer.parseInt(components[2]);
							
							calendar.add(Calendar.HOUR_OF_DAY,  	h - calendar.get(Calendar.HOUR_OF_DAY));
							calendar.add(Calendar.MINUTE,		 	m - calendar.get(Calendar.MINUTE));
							calendar.add(Calendar.SECOND, 			s - calendar.get(Calendar.SECOND));
						}
						
						Date startDate = calendar.getTime();
						
						/*
						 * ������. 
						 * ������������ � ������� ������� ��� ���������� ������ � ������ � ��� ���.
						 */
						components = lastDuration.split(":");
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
						
//						TVRadioServerMain.logger.info("lastStartTime   "+lastStartTime);
//						TVRadioServerMain.logger.info("now   "+now);
//						TVRadioServerMain.logger.info("start "+startDate);
//						TVRadioServerMain.logger.info("end   "+endDate);

						if(now.after(endDate))
						{
							lastArtistName = null;
							lastSongName = null;
							lastFetchInterval = 15*1000;
						}
						else
						{
							lastFetchInterval = Math.min(endDate.getTime() - now.getTime() + 10 * 1000, 5*60*1000);
						}
					} 
					catch (Exception e) { /* ignore */ }
				}
				
				// notify delegate
				if(delegate != null)
					delegate.didReceiveMetadataForStation(new Metadata(ourStationId, lastArtistName, lastSongName));

				// close
				this.nioSocket.close();
			}
		}
	}
}
