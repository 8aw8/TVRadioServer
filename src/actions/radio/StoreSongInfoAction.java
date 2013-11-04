package actions.radio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import actions.Action;
import actions.ActionDelegate;

import database.Database;

public class StoreSongInfoAction extends Action
{
	private final String stationId;
	private final String songTitle;
	private final String songArtist;
	private final String songGenre;
	private final String songAlbumCoverLink;

	public StoreSongInfoAction(ActionDelegate delegate, Map<String, String> parameters) 
	{
		super(delegate, parameters);
		
		this.stationId 			= parameters.get("stationId");
		this.songTitle			= parameters.get("title");
		this.songArtist			= parameters.get("artist");
		this.songGenre			= parameters.get("genre");
		this.songAlbumCoverLink	= parameters.get("coverLink");
	}
	
	@Override
	public void start() 
	{
		Connection db = Database.getDBConnection();
		if (db != null) 
		{
			PreparedStatement preparedStatement = null;
			Statement statement = null;
			
			try 
			{
				String sql; 
				ResultSet resultSet;

				statement = db.createStatement();
			
				// 1. insert song info
				sql =  	"INSERT INTO songs (title,artist,cover_url) "+
						"VALUES (?,?,?) ON DUPLICATE KEY UPDATE title=?,artist=?,cover_url=?;";

				preparedStatement = db.prepareStatement(sql);
				preparedStatement.setString(1, songTitle==null?"":songTitle);
				preparedStatement.setString(2, songArtist==null?"":songArtist);
				preparedStatement.setString(3, songAlbumCoverLink==null?"null":songAlbumCoverLink);
				preparedStatement.setString(4, songTitle==null?"":songTitle);
				preparedStatement.setString(5, songArtist==null?"":songArtist);
				preparedStatement.setString(6, songAlbumCoverLink==null?"null":songAlbumCoverLink);
				preparedStatement.executeUpdate();
				preparedStatement.close();
				
				// 2. get song id
				sql = "SELECT `id` FROM `songs` WHERE title=? AND artist=? ORDER BY `id` DESC LIMIT 1";
				
				preparedStatement = db.prepareStatement(sql);
				preparedStatement.setString(1, songTitle==null?"":songTitle);
				preparedStatement.setString(2, songArtist==null?"":songArtist);
				
				resultSet = preparedStatement.executeQuery();
				resultSet.next();
				int songId = resultSet.getInt(1);
				resultSet.close();
				preparedStatement.close();
				
				// 3. check if this song not the last song played on this station
				resultSet = statement.executeQuery("SELECT song_id FROM songs_to_stations WHERE `songs_to_stations`.`station_id`='"+stationId+"' ORDER BY timestamp DESC LIMIT 1");
				int lastPlayedSongId = 0;
				if(resultSet.next())
					lastPlayedSongId = resultSet.getInt(1);
				resultSet.close();
				
				if(lastPlayedSongId != songId)
				{
					sql =  	"INSERT INTO songs_to_stations (song_id,station_id) "+
							" VALUES ('"+songId+"', '"+stationId+"');";
					
					statement.executeUpdate(sql);

					// 4. add genre
					if(songGenre!=null && !songGenre.equalsIgnoreCase("null") && songGenre.length() > 0)
					{
						sql =  	"INSERT IGNORE INTO genres (name) "+
								" VALUES ('"+songGenre+"');";
						
						statement.executeUpdate(sql);

						// 5. link genre
						resultSet = statement.executeQuery("SELECT `id` FROM `genres` ORDER BY `id` DESC LIMIT 1");
						resultSet.next();
						int genreId = resultSet.getInt(1);
						resultSet.close();

						sql =  	"INSERT INTO songs_to_genres (genre_id,song_id) "+
								" VALUES ('"+genreId+"', '"+songId+"');";
						
						statement.executeUpdate(sql);
					}
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			finally
			{
				if(statement != null) try { statement.close(); } catch (SQLException e) { /* ignore */ }
				if(preparedStatement != null) try { preparedStatement.close(); } catch (SQLException e) { /* ignore */ }
			}
		}
	}
}
