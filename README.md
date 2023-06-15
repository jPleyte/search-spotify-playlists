# Search Spotify Playlists

Search your Spotify playlists for a particular artist or song

## Synopsis

When you perform a serach on the Spotify site you get results from all possible sources, including playlists created by others. Using spotify's app and web interface there isn't a way to confine a search to just your playlists. This app lets you search for artists and songs found on your playlists only. 

## Configuration

The application name is set using the ``application.title`` property. 

The following environment variables must be present:
* SPOTIFY_CLIENT_ID - Your Spotify-provided app-specific API id.
* SPOTIFY_CLIENT_SECRET - Secret key associated with client id.
* RYANSLIST_CALLBACK_URI - The URI Spotify redirects users to after the log in to Spotify.


## To Do

- [ ] Fill in the AboutView
- [ ] Show the spotify token time remaining
- [ ] allow user to logout (of app and spotify api). Get rid of the user widget at the bottom of the page. 
- [ ] come up with a release and versioning system. Git have something built in? 
- [ ] Modify the Home view so it tells the user they are logged in, what their name is, and how long their sessions is good for. 
- [ ] Prevent search when user hasn't authenticated with spotify
- [ ] Implement fuzzy vs strict search
- [ ] When playlist database is retrieved, all playlists are logged (for debugging). That's not necessary.

Copyright &copy; 2023 by Rex Z. 