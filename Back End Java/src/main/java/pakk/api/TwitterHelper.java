package pakk.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pakk.Cache;
import pakk.LocationSearch;
import pakk.MyCache;
import pakk.MyLocationSearch;
import pakk.MyTwitterQuery;
import pakk.MyTwitterSearch;
import pakk.SortAction;
import pakk.Tweet;
import pakk.TwitterSearch;

/**
 * Helper class for our API.
 */
public class TwitterHelper {

    private static final String FILE_LOC = "/home/Kaspar.Metsa/HW2/";
    private static final String CACHE_FILE = FILE_LOC + "2places.csv";
    private static final String HISTORY_FILE = FILE_LOC + "history.txt";
    private static final String FAVOURITES_FILE = FILE_LOC + "favourites.txt";

    private static Cache cache;
    private static LocationSearch locationSearch = new MyLocationSearch();
    private static TwitterSearch twitterSearch = new MyTwitterSearch();
    
    // Gson is used for manipulating JSON data
    private static Gson gson = new Gson();

    static {
        // Initialize cache
        cache = new MyCache();
        cache.setCacheFilename(CACHE_FILE);
        cache.init();
    }

    /**
     * Returns list of tweets based on the search parameters
     *
     * @param keyword
     * @param location
     * @param count
     * @param sortField
     * @param descending
     *
     * @return - Json object with list of tweets and the query.
     *
     * @throws Exception
     */
    public static JsonObject getTweets(String keyword, String location, int count, 
            Integer sortField, boolean descending) throws Exception {

        // Get the twitter query from the cache for this location
        MyTwitterQuery tQ = (MyTwitterQuery) cache.getQueryFromCache(location);
    
        // If the response is null, this means not in cache
        // OR If the coordinates not present in cache
        // In both of above cases, we need to fetch from OSM(OpenStreetMaps) & update in cache
        if (tQ == null || !tQ.isGeoSet()){
            // Need to fetch whole data from OSM & update in cache
            if (tQ != null) location = tQ.getLocation();
    
            tQ = (MyTwitterQuery) locationSearch.getQueryFromLocation(location);
    
            // Check if valid location found
            if ( !tQ.isGeoSet() ){
                throw new Exception("Invalid location specified.");
            }
            // If valid location, add the location to cache
            cache.addLocation(tQ);
        }
    
        // Set the count
        tQ.setCount(count);
        
        // Set the keyword
        tQ.keyword = keyword;
    
        // Perform Query
        List<Tweet> tweets = twitterSearch.getTweets(tQ);
        
        // (Optional) Apply sorting if required
        if (sortField != null) {
            tweets = sortTweets(tweets, sortField, descending);
        }
        
        // Store these results in history
        if (!tweets.isEmpty()) {
            storeSearchResult(tQ, tweets);
        }
        
        JsonObject tqObj =  gson.toJsonTree(tQ).getAsJsonObject();
        
        // Set is_favourite if this location is already a part of favourites
        tqObj.addProperty("is_favourite", fetchFavourites().has(location.toLowerCase()));

        JsonObject result = new JsonObject();
        result.add("tweets", gson.toJsonTree(tweets));
        result.add("query", tqObj);        
        
        return result;
    }
    
    /**
     * Sorts the tweets based on the sorting field and the mentioned order.
     *
     * @param tweets
     * @param sortField
     * @param desc
     *
     * @return - Sorted list of tweets
     */
    public static List<Tweet> sortTweets(List<Tweet> tweets, int sortField, boolean desc) {
        // We use a custom sorting function for this
        Collections.sort(tweets, new Comparator<Tweet>() {
            @Override
            public int compare(Tweet t1, Tweet t2) {
                int result = 0;
                switch(sortField){
                    case SortAction.FIELD_AUTHOR:
                        result = t1.getUser().compareToIgnoreCase(t2.getUser());
                        break;

                    case SortAction.FIELD_TWEET:
                        result = t1.getText().compareToIgnoreCase(t2.getText());
                        break;

                    case SortAction.FIELD_DATE:
                        result = t1.getTimestamp().compareTo(t2.getTimestamp());
                        break;
                }
                // If descending order needed, we change the sign of result
                if (desc)
                    result = -result;

                return result;
            }
        });
        
        return tweets;
    }
    
    /**
     * This method stores the search query & the results in the history file.
     * History file is a text file in which each line represents a search.
     * We are using JSON because it'll be easier to parse on UI when requested. 
     * 
     * @param tQ
     * @param tweets
     */
    public static void storeSearchResult(MyTwitterQuery tQ, List<Tweet> tweets) {
        
        // Create a JSON object for this search 
        JsonObject newSearch = new JsonObject();
        // Add query to the object
        newSearch.add("query", gson.toJsonTree(tQ));
        // And the results to the object
        newSearch.add("results", gson.toJsonTree(tweets));

        Path filePath = Paths.get(HISTORY_FILE);
        
        // Prefix a new line before adding to file
        String historyElement = System.lineSeparator() + newSearch;

        try {
            // Append the JSON string to the end of history file 
            Files.write(filePath, historyElement.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Failed to append history to file: ");
            e.printStackTrace();
            return;
        }
    }


    /**
     * Returns the search history as a JSON Array.
     * Why JSON Array? So that we can easily loop through each history and display on UI.
     *
     * @return searchResults - JSON String
     */
    public static JsonArray fetchHistory() {
        JsonArray searchResults = new JsonArray();
        
        // Read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(HISTORY_FILE))) {

            // Loop through all lines in the history file 
            stream.forEach(line-> {
                if (line.isEmpty()) return; // Skip empty lines
                // Parse each line in the file as a JSON object and add to history array 
                searchResults.add(new JsonParser().parse(line).getAsJsonObject());
            });

        } catch (NoSuchFileException e) {
            // Can safely ignore
        } catch (IOException e) {
            System.out.println("Failed to read history file: ");
            e.printStackTrace();
        }

        return searchResults;
    }
    
    /**
     * Stores the location as favourite.
     * (Optional) Also, stores the associated query.
     *
     * @param location
     * @param radius
     * @param latitude
     * @param longitude
     * @param tQ
     *
     * @throws Exception 
     */
    public static void storeFavourite(String location, JsonObject favourite) throws Exception {
        JsonObject favourites = fetchFavourites();
        
        if (favourites.has(location)) {
            throw new Exception("Location already exists in favourites");
        }
        
        // Add this favourite to the list of favourites.
        favourites.add(location.toLowerCase(), favourite);
        writeFavouritesToFile(favourites.toString());
    }
    
    /**
     * Deletes the location from favourites.
     *
     * @param location
     *
     * @throws Exception 
     */
    public static void deleteFavourite(String location) throws Exception {
        JsonObject favourites = fetchFavourites();
        
        if (!favourites.has(location)) {
            throw new Exception("Location not found in favourites");
        }

        // Remove this favourite to the list of favourites.
        favourites.remove(location);
        writeFavouritesToFile(favourites.toString());
    }
    
    /**
     * Returns the favourites history as a JSON Object.
     * Why JSON Object? So that we can build key-value pairs
     *
     * @return favourites - JSON String
     */
    public static JsonObject fetchFavourites() {
        JsonObject favourites = new JsonObject();
        
        try {
            String fileContents = new String(Files.readAllBytes(Paths.get(FAVOURITES_FILE)));
            
            if (!fileContents.isEmpty()) return new JsonParser().parse(fileContents).getAsJsonObject();
        } catch (NoSuchFileException e) {
            // Can safely ignore
        } catch (IOException e) {
            System.out.println("Failed to read favourites file: ");
            e.printStackTrace();
        }

        return favourites;
    }
    
    private static void writeFavouritesToFile(String favourites) {
        
        System.out.println(favourites);

        Path filePath = Paths.get(FAVOURITES_FILE);

        try {
            // Write the JSON string to the favourites file
            Files.write(filePath, favourites.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("Failed to write favourites to file: ");
            e.printStackTrace();
            return;
        }
    }
}

