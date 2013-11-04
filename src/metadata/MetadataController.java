package metadata;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import metadata.parsers.AfterhoursFMParser;
import metadata.parsers.AutoradioParser;
import metadata.parsers.EldoRadioParser;
import metadata.parsers.EuropaPlusParser;
import metadata.parsers.EuropaPlusUAParser;
import metadata.parsers.FinamFMParser;
import metadata.parsers.MoskvaFMParser;
import metadata.parsers.NRJParser;
import metadata.parsers.OzbegimRadioParser;
import metadata.parsers.PiterFMParser;
import metadata.parsers.PleerDotNasheParser;
import metadata.parsers.Radio4DukParser;
import metadata.parsers.Radio7Parser;
import metadata.parsers.RadioAlphaParser;
import metadata.parsers.RadioCafeParser;
import metadata.parsers.RadioClassicParser;
import metadata.parsers.RadioJazzParser;
import metadata.parsers.RadioOrfeusMainParser;
import metadata.parsers.RadioOrfeusOtherParser;
import metadata.parsers.RadioSibirParser;
import metadata.parsers.RadioZvezdaParser;
import metadata.parsers.RelaxFMParser;
import metadata.parsers.StationRUParser;
import metadata.parsers.TVRainParser;
import metadata.parsers.UnistarParser;
import metadata.parsers.UnitonRadioParser;
import metadata.parsers.YerevanNightsRadioParser;
import metadata.parsers.ZkrivkiRadioParser;

public class MetadataController implements MetadataParserDelegate {
	private static MetadataController instance = null;
	private static boolean isStarted = false;
	
	private final Map<String,Metadata> stationsMetadata;
	private final Queue<MetadataDelegate> observers;
	
	///////////////////////////////////////////////////
	// Public methods 
	///////////////////////////////////////////////////
	
	public MetadataController()
	{
		this.stationsMetadata 	= new HashMap<String,Metadata>();
		this.observers		 	= new ConcurrentLinkedQueue<MetadataDelegate>();
	}
	
	private static MetadataController getInstance()
	{
		if(instance == null)
			instance = new MetadataController();
		
		return instance;
	}
	
	public static void addObserver(MetadataDelegate observer)
	{
		if(!getInstance().observers.contains(observer))
			getInstance().observers.add(observer);
	}

	public static void removeObserver(MetadataDelegate observer)
	{
		getInstance().observers.remove(observer);
	}
	
	public static void start()
	{
		if(isStarted)
			return;
		
		isStarted = true;
		
		// start all parsers
		new TVRainParser(getInstance()).start();
		
		new RadioOrfeusMainParser(getInstance()).start();
		
		new RadioOrfeusOtherParser(getInstance(), "72", "353").start();
		new RadioOrfeusOtherParser(getInstance(), "73", "354").start();
		new RadioOrfeusOtherParser(getInstance(), "74", "355").start();
		new RadioOrfeusOtherParser(getInstance(), "75", "356").start();
		new RadioOrfeusOtherParser(getInstance(), "76", "357").start();
		new RadioOrfeusOtherParser(getInstance(), "77", "358").start();
		
		new ZkrivkiRadioParser(getInstance()).start();
		
		new NRJParser(getInstance()).start();
		
		new MoskvaFMParser(getInstance()).start();
		new PiterFMParser(getInstance()).start();

		new UnistarParser(getInstance()).start();
		
		new RadioAlphaParser(getInstance()).start();

		new EuropaPlusParser(getInstance()).start();
		new EuropaPlusUAParser(getInstance()).start();
		
		new Radio4DukParser(getInstance()).start();
		
		new YerevanNightsRadioParser(getInstance()).start();

		new UnitonRadioParser(getInstance()).start();

		new OzbegimRadioParser(getInstance()).start();

		new FinamFMParser(getInstance()).start();
		
		new RadioZvezdaParser(getInstance()).start();
		
		new RadioSibirParser(getInstance()).start();
		
		new AutoradioParser(getInstance()).start();
		
		new RadioCafeParser(getInstance()).start();
		
		new AfterhoursFMParser(getInstance()).start();
		
		new RelaxFMParser(getInstance()).start();
		
		new Radio7Parser(getInstance()).start();
		new EldoRadioParser(getInstance()).start();

		new RadioJazzParser(getInstance()).start();
		new RadioClassicParser(getInstance()).start();
		
		new StationRUParser(getInstance(), "rusradio", 	  	"3").start();
		new StationRUParser(getInstance(), "dfm", 			"173").start();
//		new StationRUParser(getInstance(), "maximum", 		"176").start();
		new StationRUParser(getInstance(), "hitfm", 		"177").start();
		new StationRUParser(getInstance(), "montecarlo", 	"175").start();
		new StationRUParser(getInstance(), "hedkandi", 		"280").start();

		new PleerDotNasheParser(getInstance(), "best", 	  "88").start();
		new PleerDotNasheParser(getInstance(), "rufm", 	  "169").start();
		new PleerDotNasheParser(getInstance(), "ultra",   "111").start();
	}
	
	public static Metadata metadataForStation(String stationId)
	{
		return getInstance().stationsMetadata.get(stationId);
	}

	public static boolean containsMetadataForStation(String stationId)
	{
		return getInstance().stationsMetadata.containsKey(stationId);
	}

	///////////////////////////////////////////////////
	// Private methods
	///////////////////////////////////////////////////
	
	private void notifyObservers(final Metadata metadata)
	{
		Iterator<MetadataDelegate> iterator = observers.iterator();
		while(iterator.hasNext())
			iterator.next().didReceiveMetadata(metadata);
	}
	
	@Override
	public void didReceiveMetadataForStation(Metadata metadata) 
	{
		//app.TVRadioServerMain.logger.info(">>> didReceiveMetadataForStation: "+ metadata.stationId+", artist: "+ metadata.artistName+", song: "+ metadata.songName);

		// keep current stations metadata
		Metadata existingMetadata = stationsMetadata.get(metadata.stationId);
		if(existingMetadata == null || !existingMetadata.equals(metadata))
		{
			stationsMetadata.put(metadata.stationId, metadata);
			notifyObservers(metadata);
		}
	}
}
