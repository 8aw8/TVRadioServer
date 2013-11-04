package metadata.parsers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;
import util.reader.xml.XmlReader;
import util.reader.xml.XmlReaderDelegate;

public class TVRainParser extends MetadataParser implements XmlReaderDelegate 
{
	private class _ProgrammItem
	{
		public final Date startDate;
		public final Date endDate;
		public final String title;
		
		public _ProgrammItem(final Date startDate, final Date endDate, final String title)
		{
			this.startDate = startDate;
			this.endDate = endDate;
			this.title = title;
		}
	}
	
	private XmlReader xmlReader;
	private Date lastStartDate;
	private Date lastEndDate;
	private boolean isParsing;
	private List<_ProgrammItem> programm;

	public TVRainParser(MetadataParserDelegate delegate)
	{
		super(delegate, "tvrain.ru", 80);

		xmlReader = new XmlReader(this);
	}

	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://tvrain.ru/markersoft/current_week/  HTTP/1.1\r\n" +
				"Host: tvrain.ru\r\n" +
				"User-Agent: %D0%A2%D0%92%20%D0%94%D0%9E%D0%96%D0%94%D0%AC/1.2.1 CFNetwork/671 Darwin/14.0.0\r\n" +
				"Accept: */*\r\n" +
				"Accept-Language: ru\r\n" +
				"\r\n"
						).getBytes();
	}

	@Override
	public void start()
	{
		// search in cache, if no - load new
		if(!searchTitleForCurrentProgrammInCache())
		{
			super.start();
			xmlReader.reset();
			isParsing = true;
		}
	}

	@Override
	protected void processPacket(byte[] packet) 
	{
		try {
			for(int i=0; i<packet.length && isParsing; i++)
				xmlReader.processByte(packet[i]);
		} catch (Exception e) {
			// close
			this.nioSocket.close();
		}
	}
	
	private boolean searchTitleForCurrentProgrammInCache()
	{
		if(programm != null)
		{
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR, TIMEZONE_HOUR_DELTA);

			Date now = calendar.getTime();
			
			for(_ProgrammItem item : programm)
			{
				if(now.after(item.startDate) && now.before(item.endDate))
				{
					// notify delegate
					if(delegate != null)
						delegate.didReceiveMetadataForStation(new Metadata("329", null, item.title));
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	private Date parseDate(String dateStr) throws ParseException
	{
		return new SimpleDateFormat("yyyyMMddHHmmss").parse(dateStr.substring(0, 14));
	}

	//======================================
	// XmlReader delegate methods
	//======================================

	@Override
	public void readerDidStartElement(XmlReader reader, String elementName, Hashtable<String, String> attributes) 
	{
		if(elementName.equals("tv"))
		{
			programm = new ArrayList<_ProgrammItem>();
		}
		else if(elementName.equals("programme"))
		{
			try 
			{
				lastStartDate = parseDate(attributes.get("start")); 
				lastEndDate   = parseDate(attributes.get("stop")); 
			} 
			catch (ParseException e) { /* ignore */ }
		}
	}

	@Override
	public void readerDidReadText(XmlReader reader, String text) { /* ignore */ }

	@Override
	public void readerDidEndElement(XmlReader reader, String elementName, String text) 
	{
		if(elementName.equals("tv"))
		{
			// search in cache
			searchTitleForCurrentProgrammInCache();

			// close
			isParsing = false;
			this.nioSocket.close();
		}
		else if(elementName.equals("title"))
		{
			programm.add(new _ProgrammItem(lastStartDate, lastEndDate, text));
		}
	} 
}
