package app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import naga.ConnectionAcceptor;
import naga.NIOServerSocket;
import naga.NIOSocket;

public class ThreadsLogger 
{
	public void start(int port)
	{
		try {
			NIOServerSocket server 	= TVRadioServerMain.eventMachine.getNIOService().openServerSocket(port);
			
			server.setConnectionAcceptor(ConnectionAcceptor.ALLOW);
			server.listen(new naga.ServerSocketObserverAdapter(){
				
				@Override
				public void acceptFailed(IOException exception) 
				{
					TVRadioServerMain.logger.info("acceptFailed");
					exception.printStackTrace();
				}

				@Override
				public void serverSocketDied(Exception exception) 
				{
					TVRadioServerMain.logger.info("serverSocketDied");
					exception.printStackTrace();
				}

				@Override
				public void newConnection(NIOSocket nioSocket) 
				{
					TVRadioServerMain.logger.info("Client " + nioSocket.getIp() + " connected.");

					nioSocket.setPacketReader(new naga.packetreader.AsciiLinePacketReader());

					nioSocket.listen(new naga.SocketObserverAdapter(){
						@Override
						public void packetReceived(final NIOSocket nioSocket, final byte[] packet)
						{
							String request = new String(packet);
		        			if(request != null && request.equals("method=threads"))
		        			{
		    					try {
		    						ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    						
		    						Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
		    						baos.write("\r---------------------\r".getBytes());
		    						baos.write(("THREADS COUNT: "+traces.size()).getBytes());
		    						for(Entry<Thread, StackTraceElement[]> entry : traces.entrySet()){
		    							baos.write("\r---------------------\r".getBytes());
		    							baos.write(("THREAD: "+entry.getKey().getName()).getBytes());
		    							baos.write("\rTRACE:\r".getBytes());

		    							for(StackTraceElement element : entry.getValue()){
		    								baos.write(element.toString().getBytes());
		    								baos.write("\r".getBytes());
		    							}
		    						}
		    						baos.flush();
		    						nioSocket.write(baos.toByteArray());
		    						nioSocket.closeAfterWrite();
		    					} catch (Exception e) {
		    						e.printStackTrace();
		    					}
		        			}
						}
					});
				}
			});
		}	
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
