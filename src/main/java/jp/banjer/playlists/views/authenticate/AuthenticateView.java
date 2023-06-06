package jp.banjer.playlists.views.authenticate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import jp.banjer.playlists.consumer.SpotifyConsumer;
import jp.banjer.playlists.domain.SpotUser;
import jp.banjer.playlists.security.SecurityUtils;
import jp.banjer.playlists.views.MainLayout;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

/**
 * This is the page that the user is returned to after authenticating with spotify
 *
 */
@PageTitle("Authenticate")
@Route(value = "authenticate", layout = MainLayout.class)
@PermitAll
public class AuthenticateView extends VerticalLayout implements HasUrlParameter<String>, AfterNavigationObserver {

	private static final long serialVersionUID = 1L;

	private Logger logger = LoggerFactory.getLogger(AuthenticateView.class);
	
	private SpotifyConsumer spotifyConsumer;
	
	public AuthenticateView(@Autowired SpotifyConsumer spotifyConsumer) {
		this.spotifyConsumer = spotifyConsumer;
		add(new Label("Authentication"));
	}
	
	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		Location location = event.getLocation();
	    QueryParameters queryParameters = location.getQueryParameters();
	    Map<String, List<String>> parametersMap = queryParameters.getParameters();
	    List<String> values =  parametersMap.get("code");
	    
	    if(values.size() != 1) {
	    	logger.warn("Single parameter `code` expected but received " + values.size());
	    	add(new Label("problem."));
	    } else {
	    	String code = values.get(0);
	    	try {
				AuthorizationCodeCredentials authorizationCodeCredentials = spotifyConsumer.getAuthorizationCodeCredentials(code);
				
				// Place credentials object in session 
				SpotUser user = new SpotUser(authorizationCodeCredentials);
				SecurityUtils.storeSpotifyCredentials(user);
				
				// Once credentials are in session, send a request for the userName and add it to the credentials object. 
				String userName = spotifyConsumer.getUser();
				user.setUserName(userName);
				
				//	UI.getCurrent().navigate(SearchView.class);
				UI.getCurrent().getPage().setLocation("search");
				
			} catch (ParseException | SpotifyWebApiException | IOException | URISyntaxException e) {
				logger.warn("Unable to get auth code from spotify");
				
				// TODO Figure out how to make this show up as a warning or error
				Notification.show("Unable to authenticate with Spotify. Please try again");
			}
	    }
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		logger.warn("calling afterNavigation");
	}
}
