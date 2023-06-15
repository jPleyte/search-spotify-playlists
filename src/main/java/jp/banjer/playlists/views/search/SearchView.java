package jp.banjer.playlists.views.search;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.VaadinSession;

import jakarta.annotation.security.PermitAll;
import jp.banjer.playlists.views.MainLayout;
import jp.banjer.playlists.views.searchresult.SearchResultView;

@PageTitle("Search")
@Route(value = "search", layout = MainLayout.class)
@PermitAll
public class SearchView extends HorizontalLayout {

    private static final long serialVersionUID = 1L;
//    private static final String SEARCH_ROUTE_TEMPLATE = "searchResult/artist/%s/song/%s";
    
	private TextField artist;
    private TextField song;
    private Button search;
    
    public SearchView() {
    	artist = new TextField("Artist");
    	song = new TextField("Song");

        search = new Button("Search");
        search.addClickListener(e -> {
        	// I don't know what the best way is to pass parameters to the next page, when using getPage().setLocation; and I don't want
        	// to use navigate() because it doesn't select the the correct tab in the drawer. 
        	Map<String,String> parameters = new HashMap<>();
        	parameters.put("artist", artist.getValue());
        	parameters.put("song", song.getValue());
        	RouteParameters rp = new RouteParameters(parameters);
        	
        	VaadinSession.getCurrent().getSession().setAttribute(SearchResultView.ATTRIBUTE_SEARCH_PARAMETERS, rp);
        	
        	UI.getCurrent().getPage().setLocation("searchResult");
//        	UI.getCurrent().navigate(SearchResultView.class, rp);
        });

        search.addClickShortcut(Key.ENTER);        
        setMargin(true);

        setVerticalComponentAlignment(Alignment.END, artist, song, search);

        add(artist, song, search);
    }
}
