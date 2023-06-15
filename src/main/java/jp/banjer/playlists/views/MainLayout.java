package jp.banjer.playlists.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIcon;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jp.banjer.playlists.components.appnav.AppNav;
import jp.banjer.playlists.components.appnav.AppNavItem;
import jp.banjer.playlists.consumer.SpotifyConsumer;
import jp.banjer.playlists.security.SecurityUtils;
import jp.banjer.playlists.views.about.AboutView;
import jp.banjer.playlists.views.home.HomeView;
import jp.banjer.playlists.views.search.SearchView;
import jp.banjer.playlists.views.searchresult.SearchResultView;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

	private static final long serialVersionUID = 1L;

	@Autowired 
	SpotifyConsumer spotifyConsumer;
	
    private H2 viewTitle;

    public MainLayout(@Autowired SpotifyConsumer spotifyConsumer) {
    	this.spotifyConsumer = spotifyConsumer;
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        H1 appName = new H1(spotifyConsumer.getApplicationName() + " v"+spotifyConsumer.getApplicationVersion());
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private AppNav createNavigation() {
        // AppNav is not an official component.
        // For documentation, visit https://github.com/vaadin/vcf-nav#readme
        // Starting with v24.1, AppNav will be replaced with the official
        // SideNav component.
        AppNav nav = new AppNav();

        nav.addItem(new AppNavItem("About", AboutView.class, LineAwesomeIcon.INFO_CIRCLE_SOLID.create()));
        nav.addItem(new AppNavItem("Home", HomeView.class, LineAwesomeIcon.HOME_SOLID.create()));

        AppNavItem search = new AppNavItem("Search", SearchView.class, LineAwesomeIcon.SEARCH_SOLID.create());
        search.setVisible(SecurityUtils.getUser() != null);
        nav.addItem(search);
        
    	AppNavItem searchResultItem = new AppNavItem("SearchResult", SearchResultView.class, LineAwesomeIcon.SPOTIFY.create());
    	boolean isSearchParametersInSession = VaadinSession.getCurrent().getSession().getAttribute(SearchResultView.ATTRIBUTE_SEARCH_PARAMETERS) != null;    	
    	searchResultItem.setVisible(SecurityUtils.getUser() != null && isSearchParametersInSession);
        nav.addItem(searchResultItem);
        
        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
