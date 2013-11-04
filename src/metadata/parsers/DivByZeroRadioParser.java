package metadata.parsers;

import java.util.Hashtable;

import metadata.MetadataParser;
import metadata.MetadataParserDelegate;
import util.reader.xml.XmlReader;
import util.reader.xml.XmlReaderDelegate;

public class DivByZeroRadioParser extends MetadataParser implements XmlReaderDelegate 
{
	private XmlReader xmlReader;
	
	private boolean isParsing;
	
	private boolean isTableBlockFound;
	private boolean isInfoBlockFound;
	
	private String lastArtist = null;
	private String lastTitle = null;

	public DivByZeroRadioParser(MetadataParserDelegate delegate)
	{
		super(delegate, "divbyzero.de", 80);

		xmlReader = new XmlReader(this);
	}


	@Override
	protected byte[] getRequestBody() 
	{
		return (
				"GET http://divbyzero.de/pls.shtml HTTP/1.1\r\n" +
				"Host: divbyzero.de\r\n" +
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
		isTableBlockFound = false;
		isInfoBlockFound = false;
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
		app.TVRadioServerMain.logger.info("START TAG [" + elementName + "], ATTRS [" + attributes + "]");
		if(elementName.equals("table") && "mytab-pls".equals(attributes.get("id")))
		{
			isTableBlockFound = true;
		}
		else if(elementName.equals("font") && "-2".equals(attributes.get("size")) && attributes.size() == 1 && isTableBlockFound)
		{
			isInfoBlockFound = true;
		}

		// close
//		this.nioSocket.close();
	}

	@Override
	public void readerDidReadText(XmlReader reader, String text) { /* ignore */ }

	@Override
	public void readerDidEndElement(XmlReader reader, String elementName, String text)
	{ 	
//		TVRadioServerMain.logger.info("END TAG [" + elementName + "], TEXT [" + text + "]");
		if(elementName.equals("b") && isTableBlockFound && isInfoBlockFound)
		{
			lastArtist = text;
		}
		else if(elementName.equals("i") && isTableBlockFound && isInfoBlockFound)
		{
			lastTitle = text;
		}
		
		if(lastArtist != null && lastTitle != null)
		{
			app.TVRadioServerMain.logger.info(lastArtist + " :: " + lastTitle);
		}
	}
}
