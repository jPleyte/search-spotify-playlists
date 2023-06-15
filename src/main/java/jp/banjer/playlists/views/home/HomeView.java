package jp.banjer.playlists.views.home;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;

import jakarta.annotation.security.PermitAll;
import jp.banjer.playlists.consumer.SpotifyConsumer;
import jp.banjer.playlists.security.SecurityUtils;
import jp.banjer.playlists.views.MainLayout;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

/**
 * Application home page 
 * @author 
 *
 */
@PageTitle("Home")
@Route(value = "home", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
public class HomeView extends HorizontalLayout {
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(HomeView.class);
	private SpotifyConsumer spotifyConsumer;
	
    public HomeView(@Autowired SpotifyConsumer spotifyConsumer) throws ParseException, SpotifyWebApiException, IOException, URISyntaxException {
    	this.spotifyConsumer = spotifyConsumer;
    	Button button = getButton(); 

        setMargin(true);
        setVerticalComponentAlignment(Alignment.END);
        add(button);
   }

	private Button getButton() throws ParseException, SpotifyWebApiException, IOException, URISyntaxException {
		if (SecurityUtils.getUser() == null) {
			return getLoginButton();
		} else {
			return getLogoutButton();
		}
	}

	private Button getLoginButton() {
    	Button loginButton = new Button("Connect to Spotify");
    	loginButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
    	loginButton.addClickShortcut(Key.ENTER);
		
    	loginButton.addClickListener(x -> getUI().ifPresent(ui -> {
			try {
				ui.getPage().setLocation(spotifyConsumer.generateAuthUri().toString());
				//	ui.getPage().open(spotifyConsumer.generateAuthUri().toString(), "_self");
			} catch (URISyntaxException e0) {
				logger.error("Unable to create auth link", e0);
				Notification.show("Error while generating auth uri: " + e0.getMessage());
			}
		}));
    	
    	return loginButton;
	}

	private Button getLogoutButton() {
		Button disconnectButton = new Button("Disconnect from Spotify");
		disconnectButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
		disconnectButton.addClickShortcut(Key.ENTER);

		disconnectButton.addClickListener(x -> getUI().ifPresent(ui -> {
			UI.getCurrent().getSession().close();		
			VaadinSession.getCurrent().getSession().invalidate();
			UI.getCurrent().getPage().setLocation("home");
		}));
		
		return disconnectButton;
	}

}
