package metadata;

import java.util.HashMap;
import java.util.Map;

public class Metadata 
{
	public final String stationId;
	public final String artistName;
	public final String songName;
	
	public Metadata(String stationId, String artistName, String songName)
	{
		this.stationId 	= stationId;
		this.artistName = artistName;
		this.songName 	= songName;
	}
	
	public String jsonRepresentation()
	{
		Map<String, String> songInfo = new HashMap<String, String>();

		songInfo.put("stationId", stationId);
		
		if(util.Util.notEmpty(artistName))
			songInfo.put("artist", artistName);
		
		if(util.Util.notEmpty(songName))
			songInfo.put("title", songName);

		Map<String, Object> metadataInfo = new HashMap<String, Object>();
		metadataInfo.put("metadata", songInfo);
		
		return util.writer.json.JsonWriter.toJSONString(metadataInfo);
	}
	
	public boolean equals(Object object)
	{
		if(object instanceof Metadata)
		{
			Metadata metadata  = (Metadata)object;

			boolean stationIdEquals	 	= (stationId == null  && metadata.stationId == null)  || (stationId != null  && metadata.stationId != null  && stationId.equals(metadata.stationId));
			boolean artistNameEquals 	= (artistName == null && metadata.artistName == null) || (artistName != null && metadata.artistName != null && artistName.equals(metadata.artistName));
			boolean songNameEquals 		= (songName == null   && metadata.songName == null)   || (songName != null   && metadata.songName != null   && songName.equals(metadata.songName));
			
			return stationIdEquals && artistNameEquals && songNameEquals;
		}
		
		return false;
	}
}
