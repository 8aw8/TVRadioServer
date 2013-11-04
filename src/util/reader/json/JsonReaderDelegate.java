package util.reader.json;

public interface JsonReaderDelegate 
{
	public void didStartJson();
	public void didEndJson();

	public void didStartObject();
	public void didEndObject();
	
	public void didStartArray();
	public void didEndArray();
	
	public void didReadKey(String key);
	public void didReadValue(Object value);
}
