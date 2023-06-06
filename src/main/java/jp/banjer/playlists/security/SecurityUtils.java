package jp.banjer.playlists.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

import jp.banjer.playlists.domain.SpotUser;
import jp.banjer.playlists.views.home.HomeView;

public class SecurityUtils {
	private static Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
	public static final String ATTRIBUTE_USER = "spp_application_user";
	
    private SecurityUtils() {
        // Util methods only
    }

	public static SpotUser getUser() {
		SpotUser user = (SpotUser) VaadinSession.getCurrent().getSession().getAttribute(ATTRIBUTE_USER);
		
		if(user == null) {
			logger.warn("AuthorizationCodeCredentials not found in request");
		}
		
		return user;
	}

    public static void storeSpotifyCredentials(SpotUser user) {
    	VaadinSession.getCurrent().getSession().setAttribute(ATTRIBUTE_USER, user);
    	VaadinSession.getCurrent().getSession().setMaxInactiveInterval(-1);
    }

    /**
     * Check for the spotify token. If it is not present redirect to home page.
     */
	public static void requireTokenAuthentication() {
		if(getUser() == null) {
			UI.getCurrent().navigate(HomeView.class);
		}
		
	}

}
