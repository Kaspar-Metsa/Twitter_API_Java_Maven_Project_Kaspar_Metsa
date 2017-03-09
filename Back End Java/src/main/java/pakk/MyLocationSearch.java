package pakk;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.ParseException;
import org.apache.http.client.fluent.Request;

import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;


public class MyLocationSearch implements LocationSearch{

	private class Point {
		double lat;
		double lon;
		
		Point(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}
		
	}
	
	private double getDistance(Point x, Point y) {

	    final int R = 6371; // Radius of the earth

	    Double latDistance = deg2rad(Math.abs(x.lat - y.lat));
	    Double lonDistance = deg2rad(Math.abs(x.lon - y.lon));
	    
	    Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
	            + Math.cos(deg2rad(x.lat)) * Math.cos(deg2rad(y.lat))
	            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
	    
	    Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    
	    return R * c;
	    
	}

	private double deg2rad(double deg) {
	    return (deg * Math.PI / 180.0);
	}
	
	@Override
	public TwitterQuery getQueryFromLocation(String location) {
		TwitterQuery tQ = new MyTwitterQuery(location);
		
		String TARGET = "http://nominatim.openstreetmap.org/search?q=";
		
		try {
			// Encode location to handle spaces
			TARGET += URLEncoder.encode(location, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
		}

		TARGET += "&format=jsonv2";
		try {
			String jsonString = Request.Get(TARGET)
					.execute()
					.returnContent()
					.asString();
			
			// We work on the first Object of the response
			JSONArray arr = new JSONArray(jsonString);
			
			for(int i=0; i< arr.length(); i++){
				
				JSONObject obj = arr.getJSONObject(i);
				Point p = new Point(
						Double.valueOf(obj.getString("lat")),
						Double.valueOf(obj.getString("lon"))
						);
				
				// Find the nearest point in the box			
				JSONArray bbox = obj.getJSONArray("boundingbox");
				
				// The bounding box has 4 points
				// We can get perpendicular distance to the sides too
				// But we'll just get distance between our point and the corners for simplicity
				
				Point[] corners = {
						new Point(bbox.getDouble(0),bbox.getDouble(2)),
						new Point(bbox.getDouble(0),bbox.getDouble(3)),
						new Point(bbox.getDouble(1),bbox.getDouble(2)),
						new Point(bbox.getDouble(1),bbox.getDouble(3))
				};
				
				double radius = Double.MAX_VALUE;
				
				for(Point x: corners) {
					radius = Math.min(getDistance(x, p), radius);
				}
				
				if (radius == 0.0){
					// Skip single points, we need a region
					continue;
				}
					
				
				else{
					// Set the TwitterQuery data and break
					tQ.setLatitude(p.lat);
					tQ.setLongitude(p.lon);			
					tQ.setRadius(radius);
					break;
				}				

			}
			
						
			
		} catch (IOException e) {
			System.out.println("Error while connecting to the OSM API.");
		} catch (ParseException|JSONException e) {
			System.out.println("Error while parsing the JSON response: " + e);
		} 
		return tQ;

	}

}
