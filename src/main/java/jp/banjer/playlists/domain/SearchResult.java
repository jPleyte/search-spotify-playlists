package jp.banjer.playlists.domain;

import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

/**
 * Each SearchResult
 * @author cp
 *
 */
public class SearchResult {
	private final PlaylistSimplified playlist;
	private final Track track;
	
	public SearchResult(PlaylistSimplified playlist, Track track) {
		this.playlist = playlist;
		this.track = track;
	}

	public PlaylistSimplified getPlaylist() {
		return playlist;
	}

	public Track getTrack() {
		return track;
	}
}
