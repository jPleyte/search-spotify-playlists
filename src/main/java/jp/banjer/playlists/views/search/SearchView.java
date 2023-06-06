package jp.banjer.playlists.views.search;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import jp.banjer.playlists.views.MainLayout;

@PageTitle("Search")
@Route(value = "search", layout = MainLayout.class)
@PermitAll
public class SearchView extends HorizontalLayout {

    private static final long serialVersionUID = 1L;
    private static final String SEARCH_ROUTE_TEMPLATE = "searchResult/artist/%s/song/%s";
    
	private TextField artist;
    private TextField song;
    private Button search;
    
    public SearchView() {
    	artist = new TextField("Artist");
    	song = new TextField("Song");

        search = new Button("Search");
        search.addClickListener(e -> {
        	UI.getCurrent().navigate(String.format(SEARCH_ROUTE_TEMPLATE, artist.getValue(), song.getValue()));
        });

        search.addClickShortcut(Key.ENTER);        
        setMargin(true);

        setVerticalComponentAlignment(Alignment.END, artist, song, search);

        add(artist, song, search);
    }
}
