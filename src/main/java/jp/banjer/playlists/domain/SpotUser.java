package jp.banjer.playlists.domain;

import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

public class SpotUser {
	
	private String userName;
	private AuthorizationCodeCredentials spotifyCredentials;
	
	public SpotUser(AuthorizationCodeCredentials credentials) {
		this.spotifyCredentials = credentials;
	}

	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public AuthorizationCodeCredentials getSpotifyCredentials() {
		return spotifyCredentials;
	}
	public void setSpotifyCredentials(AuthorizationCodeCredentials spotifyCredentials) {
		this.spotifyCredentials = spotifyCredentials;
	}
	
}
