package metadata.parsers;

import java.util.Hashtable;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;
import util.reader.xml.XmlReader;
import util.reader.xml.XmlReaderDelegate;

public class YerevanNightsRadioParser extends MetadataParser implements XmlReaderDelegate {	
	private XmlReader xmlReader;
	
	private boolean isParsing;

	public YerevanNightsRadioParser(MetadataParserDelegate delegate)
	{
		super(delegate, "www.yerevannights.com", 80);

		xmlReader = new XmlReader(this);
	}


	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://www.yerevannights.com/includes/xml/now_playing.xml?cachebuster="+System.currentTimeMillis()+" HTTP/1.1\r\n" +
				"Host: www.yerevannights.com\r\n" +
				"User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/536.26.17 (KHTML, like Gecko) Version/6.0.2 Safari/536.26.17\r\n" +
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
//		TVRadioServerMain.logger.info(elementName + " " + attributes);
		if(elementName.equals("row"))
		{
			isParsing = false;
			
			String artist 	= attributes.get("Artist");
			String title 	= attributes.get("Title");

			// notify delegate
			if(delegate != null)
				delegate.didReceiveMetadataForStation(new Metadata("297", artist, title));
		}

		// close
		this.nioSocket.close();
	}

	@Override
	public void readerDidReadText(XmlReader reader, String text) { /* ignore */ }

	@Override
	public void readerDidEndElement(XmlReader reader, String elementName, String text) { /* ignore */ }
}
