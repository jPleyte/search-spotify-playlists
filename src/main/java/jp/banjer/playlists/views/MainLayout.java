package jp.banjer.playlists.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jp.banjer.playlists.components.appnav.AppNav;
import jp.banjer.playlists.components.appnav.AppNavItem;
import jp.banjer.playlists.security.SecurityUtils;
import jp.banjer.playlists.views.about.AboutView;
import jp.banjer.playlists.views.home.HomeView;
import jp.banjer.playlists.views.search.SearchView;
import jp.banjer.playlists.views.searchresult.SearchResultView;

import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

	private static final long serialVersionUID = 1L;
	
    private H2 viewTitle;

    public MainLayout() {
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
        H1 appName = new H1("Search Playlists");
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
        
    	AppNavItem searchResultItem = new AppNavItem("SearchResult", SearchResultView.class, LineAwesomeIcon.SPOTIFY.create());
    	searchResultItem.setVisible(SecurityUtils.getUser() != null);
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
