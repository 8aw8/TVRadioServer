/**
 * 
 */
package socketObservers;

import java.io.UnsupportedEncodingException;

import clients.ClientThread;

import naga.NIOSocket;
import naga.SocketObserver;

/**
 * @author account
 *
 */
public class Observer implements SocketObserver {
	
	private String sendHeader;
	private int numThread;
	private int countSend = 0;
	private ClientThread ownerThread;
	
	public  Observer(String _sendHeader, int _numThread, ClientThread _ownerThread)
	{
		sendHeader = _sendHeader;
		numThread = _numThread;
		ownerThread = _ownerThread;
	}
  
	
	public void connectionOpened(NIOSocket nioSocket) 
    { 
    	byte[] content = null;
		try {
			content = sendHeader.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("ERROR: "+e.getMessage());
		} 

 	   nioSocket.write(content);
 	   System.out.println("Sending ... Thread #"+ numThread +" "+ sendHeader);
    }

    public void packetReceived(NIOSocket socket, byte[] packet)
    {
            try
            {
         	   // Read the UTF-reply and print it.
           //      String reply = new DataInputStream(new ByteArrayInputStream(packet)).readUTF();
            	
                String send1 = "id=4&iso_code=RU&country_iso_code=RU&region_id=r101053&user_id=0&method=radio.getRegions&userLocale=uk";
                String send2 = "method=radio.getCategories&iso_code=RU&region_id=r101054&id=5&userLocale=uk";
            	
            	byte[] content = null;
        		try {
        			content = send1.getBytes("UTF-8");
        		} catch (UnsupportedEncodingException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        			System.out.println("ERROR: "+e.getMessage());
        		} 
         	   
                    System.out.println("Thread #"+ numThread+" Reply was: " +   new String(packet, "UTF-8"));
                   
                    
                if (countSend<1) 
               {
                    	socket.write(send1.getBytes("UTF-8"));
                    	socket.write(send2.getBytes("UTF-8"));
                    	countSend++;
               }
                else
                {
                //  socket.closeAfterWrite(); 
                //  ownerThread.setThreadStatus(false);
                }
                   
                    // Exit the program.
                 //   System.exit(0);
            }
            catch (Exception e)
            {
                    e.printStackTrace();
            }
    }

    public void connectionBroken(NIOSocket nioSocket, Exception exception)
    {
            System.out.println("Connection failed.");
            // Exit the program.
        //    System.exit(-1);
    }

	@Override
	public void packetSent(NIOSocket socket, Object tag) {
		// TODO Auto-generated method stub
	
	}


	public ClientThread getOwnerThread() {
		return ownerThread;
	}


	public void setOwnerThread(ClientThread ownerThread) {
		this.ownerThread = ownerThread;
	}	
	
}
