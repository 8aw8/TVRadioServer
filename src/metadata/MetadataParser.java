package metadata;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import naga.NIOSocket;
import naga.SocketObserver;
import app.TVRadioServerMain;


public abstract class MetadataParser  implements SocketObserver
{
	protected static final int 			DEFAULT_FETCH_INTERVAL = 20 * 1000; 
	
	protected static final int 			TIMEZONE_HOUR_DELTA = 4; 
	
	protected MetadataParserDelegate 	delegate;
	protected long 						lastFetchInterval = 0;
	protected NIOSocket 				nioSocket;
	
	private volatile boolean			isProcessing;
	private Queue<byte[]>				packetQueue;
	
	protected String 					host;
	protected int 						port;
	
	//======================================
	// Abstract methods for subclasses
	//======================================

	protected abstract byte[] getRequestBody();
	protected abstract void processPacket(byte[] packet) throws Exception;
	
	//======================================
	// Public methods
	//======================================

	public MetadataParser(MetadataParserDelegate delegate, String host, int port)
	{
		this.delegate 	= delegate;
		this.host 		= host;
		this.port 		= port;
		
		packetQueue = new ConcurrentLinkedQueue<byte[]>();
	}
	
	public void start()
	{
		lastFetchInterval = DEFAULT_FETCH_INTERVAL;
		
		try {
			nioSocket = TVRadioServerMain.eventMachine.getNIOService().openSocket(host, port); 
			nioSocket.listen(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//======================================
	// Private methods
	//======================================

	private void nextFetch()
	{
		// clear
		packetQueue.clear();
		
		// start next fetch
		TVRadioServerMain.eventMachine.executeLater(new Runnable(){
			@Override
			public void run() {
				start();
			}
		}, lastFetchInterval);
	}
	
	private void processNextPacket()
	{
//		synchronized(packetQueue)
		{
		if(!isProcessing && packetQueue.size() > 0)
		{
			// flag
			isProcessing = true;
			
			// process first packet from queue in subclasses
			try 
			{
				processPacket(packetQueue.poll());
			} 
			catch (Exception e) 
			{
				// unflag
				isProcessing = false;
				
				// clear queue
				packetQueue.clear();
				
				// close socket
				nioSocket.close();
				
				return;
			}
			
			// unflag
			isProcessing = false;

			// next
			processNextPacket();
		}
		}
	}

	//======================================
	// NioSocket delegate methods
	//======================================

	@Override
	public void connectionOpened(NIOSocket nioSocket)
	{
		nioSocket.write(this.getRequestBody());
	}

	@Override
	public void connectionBroken(NIOSocket nioSocket, Exception exception) 
	{
		// start next fetch
		this.nextFetch();
	}

	@Override
	public void packetReceived(NIOSocket socket, byte[] packet) 
	{
		if(packet.length == 0)
			return;
		
		// add to queue
		packetQueue.add(packet);
		
		// process if possible
		if(!isProcessing)
		{
			TVRadioServerMain.executor.submit(new Runnable() {
				
				public void run() {
					processNextPacket();
				}
			});
		}
	}

	@Override
	public void packetSent(NIOSocket socket, Object tag) { /* ignore */ }

}
