package rootServer;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import app.TVRadioServerMain;

import naga.ConnectionAcceptor;
import naga.NIOServerSocket;
import naga.NIOSocket;
import naga.ServerSocketObserver;
//import client.Client;
//import client.ClientDelegate;

import server.Server;
import server.ServerDelegate;

public class rootServer implements ServerSocketObserver, ServerDelegate
{
	public static final Queue<Server> servers = new ConcurrentLinkedQueue<Server>();

	private final int port;

	private static long lastLogTotalClientsCountTime = 0;
	
	// ======================================
	// Public methods
	// ======================================

	public rootServer(int port) throws IOException 
	{
		this.port = port;
	}

	public void start() throws IOException
	{
		// start server
		NIOServerSocket serverSocket = TVRadioServerMain.eventMachine.getNIOService().openServerSocket(port);

		serverSocket.listen(this);
		serverSocket.setConnectionAcceptor(ConnectionAcceptor.ALLOW);
	}

	private void log()
	{
		if(System.currentTimeMillis() - lastLogTotalClientsCountTime > 5 * 60 * 1000)
		{
			TVRadioServerMain.logger.info("===================");
			TVRadioServerMain.logger.info("TOTAL CLIENTS COUNT: "+servers.size());
			long total = TVRadioServerMain.executor.getTaskCount();
			long completed = TVRadioServerMain.executor.getCompletedTaskCount();
			TVRadioServerMain.logger.info("TOTAL EXECUTOR TASKS COUNT: total:"+ total + ", completed:" +completed +", uncompleted:"+(total-completed));
			TVRadioServerMain.logger.info("TOTAL EVENTMACHINE TASKS COUNT: "+TVRadioServerMain.eventMachine.getQueueSize());
			TVRadioServerMain.logger.info("===================");
			lastLogTotalClientsCountTime = System.currentTimeMillis();
			
			System.gc();
		}
	}
	
	// ======================================
	// Server socket observer
	// ======================================

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
		// start new client
		new Server(nioSocket, this);
	}

	// ======================================
	// Client delegate
	// ======================================

	@Override
	public void ServerDidConnect(Server server) 
	{
		servers.add(server);
		log();	
	}

	@Override
	public void ServerDidDisconnect(Server server) 
	{
		servers.remove(server);
	}
}