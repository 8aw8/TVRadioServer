package app;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
//import java.util.logging.FileHandler;
import java.util.logging.Logger;
//import java.util.logging.SimpleFormatter;

//import server.Server;
import rootServer.rootServer;

import metadata.MetadataController;
import naga.eventmachine.EventMachine;

public class TVRadioServerMain 
{
	public static final Logger logger = Logger.getLogger("Log");
	public static final EventMachine eventMachine;
	public static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
	
	static 
	{
		// init event machine
		EventMachine _eventMachine;
		try { 
			_eventMachine = new EventMachine(); 
		} 
		catch (IOException e) 
		{ 
			_eventMachine = null; 
			e.printStackTrace(); 
		}
		eventMachine = _eventMachine;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try 
		{
			// log
//	    	FileHandler fh = new FileHandler("server.txt");  
//	        logger.addHandler(fh);
//	        SimpleFormatter formatter = new SimpleFormatter();  
//	        fh.setFormatter(formatter);  

			// start event machine
			eventMachine.start();
			
			// start metadata controller
			MetadataController.start();
			
			// start server
			new rootServer(8081).start();
			TVRadioServerMain.logger.info("Server started.");
			
			// log threads
			new ThreadsLogger().start(8082);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
