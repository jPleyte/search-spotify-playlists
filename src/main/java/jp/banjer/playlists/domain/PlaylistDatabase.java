package jp.banjer.playlists.domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.apache.commons.collections4.MultiValuedMap;

import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

/**
 * All playlists and songs 
 * @author j
 *
 */
public class PlaylistDatabase {
	private MultiValuedMap<PlaylistSimplified, PlaylistTrack> playlistSongs;
	private LocalDateTime lastUpdated;
	
	public void update() {
		lastUpdated = LocalDateTime.now();
	}
	
	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public MultiValuedMap<PlaylistSimplified, PlaylistTrack> getPlaylistSongs() {
		return playlistSongs;
	}
	public void setPlaylistSongs(MultiValuedMap<PlaylistSimplified, PlaylistTrack> playlistSongs) {
		this.playlistSongs = playlistSongs;
	}
	
	/**
	 * Return the number of minutes that have passed since the database was last refreshed.
	 * @return
	 */
	public long getAgeInMinutes() {
		LocalDateTime now = LocalDateTime.now();
		return ChronoUnit.MINUTES.between(now, lastUpdated);
		
	}
}
