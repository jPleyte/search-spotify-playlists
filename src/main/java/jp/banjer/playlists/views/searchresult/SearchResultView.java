package jp.banjer.playlists.views.searchresult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.VaadinSession;

import jakarta.annotation.security.PermitAll;
import jp.banjer.playlists.consumer.SpotifyConsumer;
import jp.banjer.playlists.domain.SearchResult;
import jp.banjer.playlists.security.SecurityUtils;
import jp.banjer.playlists.views.MainLayout;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

@PageTitle("Search Result")
@Route(value = "searchResult", layout = MainLayout.class)
@PermitAll
public class SearchResultView extends VerticalLayout implements BeforeEnterObserver {

	private Logger logger = LoggerFactory.getLogger(SearchResultView.class);
	
	private static final long serialVersionUID = 1L;
	
	public static final String PARAMETER_ARTIST = "artist";
	public static final String PARAMETER_SONG = "song";
	public static final String ATTRIBUTE_SEARCH_PARAMETERS = "SEARCH_PARAMETERS";
	
	private static final int MESSAGE_DURATION = 15000; // leave those messages up for 15s
	private static final Position MESSAGE_POSITION = Position.BOTTOM_START;
	
	private String artist;
	private String song;
	
	private Grid<SearchResult> searchResultGrid;

    private Text searchText = new Text("");;
    private Text searchCount = new Text("");
    
    private SpotifyConsumer spotifyConsumer;
    
    public SearchResultView(@Autowired SpotifyConsumer spotifyConsumer) {
    	this.spotifyConsumer = spotifyConsumer;
    	searchResultGrid = new Grid<>();

    	setMargin(true);
    	searchText = new Text("");
    	
    	configureGrid();
    	add(searchResultGrid);
    	
        add(searchText, searchCount);
    }
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
    	SecurityUtils.requireTokenAuthentication();
    	
//    	RouteParameters parameters = event.getRouteParameters();
    	Object p = VaadinSession.getCurrent().getSession().getAttribute(ATTRIBUTE_SEARCH_PARAMETERS);
    	if(p != null) {
    		RouteParameters parameters = (RouteParameters) p;        	
        	artist = parameters.get(PARAMETER_ARTIST).orElse("");
        	song = parameters.get(PARAMETER_SONG).orElse("");    		
    	}
    	
    	if(!StringUtils.isBlank(artist) || !StringUtils.isBlank(song)) {
    		searchText.setText("Searching for artist="+artist+", song="+song);
    		Notification.show("Searching for " + getSearchDescription(song, artist), MESSAGE_DURATION, MESSAGE_POSITION);
    	} else {
    		Notification.show("Oy! You didn't specify an artist or song to search for.", MESSAGE_DURATION, MESSAGE_POSITION);
    		return;
    	}

    	List<SearchResult> results= Collections.emptyList();
    	try {
			results = spotifyConsumer.getSearchResult(artist, song);
		} catch (ParseException | SpotifyWebApiException | URISyntaxException | IOException e) {
			logger.error("An error occurred while performing search: ", e);
			Notification.show("An error occurred while performing your search: " + e.getMessage(), MESSAGE_DURATION, Position.MIDDLE);
		}
    	
    	if(results.isEmpty()) {
			searchText.setText("There are no "+getSearchDescription(artist, song));
		} else {
			searchText.setText("Search results for " + getSearchDescription(artist, song));
			searchCount.setText(" (" + results.size() + ")");
			searchResultGrid.setItems(results);
		}
    }    
    
	/**
	 * Create a description of the search being performed. 
	 * @param song
	 * @param artist
	 * @return
	 */
	private String getSearchDescription(String artist, String song) {
		 if (!StringUtils.isBlank(artist) && !StringUtils.isBlank(song)) {
			 return String.format("playlists with the song %s by the artist %s ", song, artist); 
		} else if(!StringUtils.isBlank(artist) && StringUtils.isBlank(song)) {
			return String.format("playlists with any songs by the artist %s ", artist);
		} else if(StringUtils.isBlank(artist) && !StringUtils.isBlank(song)) {
			return String.format("playlists with the song %s by any artist ", song);
		} else {
			return "Unable to perform search because neither a song or artist was specified";
		}
	}

	private void configureGrid() {
		searchResultGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
		searchResultGrid.addThemeName("vaadin-grid-flow");
		
		searchResultGrid.addColumn(x -> x.getPlaylist().getName()).setHeader("Playlist");
		searchResultGrid.addColumn(x -> getArtistLabel(x.getTrack())).setHeader("Artist");
		searchResultGrid.addColumn(x -> x.getTrack().getAlbum().getName()).setHeader("Album");
		searchResultGrid.addColumn(x -> x.getTrack().getName()).setHeader("Song");
	}

	/**
	 * Return the artists on a track.  
	 * @param track
	 * @return
	 */
	private String getArtistLabel(Track track) {
		StringJoiner joiner = new StringJoiner(", ");
		for(ArtistSimplified artist: track.getArtists()) {
			joiner.add(artist.getName());
		}
		return joiner.toString();
	}

}
