package pakk.api;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.JsonObject;

import pakk.SortAction;

/**
 * Root resource (exposed at "twitter-api" path)
 */
@Path("twitter-api")
public class TwitterResource {

    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public String searchTwitter(
        @QueryParam("query") String query,
        @QueryParam("location") String location,
        @DefaultValue("3") @QueryParam("count") int count,
        @QueryParam("sort_on") String sortOn,
        @QueryParam("desc") boolean desc) {
        
        // Set max number of count to 15 even if higher value specified
        if (count > 15) count = 15;

        if (query == null || query.isEmpty()) {
            throwError("query can't be empty");
        }
        if (location == null || location.isEmpty()) {
            throwError("location can't be empty");
        }

        Integer field = null;
        if (sortOn != null) {
            switch (sortOn){
                case "author": field = SortAction.FIELD_AUTHOR; break;
                case "tweet": field = SortAction.FIELD_TWEET; break;
                case "date": field = SortAction.FIELD_DATE; break;
                default:
                    throwError("sort_on must be one of: [author, tweet, date]");
            }
        }

        JsonObject result = new JsonObject();
        try {
            result = TwitterHelper.getTweets(query, location, count, field, desc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result.toString();
    }
    
    @GET
    @Path("history")
    @Produces(MediaType.APPLICATION_JSON)
    public String getHistory() {
        
        // Fetch history from twitter helper and convert it into JSON String
        return TwitterHelper.fetchHistory().toString();
    }
    
    @POST
    @Path("favourites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String postFavourite(
        @NotNull @Size(min = 1) @FormParam("location") String location,
        @NotNull @Size(min = 1) @FormParam("radius") double radius,
        @FormParam("latitude") double latitude,
        @FormParam("longitude") double longitude,
        @DefaultValue("3") @FormParam("query_count") int queryCount,
        @DefaultValue("") @FormParam("query_keyword") String queryKeyword) { 

        if (location.isEmpty()) {
            throwError("Location is a required parameter.");
        }
        
        location = location.toLowerCase();

        JsonObject favourite = new JsonObject();
        favourite.addProperty("radius", radius);
        favourite.addProperty("latitude", latitude);
        favourite.addProperty("longitude", longitude);
        favourite.addProperty("query_count", queryCount);
        favourite.addProperty("query_keyword", queryKeyword);

        try {
            TwitterHelper.storeFavourite(location, favourite);
        } catch (Exception e) {
            throwError(e.getMessage());
            e.printStackTrace();
        }

        return "success"; 
    }
    
    @GET
    @Path("favourites")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFavourites() {
        
        // Fetch favourites from twitter helper and convert it into JSON String
        return TwitterHelper.fetchFavourites().toString();
    }
    
    @POST
    @Path("favourites/delete/{location}")
    public String deleteFavourite(@PathParam("location") String location) { 
        try {
            TwitterHelper.deleteFavourite(location);
        } catch (Exception e) {
            throwError(e.getMessage());
            e.printStackTrace();
        }
        
        return "success";
    }

    /**
     * This method will return a 400 (HTTP Bad Request) response. 
     *
     * @param error
     */
    private void throwError(String error) {
        // Convert the error into a JSON response
        error = "{\"error\": \"" + error + "\"}";
        // throw exception which will result in HTTP Status code 400 (Bad Request)
        throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(error).build());
    }
}

