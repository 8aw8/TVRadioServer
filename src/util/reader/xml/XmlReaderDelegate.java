package util.reader.xml;

import java.util.Hashtable;

public interface XmlReaderDelegate 
{
	public void readerDidStartElement(XmlReader reader, String elementName, Hashtable<String, String> attributes);
	public void readerDidReadText(XmlReader reader, String text);
	public void readerDidEndElement(XmlReader reader, String elementName, String text);
}
