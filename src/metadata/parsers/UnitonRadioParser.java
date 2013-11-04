package metadata.parsers;

import java.util.Hashtable;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;
import util.reader.xml.XmlReader;
import util.reader.xml.XmlReaderDelegate;

public class UnitonRadioParser extends MetadataParser implements XmlReaderDelegate {	
	private XmlReader xmlReader;
	
	private boolean isParsing;

	public UnitonRadioParser(MetadataParserDelegate delegate)
	{
		super(delegate, "www.r-uniton.ru", 80);

		xmlReader = new XmlReader(this);
	}


	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://www.r-uniton.ru/onair/song/ HTTP/1.1\r\n" +
				"Host: www.r-uniton.ru\r\n" +
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
//		TVRadioServerMain.logger.info("\n\n\n=============\n" + new String(packet) + "\n=============\n");
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
	public void readerDidStartElement(XmlReader reader, String elementName, Hashtable<String, String> attributes) { /* ignore */ }

	@Override
	public void readerDidReadText(XmlReader reader, String text) 
	{
		if(text.startsWith("Сейчас в эфире: "))
		{
			isParsing = false;
			
			String artist 	= null;
			String title 	= null;
			String song 	= text.substring("Сейчас в эфире: ".length());
			
			if(song != null && song.length() > 4)
			{
				String components[] = song.split(" - ", 2);
				if(components != null && components.length == 2)
				{
					artist = components[0];
					title  = components[1];
				}
				else
					title = song;
			}

			// notify delegate
			if(delegate != null)
				delegate.didReceiveMetadataForStation(new Metadata("265", artist, title));
		}

		// close
		this.nioSocket.close();
	}

	@Override
	public void readerDidEndElement(XmlReader reader, String elementName, String text) { /* ignore */ }
}
