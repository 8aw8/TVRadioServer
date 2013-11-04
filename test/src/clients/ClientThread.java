/**
 * 
 */
package clients;

import java.io.IOException;
import naga.NIOService;
import naga.NIOSocket;
import naga.packetreader.RegularPacketReader;
import naga.packetwriter.RegularPacketWriter;
import socketObservers.Observer;

/**
 * @author account
 *
 */
public class ClientThread extends Thread {

	private static NIOSocket socket;
	private final String sendHeader = "deviceFamily=iPhone%20Simulator&userLocale=uk&region_id=rx0001&version=1.5&id=1&method=radio.getCategories&iso_code=RU&deviceId=c37b2e6fe99536283ca3f658580c4cdd&deviceOS=6.0";
	private NIOService service;
	private String host;
	private int port;
	private int numThread;
	private Observer observer;
	private boolean runThread;

	
	public ClientThread(NIOService NIOservice, String Host, int Port, int NumThread) {
		// TODO Auto-generated constructor stub	
		service = NIOservice;
		host = Host;
		port = Port;
		numThread = NumThread;
		runThread = true;
	
		
		 
		 if (service.isOpen() ) 
				try {
					socket = service.openSocket(host, port);
					
					if (socket.isOpen())
					{
						socket.setPacketReader(new RegularPacketReader(4, true));
				        socket.setPacketWriter(new RegularPacketWriter(4, true));
				        
				        observer = new Observer(sendHeader, numThread, this);
				        socket.listen(observer);
				        
					}
			        
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
	
	public void setThreadStatus(boolean status) {
		runThread = status;
	}

	public boolean getThreadStatus(){
		return runThread;
	}
	
	@Override
    public void run()	//Этот метод будет выполнен в побочном потоке
    {
        System.out.println("Привет из побочного потока #"+numThread +"!");
        
        if (service.isOpen() ) 
		{
        	try 
        	{
			while (runThread)
                {
					service.selectBlocking();
                }
        	} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
                
		}//if service.isOpen() ) 
    }

}
