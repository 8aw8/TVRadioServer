package metadata.parsers;

import java.util.Hashtable;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;
import util.reader.xml.XmlReader;
import util.reader.xml.XmlReaderDelegate;

public class RadioJazzParser extends MetadataParser implements XmlReaderDelegate {	
	private XmlReader xmlReader;
	private String ourStationId;
	private String link;
	
	private boolean isParsing;

	public RadioJazzParser(MetadataParserDelegate delegate)
	{
		this(delegate, "199", "http://www.cultandart.ru/radiojazz/play_jazz.xml");
	}

	protected RadioJazzParser(MetadataParserDelegate delegate, String ourStationId, String link)
	{
		super(delegate, "cultandart.ru", 80);

		this.ourStationId 	= ourStationId;
		this.link			= link;
		this.xmlReader 		= new XmlReader(this);
	}

	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET "+link+"?0."+System.currentTimeMillis()+" HTTP/1.1\r\n" +
				"Host: cultandart.ru\r\n" +
				"User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B179 Safari/7534.48.3\r\n" +
				"Accept: */*\r\n" +
				"Accept-Language: ru\r\n" +
				"X-Requested-With: XMLHttpRequest\r\n" +
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
	public void readerDidStartElement(XmlReader reader, String elementName, Hashtable<String, String> attributes) { /* ignore */ }

	@Override
	public void readerDidReadText(XmlReader reader, String text) { /* ignore */ }

	@Override
	public void readerDidEndElement(XmlReader reader, String elementName, String text) 
	{
//		TVRadioServerMain.logger.info(elementName + " " + attributes + " " + reader.getText());
		if(elementName.equals("item"))
		{
			isParsing = false;
			
			String artist 	= null;
			String title 	= null;
			String song 	= reader.getAttributeValueForKey("song");
			
			if(song != null && song.length() > 4)
			{
				String components[] = song.split(" - ", 2);
				if(components != null && components.length == 2)
				{
					artist = components[0];
					title  = components[1];
				}
			}

			// notify delegate
			if(delegate != null)
				delegate.didReceiveMetadataForStation(new Metadata(ourStationId, artist, title));

			// close
			this.nioSocket.close();
		}
	}
}
