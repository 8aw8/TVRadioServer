package metadata.parsers;

import java.util.Hashtable;

import metadata.Metadata;
import metadata.MetadataParser;
import metadata.MetadataParserDelegate;
import util.reader.xml.XmlReader;
import util.reader.xml.XmlReaderDelegate;

public class AfterhoursFMParser extends MetadataParser implements XmlReaderDelegate {	
	private XmlReader xmlReader;
	
	private boolean isParsing;

	public AfterhoursFMParser(MetadataParserDelegate delegate)
	{
		super(delegate, "www.ah.fm", 80);

		xmlReader = new XmlReader(this);
	}


	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://www.ah.fm/header/getHeaderData.php HTTP/1.1\r\n" +
				"Host: www.ah.fm\r\n" +
				"User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/536.26.17 (KHTML, like Gecko) Version/6.0.2 Safari/536.26.17\r\n" +
				"Accept: application/xml, text/xml, */*; q=0.01\r\n" +
				"Referer: http://www.ah.fm/forum/\r\n" +
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
		if(elementName.equals("now_playing"))
		{
			isParsing = false;
			
			String artist 	= null;
			String title 	= null;
			String song 	= reader.getText();
			
			if(song != null && song.length() > 4)
			{
				String components[] = song.split(" - ", 2);
				if(components != null && components.length == 2)
				{
					artist = components[0];
					title  = components[1];
					
					String titleComponents[] = title.split("on AH.FM");
					if(titleComponents != null && titleComponents.length >= 1)
						title = titleComponents[0];
				}
			}

			// notify delegate
			if(delegate != null)
				delegate.didReceiveMetadataForStation(new Metadata("201", artist, title));

			// close
			this.nioSocket.close();
		}
	}
}
