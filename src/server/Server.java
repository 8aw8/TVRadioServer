package server;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import metadata.Metadata;
import metadata.MetadataController;
import metadata.MetadataDelegate;
import naga.NIOSocket;
import naga.SocketObserver;
import naga.packetreader.RegularPacketReader;
import naga.packetwriter.RegularPacketWriter;
import util.Util;
import actions.Action;
import actions.ActionDelegate;
import actions.JSONResponseAction;
import app.TVRadioServerMain;

public class Server implements SocketObserver, MetadataDelegate, ActionDelegate
{
	// song informers
	private static final int 						MAX_SONGINFO_INFORMERS_PER_STATION 	= 3;
	private static final Map<String, List<Server>> 	songInfoInformers 					= new ConcurrentHashMap<String, List<Server>>();

	// ping
	private static final byte[] PING_PACKET 	= new byte[]{};
	private static Timer 		pingTimer 		= new Timer(); 
	private TimerTask 			pingTimerTask;
		
	// common
	private final NIOSocket socket;
	private ServerDelegate 	delegate;

	// client vars
	private String 			userId;
	private String 			deviceId;
	private String			regionId;
	private float 			version;
	private long 			startListeningStationTime;
	private String 			currentListeningStationId;
	
	// methods map
	private static final Map<String, Class<?>> methodActions;
	static
	{
		methodActions = new HashMap<String, Class<?>>();
		
		// tv
		methodActions.put("tv.getCategories", 						actions.tv.GetCategoriesAction.class);
		methodActions.put("tv.getCategory", 						actions.tv.GetCategoryContentAction.class);
		
		// radio
		methodActions.put("radio.getCategories", 					actions.radio.GetCategoriesAction.class);
		methodActions.put("radio.getCategory", 						actions.radio.GetCategoryContentAction.class);
		methodActions.put("radio.didAddStationToFavorites", 		actions.radio.AddStationToFavoritesAction.class);
		methodActions.put("radio.didRemoveStationFromFavorites", 	actions.radio.RemoveStationFromFavoritesAction.class);
		methodActions.put("radio.searchStation", 					actions.radio.SearchStationWithNameAction.class);
//   Закоментировал для теста "radio.GetFavoritesAction"
	//	methodActions.put("radio.getCountries", 					actions.radio.GetFavoritesAction.class);
		methodActions.put("radio.getCountries", 					actions.radio.GetSupportedCountiesAction.class);
		
		methodActions.put("radio.getDefaultCountry", 				actions.radio.GetDefaultCountryAction.class);
		methodActions.put("radio.getRegions", 						actions.radio.GetSupportedRegionsAction.class);
		methodActions.put("radio.addStation", 						actions.radio.AddStationAction.class);
		methodActions.put("radio.getPlayedSongs", 					actions.radio.GetPlayedSongsForStationAction.class);
		
		methodActions.put("radio.getFavorites", 					actions.radio.GetFavoritesAction.class);
		
		methodActions.put("radio.getGenre", 					    actions.radio.GetGenreStaionsAction.class);
		methodActions.put("radio.getMood", 					        actions.radio.GetMoodStationsAction.class);
		
		// common
		methodActions.put("getCitiesForCountryWithCode", 			actions.radio.GetCitiesForCountryWithCodeAction.class);
		methodActions.put("searchCity", 							actions.radio.SearchCitiesWithNameForCountryWithCodeAction.class);
	}
	
	// ======================================
	// Public
	// ======================================

	public Server(NIOSocket socket, ServerDelegate delegate)
	{
		this.socket 	= socket;
		this.delegate 	= delegate;
		
		socket.setPacketWriter(new RegularPacketWriter(4, true));
		socket.setPacketReader(new RegularPacketReader(4, true));

		socket.listen(this);
	}

	public void write(String string)
	{
		if(string == null)
			return;
		
		try 
		{
			this.write(string.getBytes("UTF-8"));
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
	}

	public void write(final byte[] bytes)
	{
		if(bytes == null)
			return;
		
		// all write operations in NIO thread
		TVRadioServerMain.eventMachine.asyncExecute(new Runnable() 
		{
			public void run() 
			{
				socket.write(bytes);
				
				// we don't want to store unknown clients
				if(deviceId == null)
					socket.closeAfterWrite();
			}
		});
	}

	// ======================================
	// Private
	// ======================================

	private void cancelPing()
	{
		// cancel previous timer task
		if(pingTimerTask != null)
			pingTimerTask.cancel();
	}
	
	private void schedulePing()
	{
		cancelPing();
		
		// create new timer task
		pingTimerTask = new TimerTask(){
			@Override
			public void run() 
			{
				TVRadioServerMain.logger.info("CLIENT TIMEOUT "+deviceId);
				socket.close();
			}
		};
		
		// schedule task		
		try 
		{
			pingTimer.schedule(pingTimerTask, 5 * 60 * 1000);
		} 
		catch (Exception e) 
		{
			// restart timer on error
			pingTimer = new Timer();
			pingTimer.schedule(pingTimerTask, 5 * 60 * 1000);
		}
	}
	
	private void saveListeningSessionToDB()
	{
		if(currentListeningStationId != null && userId != null && startListeningStationTime > 0 && (System.currentTimeMillis() - startListeningStationTime > 60 * 1000))
			new actions.radio.AddListeningSessionForStationAction(currentListeningStationId, userId, startListeningStationTime, System.currentTimeMillis()).start();
	}
	
	private List<Server> getOrCreateInformersListForCurrentListeningSTation()
	{
		List<Server> informers = songInfoInformers.get(currentListeningStationId);
		if(informers == null)
		{
			informers = new CopyOnWriteArrayList<Server>();
			songInfoInformers.put(currentListeningStationId, informers);
		}
		
		return informers;
	}
	
	private void enableInformingAboutSongInfo()
	{
		if(currentListeningStationId == null || version < 1.4f)
			return;
		
		// add new informer if not enough
		List<Server> informers = getOrCreateInformersListForCurrentListeningSTation();
		if(informers.size() < MAX_SONGINFO_INFORMERS_PER_STATION)
			write("{\"songInfo\":{\"enabled\":1}}");
	}
	
	private void confirmEnabledInformerAboutSongInfo()
	{
		if(currentListeningStationId == null || version < 1.4f)
			return;
		
		List<Server> informers = getOrCreateInformersListForCurrentListeningSTation();
		if(informers.size() < MAX_SONGINFO_INFORMERS_PER_STATION)
			informers.add(this);
		else
			disableInformingAboutSongInfo();
	}
	
	private void disableInformingAboutSongInfo()
	{
		if(currentListeningStationId == null || version < 1.4f)
			return;
		
		// check
		List<Server> informers = songInfoInformers.get(currentListeningStationId);
		if(informers != null)
			informers.remove(this);
		
		// some error may occurs, so disable in anyway
		write("{\"songInfo\":{\"enabled\":0}}");
	}
	
	private void processPacketWithParameters(Map<String, String> parameters) throws Exception
	{
//		TVRadioServerMain.logger.info(parameters.toString());
		
		// get method
		String method = parameters.get("method");
		if (method == null)
			return;
		
		regionId = parameters.get("region_id");

		// get client information
		if(deviceId == null)
		{
			// get version
			version = Util.appVersionToFloat(parameters.get("version"));
			
			// get userId
			userId = parameters.get("userId");
			
			// get deviceId
			deviceId = parameters.get("deviceId");
			if(deviceId != null)
			{
				// add new client
				if(userId == null)
				{
					// check purchase info
					if(deviceId.endsWith("#"))
						deviceId = deviceId.substring(0, deviceId.length()-1);
					else
						new actions.radio.CheckClientPurchaseSongInfoAction(this, deviceId).start();
				
					// add client to db
					actions.radio.AddClientAndGetUserIdAction action = 
							new actions.radio.AddClientAndGetUserIdAction(deviceId, parameters.get("deviceFamily"), parameters.get("deviceOS"), version);
					action.start();
					userId = action.getUserId();

					// write userId to client
					if(userId != null)
						write("{\"userId\":\""+userId+"\"}");
				}
				// add new device to existing client
				else
				{
					new actions.radio.AddNewDeviceForExistingClientAction(userId, deviceId, parameters.get("deviceFamily"), parameters.get("deviceOS"), version);
				}
			}

			// notify delegate
			if(delegate!=null)
				delegate.ServerDidConnect(this);
		}
		
		// process methods
		// ======================================
		if (method.equals("radio.songInfo")) 
		{
			// confirm that our informer is active
			confirmEnabledInformerAboutSongInfo();
			
			// run action that store info into db
			new actions.radio.StoreSongInfoAction(this, parameters).start();
		} 
		
		// ======================================
		else if (method.equals("radio.startPlaying")) 
		{
			String stationId = parameters.get("stationId");
			if(stationId == null || !Util.isNumeric(stationId))
				return;
			
			if(currentListeningStationId == null || !currentListeningStationId.equals(stationId))
			{
				// saveListeningSession
				saveListeningSessionToDB();
				
				// save start time
				startListeningStationTime = System.currentTimeMillis();
				
				// save station id
				currentListeningStationId = stationId;
						
				// start metadata observing
				MetadataController.addObserver(this);
				
				// check metadata
				Metadata metadata = MetadataController.metadataForStation(stationId);
				if (metadata != null)
					didReceiveMetadata(metadata);
			
				// enable informer
				enableInformingAboutSongInfo();
			}
		} 
		
		// ======================================
		else if (method.equals("radio.stopPlaying")) 
		{
			// disable informer
			disableInformingAboutSongInfo();
			
			// saveListeningSession
			saveListeningSessionToDB();
			
			// stop metadata observing
			MetadataController.removeObserver(this);
			
			// reset station id
			currentListeningStationId = null;
			// reset start time
			startListeningStationTime = 0;
		} 
		
		
		// ======================================
		else if (method.equals("radio.failPlaying")) 
		{
			// disable informer
			disableInformingAboutSongInfo();
			
			// stop metadata observing
			MetadataController.removeObserver(this);
			
			// reset station id
			currentListeningStationId = null;
			// reset start time
			startListeningStationTime = 0;
		} 
		
		// ======================================
		else
		{
			// run registered action
			Class<?> actionClass = methodActions.get(method);
			
			parameters.put("user_id", this.userId);
			
			if(actionClass != null)
			{
				((Action) actionClass.getConstructor(ActionDelegate.class, Map.class).newInstance(this, parameters)).start();
			}
			// unknown method
			else
			{
				Map<String, Object> itemInfo = new HashMap<String, Object>();
				itemInfo.put("error", "unknown method passed");
				itemInfo.put("request", parameters);
				itemInfo.put("id", parameters.get("id"));
				write(util.writer.json.JsonWriter.toJSONString(itemInfo));
			}
		}
	}
	
	// ======================================
	// Socket observer
	// ======================================

	@Override
	public void connectionOpened(NIOSocket nioSocket) { /* ignore */ }

	@Override
	public void connectionBroken(NIOSocket nioSocket, Exception exception)
	{
//		TVRadioServerMain.logger.info("CLIENT DISCONNECTED");
		
		// disable ping
		cancelPing();
		
		// disable informer
		disableInformingAboutSongInfo();

		// saveListeningSession
		saveListeningSessionToDB();
		
		// stop metadata observing
		MetadataController.removeObserver(this);
		
		// notify delegate
		if(delegate!=null)
			delegate.ServerDidDisconnect(this);
	}

	@Override
	public void packetReceived(final NIOSocket socket, final byte[] packet) 
	{
		TVRadioServerMain.logger.info("PACKET>> "+new String(packet));

		// ping
		if(packet.length == 0)
		{
			// send ping response
			write(PING_PACKET);
			// cancel previous timeout timer and start new
			schedulePing();
			return;
		}

		// process in thread
		TVRadioServerMain.executor.submit(new Runnable() 
		{
			public void run()
			{
				try 
				{
					processPacketWithParameters(Util.getQueryMap(new String(packet)));
				} 
				catch (Exception e) 
				{
					TVRadioServerMain.logger.info("PACKET>> " + new String(packet));
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void packetSent(NIOSocket socket, Object tag) { /* ignore */ }

	// ======================================
	// Metadata
	// ======================================

	@Override
	public void didReceiveMetadata(Metadata metadata) 
	{
		if(currentListeningStationId!=null && currentListeningStationId.equals(metadata.stationId))
			write(metadata.jsonRepresentation());
	}

	// ======================================
	// Action Delegate
	// ======================================

	@Override
	public void didFinishAction(Action action) 
	{
		if(action instanceof JSONResponseAction)
			write(((JSONResponseAction) action).getJSONResponse());
	}
	
	public String getUserIdForAction(Action action)
	{
		return userId;
	}

}