package jp.banjer.playlists.consumer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.vaadin.flow.server.VaadinSession;

import jp.banjer.playlists.domain.PlaylistDatabase;
import jp.banjer.playlists.domain.SearchResult;
import jp.banjer.playlists.domain.SpotUser;
import jp.banjer.playlists.security.SecurityUtils;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

@Component
public class SpotifyConsumer {

	private Logger logger = LoggerFactory.getLogger(SpotifyConsumer.class);

	@Value("${spotify.client.id}")
	private String clientId;

	@Value("${spotify.client.secret}")
	private String clientSecret;

	@Value("${ryanslist.callback.uri}")
	private String callbackUri;

	/**
	 * Configure and return the api
	 * 
	 * @return
	 * @throws URISyntaxException
	 */
	private SpotifyApi getSpotifyApi() throws URISyntaxException {
		Optional<SpotUser> user = Optional.ofNullable(SecurityUtils.getUser());

		if (user == null) {
			logger.error("User has not authenticated");
			return null;
		}

		return new SpotifyApi.Builder().setClientId(clientId).setClientSecret(clientSecret)
				.setRedirectUri(new URI(callbackUri))
				.setAccessToken(user.map(SpotUser::getSpotifyCredentials)
						.map(AuthorizationCodeCredentials::getAccessToken).orElse(null))
				.setRefreshToken(user.map(SpotUser::getSpotifyCredentials)
						.map(AuthorizationCodeCredentials::getRefreshToken).orElse(null))
				.build();
	}

	/**
	 * Get the list of songs on a playlist
	 * 
	 * @param playlistId
	 * @return
	 * @throws URISyntaxException
	 * @throws ParseException
	 * @throws SpotifyWebApiException
	 * @throws IOException
	 */
	private PlaylistTrack[] getPlaylistItems(String playlistId)
			throws URISyntaxException, IOException, ParseException, SpotifyWebApiException {

		SpotifyApi spotifyApi = getSpotifyApi();

		final int itemsRequestLimit = 100;

		List<PlaylistTrack> allSongs = new ArrayList<>();
		int pageOffset = 0;
		Paging<PlaylistTrack> tracks;
		do {
			tracks = spotifyApi.getPlaylistsItems(playlistId).limit(itemsRequestLimit).offset(pageOffset).build()
					.execute();

			allSongs.addAll(Arrays.asList(tracks.getItems()));

			logger.debug("Query for playlist songs: id={}, received={}/{}, limit={}, offset={}", playlistId,
					allSongs.size(), tracks.getTotal(), tracks.getLimit(), pageOffset);
			pageOffset += itemsRequestLimit;
		} while (!StringUtils.isBlank(tracks.getNext()));

		return allSongs.toArray(new PlaylistTrack[allSongs.size()]);
	}

	/**
	 * Return the URI that a user follows to log into spotify and generate a session token. 
	 * @return
	 * @throws URISyntaxException
	 */
	public URI generateAuthUri() throws URISyntaxException {

		SpotifyApi spotifyApi = getSpotifyApi();
		AuthorizationCodeUriRequest request = spotifyApi.authorizationCodeUri()
				// .state("x4xkmn9pu3j6ukrs8n")
				.scope(getDelimitedScopes()).show_dialog(false).build();

		URI uri = request.execute();
		logger.info("User auth URI: " + uri.toString());
		return uri;
	}

	public AuthorizationCodeCredentials getAuthorizationCodeCredentials(String code)
			throws URISyntaxException, ParseException, SpotifyWebApiException, IOException {
		SpotifyApi spotifyApi = getSpotifyApi();
		return spotifyApi.authorizationCode(code).build().execute();
	}

	/**
	 * Return a space-separated list of scopes (permissions) which this application
	 * needs access to
	 * 
	 * @return
	 */
	private static final String getDelimitedScopes() {
		StringJoiner joiner = new StringJoiner(" ");
		// joiner.add("user-read-email");
		joiner.add("playlist-read-private");
		joiner.add("playlist-read-collaborative");
		return joiner.toString();
	}

	/**
	 * 
	 * @return
	 * @throws ParseException
	 * @throws SpotifyWebApiException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public String getUser() throws ParseException, SpotifyWebApiException, IOException, URISyntaxException {
		SpotifyApi spotifyApi = getSpotifyApi();
		User user = spotifyApi.getCurrentUsersProfile().build().execute();
		return user.getDisplayName();
	}

	private PlaylistDatabase getPlayListDatabase()
			throws ParseException, SpotifyWebApiException, URISyntaxException, IOException {

		PlaylistDatabase playlistDatabase = VaadinSession.getCurrent().getAttribute(PlaylistDatabase.class);

		if (playlistDatabase == null) {

			StopWatch playlistBuildTime = new StopWatch();
			playlistBuildTime.start();

			MultiValuedMap<PlaylistSimplified, PlaylistTrack> playlistSongs = new ArrayListValuedHashMap<>();

			for (PlaylistSimplified playlist : getPlaylists()) {
				PlaylistTrack[] tracks = getPlaylistItems(playlist.getId());
				logger.debug("Playlist=" + playlist.getName() + " has " + tracks.length + " songs");
				playlistSongs.putAll(playlist, Arrays.asList(tracks));
			}

			playlistBuildTime.stop();

			playlistDatabase = new PlaylistDatabase();
			playlistDatabase.setPlaylistSongs(playlistSongs);
			playlistDatabase.update();

			VaadinSession.getCurrent().setAttribute(PlaylistDatabase.class, playlistDatabase);

			logger.info(String.format("Read %d playlists with %d total tracks in %f seconds",
					playlistDatabase.getPlaylistSongs().keySet().size(),
					playlistDatabase.getPlaylistSongs().values().size(), (playlistBuildTime.getTime() / 1000.0)));
		} else {
			logger.info("Using cached playlist database from " + playlistDatabase.getAgeInMinutes() + "m ago.");
		}

		return playlistDatabase;
	}

	private PlaylistSimplified[] getPlaylists()
			throws URISyntaxException, ParseException, SpotifyWebApiException, IOException {
		SpotUser user = SecurityUtils.getUser();

		SpotifyApi spotifyApi = getSpotifyApi();
		spotifyApi.setAccessToken(user.getSpotifyCredentials().getAccessToken());
		spotifyApi.setRefreshToken(user.getSpotifyCredentials().getRefreshToken());

		final int playlistRequestLimit = 20;
		int pageOffset = 0;
		List<PlaylistSimplified> allPlaylists = new ArrayList<>();
		Paging<PlaylistSimplified> playlists;

		do {
			playlists = spotifyApi.getListOfCurrentUsersPlaylists()
					.limit(playlistRequestLimit)
					.offset(pageOffset)
					.build()
					.execute();

			allPlaylists.addAll(Arrays.asList(playlists.getItems()));

			logger.debug("Query for playlists: received={}/{}, limit={}, offset={}", allPlaylists.size(),
					playlists.getTotal(), playlists.getLimit(), pageOffset);
			pageOffset += playlistRequestLimit;
		} while (!StringUtils.isBlank(playlists.getNext()));

		// TODO after making a request the refresh token is probably updated right?
		return allPlaylists.toArray(new PlaylistSimplified[allPlaylists.size()]);
	}

	/**
	 * Return playlists that have the artist and/or song.
	 * @param artist
	 * @param song
	 * @return
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * @throws SpotifyWebApiException 
	 * @throws ParseException 
	 */
	public List<SearchResult> getSearchResult(String artist, String song) throws ParseException, SpotifyWebApiException, URISyntaxException, IOException {
		PlaylistDatabase playlistDatabase = getPlayListDatabase();
		
		List<SearchResult> results = new ArrayList<>();
		Set<PlaylistSimplified> playlists = getPlayListDatabase().getPlaylistSongs().keySet();
		
		for(PlaylistSimplified playlist: playlists) {
			logger.info("Searching playlist " + playlist.getName());

			for(PlaylistTrack pTrack: playlistDatabase.getPlaylistSongs().get(playlist)) {
				Track track = (Track)pTrack.getTrack();
				logger.debug("  Track=" + track.getName()+", Artist(s)="+Arrays.asList(track.getArtists()).stream()
						.map(ArtistSimplified::getName)
						.collect(Collectors.joining(", ")));
				
				if(!StringUtils.isBlank(song) && track.getName().toLowerCase().contains(song.toLowerCase())) {
					logger.debug("    Song matches song search term: " + song);
					results.add(new SearchResult(playlist, track));
				} else if(!StringUtils.isBlank(artist) && Arrays.asList(track.getArtists()).stream()
						.map(ArtistSimplified::getName)
						.map(String::toLowerCase)
						.filter(x -> x.contains(artist))
						.findAny()
						.isPresent()) {
					logger.debug("    Artist matches artist search term: " + artist);
					results.add(new SearchResult(playlist, track));
				} else {
					logger.debug("  Track " + track.getName() + " does not match search terms.");
				}
			}
		}
		
		return results;
	}
}
