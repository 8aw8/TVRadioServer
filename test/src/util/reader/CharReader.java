package util.reader;

public class CharReader 
{
	private final CharReaderDelegate delegate; 
   
	private int ch, byte1 = -1, byte2 = -1, byte3 = -1;
    private boolean flag;

	public CharReader(CharReaderDelegate delegate)
	{
		this.delegate = delegate;
	}
	
	public void reset()
	{
		ch 		= 0;
		byte1 	= -1;
		byte2 	= -1; 
		byte3 	= -1;
		flag	= false;
	}
	
    public void processCP1251Byte(int b)
    {
    	if(delegate != null)
    		delegate.didReadChar((char)(b < 0 ? b == -88 ? 0x401 : b == -72 ? 0x451 : (0x400 | ((b & 0x7f) - 0x30)) : b));
    }
    
 	public void processByte(int b) 
 	{
		if (byte1 == -1) 
		{
			byte1 = b & 0xFF;
			flag = true;
		} 
		else 
			flag = false;
		
		switch (byte1 >> 4) 
		{
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
			ch = byte1;

			byte1 = -1;
			byte2 = -1;
			byte3 = -1;

			if(delegate != null)
				delegate.didReadChar((char) ch);
			
			return;

		case 12:
		case 13:
			if (flag)
				return;

			byte2 = b;

			if ((byte2 & 0xC0) != 128) {
				//TVRadioServerMain.logger.info("ERROR: (char2 & 0xC0) != 128");
				return;
			}

			ch = ((byte1 & 0x1F) << 6 | byte2 & 0x3F);

			byte1 = -1;
			byte2 = -1;
			byte3 = -1;

			if(delegate != null)
				delegate.didReadChar((char) ch);
			
			return;

		case 14:
			if (flag)
				return;

			if (byte2 == -1) 
			{
				byte2 = b;
				return;
			}

			byte3 = b;

			if (((byte2 & 0xC0) != 128) || ((byte3 & 0xC0) != 128)) 
			{
				//TVRadioServerMain.logger.info("ERROR: ((char2 & 0xC0) != 128) || ((char3 & 0xC0) != 128)");
				return;
			}

			ch = ((ch & 0xF) << 12 | (byte2 & 0x3F) << 6 | (byte3 & 0x3F) << 0);

			byte1 = -1;
			byte2 = -1;
			byte3 = -1;

			if(delegate != null)
				delegate.didReadChar((char) ch);
			
			return;

		case 8:
		case 9:
		case 10:
		case 11:
		default:
			// error, skip
			return;
		}
	}
}
