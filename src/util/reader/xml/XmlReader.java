package util.reader.xml;

import java.util.Enumeration;
import java.util.Hashtable;

import util.reader.CharReader;
import util.reader.CharReaderDelegate;

public class XmlReader implements CharReaderDelegate
{
    public final Hashtable<String, String> attributes = new Hashtable<String, String>(5);
    private String tagName;
    private String text;
    private int lastKeyChar;
    private int valueStartQuoteChar = 0;
    
    private StringBuffer tagNameChars;
    private StringBuffer attrKeyChars;
    private StringBuffer attrValueChars;
    private StringBuffer textChars;

    private XmlReaderDelegate delegate;
    
    private CharReader charReader;
    
    public XmlReader(XmlReaderDelegate delegate)
    {
    	this.delegate 	= delegate;
    	this.charReader = new CharReader(this);
    }
    
    public void reset()
    {
    	charReader.reset();
    	
        lastKeyChar 		= 0;
        valueStartQuoteChar = 0;
    	
        tagName 		= null;
        text			= null;
        tagNameChars 	= null;
        attrKeyChars	= null;
        textChars 		= null;
        
        attributes.clear();
    }
    
    public String getTagName()
    {
    	return tagName;
    }
    
    public String getText()
    {
    	return text;
    }
    
    public String getAttributeValueForKey(String key)
    {
    	return attributes.get(key);
    }
    
    public Enumeration<String> getAttributeKeys()
    {
    	return attributes.keys();
    }
    
    public void processCP1251Byte(int b)
    {
    	charReader.processCP1251Byte(b);
    }
    
 	public void processByte(int b) 
 	{
 		charReader.processByte(b);
	}

    public void processChar(char ch)
    {
    	if(lastKeyChar == '<')
    	{
    		// end tag
            if (ch == '/' && attrValueChars == null) 
            {
        		if(tagName != null)
        		{
    				lastKeyChar = ch;
    				
    				// notify delegate
    				if(delegate!=null)
    					delegate.readerDidStartElement(this, getTagName(), attributes);
    				
    				return;
    			}

				lastKeyChar = ch;
				
                return;
            }
            // ignore xml heading
            else if (ch == '?') 
            { 
                tagNameChars = null;
            }
            // ignore comments 
            else if (ch == '!') 
            { 
                tagNameChars = null;
            }
            // start tag
            else
            {
            	if(tagNameChars != null && (ch == ' ' || ch == '/' || ch == '>'))
            	{
            		tagName 		= tagNameChars.toString();
            		tagNameChars 	= null;
            		
            		if(ch == '>')
            		{
        				lastKeyChar = ch;
        				
        				// notify delegate
        				if(delegate!=null)
        					delegate.readerDidStartElement(this, getTagName(), attributes);
            		}
 
            		return;
            	}
            	
            	// read tag name
            	if(tagNameChars != null)
            	{
            		tagNameChars.append(ch);
            		
            		return;
            	}
            	// read attribute
            	else
            	{
            		if(ch == ' ' && attrValueChars == null)
            			return;
        			
        			if(ch == '>')
        			{
        				lastKeyChar = ch;
        				if(tagName == null)
        					return;
        				
        				
        				// notify delegate
        				if(delegate!=null)
        					delegate.readerDidStartElement(this, getTagName(), attributes);
        				
        				return;
        			}

            		if(ch == '=')
            		{
            			attrValueChars = new StringBuffer();
            			return;
            		}
            			
            		if((ch == '"' || ch == '\'') && (valueStartQuoteChar == 0))
            		{
						valueStartQuoteChar = ch;
						return;
            		}
            		
					if((valueStartQuoteChar != 0) && (valueStartQuoteChar == ch))
					{
						attributes.put(attrKeyChars.toString(), unescapeXML(attrValueChars.toString()));
						
						attrKeyChars 		= null;
						attrValueChars 		= null;
						valueStartQuoteChar = 0;
						
						return;
					}
            		
            		if(attrKeyChars == null)
            			attrKeyChars = new StringBuffer();
            		
            		if(attrValueChars == null)
            			attrKeyChars.append(ch);
            		else
            			attrValueChars.append(ch);
            		
        			return;
            	}
            }
    	}
    	else if(lastKeyChar == '/')
    	{
    		if(ch == ' ')
    			return;

    		if(ch == '>' && tagName != null && tagNameChars == null)
    		{
    			lastKeyChar = ch;
				
				// notify delegate
				if(delegate!=null)
					delegate.readerDidEndElement(this, getTagName(), text);
				
    			return;
    		}
    		else
    		{
        		if(ch == '>')
        		{
        			if(tagNameChars != null)
        			{
        				tagName = tagNameChars.toString();
            			tagNameChars = null;
        			}
        			
        			lastKeyChar = ch;

    				
    				// notify delegate
    				if(delegate!=null)
    					delegate.readerDidEndElement(this, getTagName(), text);
    				
            		return;
            	}

            	if(tagNameChars == null)
            		tagNameChars = new StringBuffer();
            	
            	// read tag name
            	tagNameChars.append(ch);
            	
            	return;
    		}
    	}
    	else if(lastKeyChar == '>')
    	{
    		if(textChars == null)
    			textChars = new StringBuffer();
    		
    		if(ch == '<')
    		{
	            // reset all
	            tagName 		= null;
	            text			= null;
	            lastKeyChar 	= ch;
                tagNameChars 	= new StringBuffer();
	            attributes.clear();
	            
	            // set text
	            if(textChars.length() > 0)
	            {
		            text = unescapeXML(textChars.toString());
		            textChars = null;
		            
    				// notify delegate
    				if(delegate!=null)
    					delegate.readerDidReadText(this, getText());
	            }
				
	            return;
    		}
    		
    		textChars.append(ch);
    	}
    	else
    	{
	    	if(ch < ' ') 
	    		return;
	        
	        if(ch == '<') 
	        {
	            // reset all
	            tagName 		= null;
	            text 			= null;
	            lastKeyChar 	= ch;
                tagNameChars 	= new StringBuffer();
	            attributes.clear();
	        }
    	}
   }
    
	public static String unescapeXML(String str) 
	{
		if (str == null || str.length() == 0)
			return "";

		StringBuffer buf = new StringBuffer();
		int len = str.length();
		for (int i = 0; i < len; ++i) {
			char c = str.charAt(i);
			if (c == '&') {
				int pos = str.indexOf(";", i);
				if (pos == -1) { // Really evil
					buf.append('&');
				} else if (str.charAt(i + 1) == '#') {
					int val = Integer.parseInt(str.substring(i + 2, pos), 16);
					buf.append((char) val);
					i = pos;
				} else {
					String substr = str.substring(i, pos + 1);
					if (substr.equals("&amp;"))
						buf.append('&');
					else if (substr.equals("&lt;"))
						buf.append('<');
					else if (substr.equals("&gt;"))
						buf.append('>');
					else if (substr.equals("&quot;"))
						buf.append('"');
					else if (substr.equals("&apos;"))
						buf.append('\'');
					else
						// ????
						buf.append(substr);
					i = pos;
				}
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}

	@Override
	public void didReadChar(char ch) 
	{
		this.processChar(ch);
	}

}
