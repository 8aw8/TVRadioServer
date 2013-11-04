package util.reader.json;

import util.reader.CharReader;
import util.reader.CharReaderDelegate;

public class JsonReader implements CharReaderDelegate
{
    private JsonReaderDelegate delegate;
    private CharReader charReader;
    
    private StringBuffer stringChars;
    private StringBuffer characterChars;

    private boolean isReadingString;
    private boolean isReadingChar;
    
    private int objectCount = 0;
    private int arrayCount = 0;
    
    public JsonReader(JsonReaderDelegate delegate)
    {
    	this.delegate 	= delegate;
    	this.charReader = new CharReader(this);
    }

    public void processCP1251Byte(int b)
    {
    	charReader.processCP1251Byte(b);
    }
    
 	public void processByte(int b) 
 	{
 		charReader.processByte(b);
	}
 	
 	public void reset()
 	{
 		charReader.reset();
 		
 	    stringChars = null;
 	    characterChars = null;

 	    isReadingString = false;
 	    isReadingChar = false;
 	    
 	    objectCount = 0;
 	    arrayCount = 0;
 	}
 	
 	private void checkIfValueReadAndNotify()
 	{
		if(stringChars != null)
		{
			delegate.didReadValue(stringChars.toString());
			stringChars = null;
		}
 	}
 	
	@Override
	public void didReadChar(char ch) 
	{
		if(arrayCount == 0 && objectCount == 0 && ch != '{' && ch != '[')
			return;
			
		if(isReadingString && (ch!='"' || (isReadingChar && ch=='"')))
		{
			if(!isReadingChar && ch == '\\')
			{
				isReadingChar = true;
			}
			else if(isReadingChar)
			{
				if(characterChars != null)
				{
					if((characterChars.append(ch)).length() == 4)
					{
						stringChars.append((char) Integer.parseInt(characterChars.toString(), 16));
						characterChars = null;
						isReadingChar = false;
					}
				}
				else
				{
					switch(ch)
					{
						case '"' : stringChars.append('"');  isReadingChar = false; break;
						case '\\': stringChars.append('\\'); isReadingChar = false; break;
						case '/' : stringChars.append('/');  isReadingChar = false; break;
						case 'b' : stringChars.append('\b'); isReadingChar = false; break;
						case 'f' : stringChars.append('\f'); isReadingChar = false; break;
						case 'n' : stringChars.append('\n'); isReadingChar = false; break;
						case 'r' : stringChars.append('\r'); isReadingChar = false; break;
						case 't' : stringChars.append('\t'); isReadingChar = false; break;
						case 'u' : characterChars = new StringBuffer(); break;
					}
				}
			}
			else
				stringChars.append(ch);
		}
		else
		{
			switch(ch)
			{
				case '{':
				{
					objectCount++;
					
					if(arrayCount == 0 && objectCount == 1)
						delegate.didStartJson();
						
					delegate.didStartObject(); 
				} 
				break;
				
				case '}':
				{
					objectCount--;
										
					if(arrayCount == 0 && objectCount == 0)
						delegate.didEndJson();
						
					checkIfValueReadAndNotify(); 
					delegate.didEndObject();
				}
				break;
					
				case '[':
				{
					arrayCount++;
					
					if(objectCount == 0 && arrayCount == 1)
						delegate.didStartJson();
						
					delegate.didStartArray(); 
				}
				break;
					
				case ']': 
				{
					arrayCount--;
					
					if(arrayCount == 0 && objectCount == 0)
						delegate.didEndJson();
	
					checkIfValueReadAndNotify(); 
					delegate.didEndArray();
				}
				break;
	
				case '"':
				{
					if(!isReadingString)
					{
						isReadingString = true;
						stringChars = new StringBuffer();
					}
					else
					{
						isReadingString = false;
					}
				} 
				break;
				
				case ':':
				{
					delegate.didReadKey(stringChars.toString());
					stringChars = null;
				}
				break;
	
				case ',':
				{
					checkIfValueReadAndNotify();
				} 
				break;
				
				default:
				{
					if(ch != ' ')
					{
						if(stringChars == null)
							stringChars = new StringBuffer();
						
						stringChars.append(ch);
					}
					else
						checkIfValueReadAndNotify();
				}
				break;
			}
		}
	}
}
