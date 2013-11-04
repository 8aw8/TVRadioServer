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

public class StationRUParser extends MetadataParser implements XmlReaderDelegate 
{	
	private String lastArtistName;
	private String lastSongName;
	private String lastStartTime;
	private String lastDuration;
	
	private XmlReader xmlReader;
	
	private final String stationRuId;
	private final String ourStationId;
	
	public StationRUParser(MetadataParserDelegate delegate, String stationRuId, String ourStationId) 
	{
		super(delegate, "station.ru", 80);
		
		this.stationRuId  = stationRuId;
		this.ourStationId = ourStationId;
		
		xmlReader = new XmlReader(this);
	}

	@Override
	protected byte[] getRequestBody()
	{
		byte bodyBytes[] = 
				(
				"<?xml version=\"1.0\"?>" +
					"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:StationServiceSvc=\"http://tempuri.org/\" xmlns:ns1=\"http://tempuri.org/Imports\" xmlns:tns1=\"http://schemas.datacontract.org/2004/07/Quantumart.Station\" xmlns:tns2=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\" xmlns:tns3=\"http://schemas.microsoft.com/2003/10/Serialization/\" xsl:version=\"1.0\">" +
						"<soap:Body>" +
							"<StationServiceSvc:GetCurrentStationTrack>" +
								"<StationServiceSvc:stationId>"+stationRuId+"</StationServiceSvc:stationId>" +
							"</StationServiceSvc:GetCurrentStationTrack>" +
						"</soap:Body>" +
					"</soap:Envelope>"
						).getBytes();
		
		byte headerBytes[] =
				(
				"POST http://station.ru/services/StationService.svc HTTP/1.1\r\n" +
				"Host: station.ru\r\n" +
				"Accept: */*\r\n" +
				"Content-Type: text/xml; charset=utf-8\r\n" +
				"Content-Length: "+(bodyBytes.length)+"\r\n" +
				"Accept-Language: ru\r\n" +
				"SOAPAction: http://tempuri.org/IStationService/GetCurrentStationTrack\r\n" +
				"Pragma: no-cache\r\n" +
				"Connection: keep-alive\r\n" +
				"User-Agent: wsdl2objc\r\n" +
				"\r\n").getBytes();
		
		byte packetBytes[] = new byte[headerBytes.length + bodyBytes.length];
				
		System.arraycopy(headerBytes, 0, packetBytes, 0, headerBytes.length);
		System.arraycopy(bodyBytes, 0, packetBytes, headerBytes.length, bodyBytes.length);
		
		return packetBytes;
	}

	@Override
	protected void processPacket(byte[] packet) 
	{
		try {
			for(int i=0; i<packet.length; i++)
				xmlReader.processByte(packet[i]);
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
		if(elementName.equals("GetCurrentStationTrackResult"))
		{
			lastArtistName 	= null;
			lastSongName 	= null;
			lastStartTime	= null;
			lastDuration	= null;
		}
	}

	@Override
	public void readerDidReadText(XmlReader reader, String text) { /* ignore */ }

	@Override
	public void readerDidEndElement(XmlReader reader, String elementName, String text) 
	{
//		TVRadioServerMain.logger.info(elementName + " " + attributes + " " + reader.getText());

		if(elementName.equals("a:StartTime"))
			lastStartTime = reader.getText();
		else if(elementName.equals("a:Song"))
			lastSongName = reader.getText();
		else if(elementName.equals("a:Artist"))
			lastArtistName = reader.getText();
		else if(elementName.equals("a:Duration"))
			lastDuration = reader.getText();
		// end
		else if(elementName.equals("GetCurrentStationTrackResult"))
		{
			// calculate next fetch time
			lastFetchInterval = DEFAULT_FETCH_INTERVAL;
			if(lastStartTime != null && lastDuration != null)
			{
				try {
					Date startDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(lastStartTime);
					
					Calendar calendar = Calendar.getInstance();
					
					
					// (!!!) this is not work
					//calendar.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
					calendar.add(Calendar.HOUR, TIMEZONE_HOUR_DELTA);
					
					
					Date now = calendar.getTime();

					int s = Integer.parseInt(lastDuration);
					calendar.setTime(startDate);
					calendar.add(Calendar.SECOND, s);
					
					Date endDate = calendar.getTime();
					
//					TVRadioServerMain.logger.info("now   "+now);
//					TVRadioServerMain.logger.info("start "+startDate);
//					TVRadioServerMain.logger.info("end   "+endDate);

					if(now.after(endDate))
					{
						lastArtistName = null;
						lastSongName = null;
						lastFetchInterval = 30*1000;
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
				delegate.didReceiveMetadataForStation(new Metadata(ourStationId, lastArtistName, lastSongName));

			// close
			this.nioSocket.close();
		}
	}
}
