package pakk;

import java.util.ArrayList;
import java.util.List;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.Query.ResultType;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


public class MyTwitterSearch implements TwitterSearch {

	private TwitterFactory tf;
	public MyTwitterSearch() {
		ConfigurationBuilder cb = new ConfigurationBuilder()
		.setDebugEnabled(true)
		.setApplicationOnlyAuthEnabled(true)
		.setOAuthConsumerKey("Z1W25xq5ARkskzsNQZ8oUSrsJ")
		.setOAuthConsumerSecret("Xw1cfAPFgzS7Jejzmn5dZnD0EMpmwhhGd62fO6ss9PrmSTvZi5");

		tf = new TwitterFactory(cb.build());
	}
	@Override
	public List<Tweet> getTweets(TwitterQuery query) {

		List<Tweet> tweets = new ArrayList<Tweet>();
		twitter4j.Twitter twitter = tf.getInstance();

		// Get the OAuth2Token using Consumer Key & Secret
		try {
			twitter.getOAuth2Token();
		} catch (TwitterException e) {
			System.out.println("Auth generation failed: " + e);
			System.exit(1);
		}

		try {
			// Set Query parameters

			// Set Query & count
			Query q = new Query(((MyTwitterQuery)query).keyword)
				.count(query.getCount())
				.resultType(ResultType.recent);

			// Check if GeoSet and add to query
			if (query.isGeoSet()){
				GeoLocation g = new GeoLocation(query.getLatitude(), query.getLongitude());
				q.geoCode(g, query.getRadius(), "km");
			}

			// Search using our Query
			QueryResult result= twitter.search(q);

			// Add results to the list
			for (Status tweet : result.getTweets()) {
			    String text = tweet.getText();
			    if (tweet.isRetweet()) {
			        text = tweet.getRetweetedStatus().getText();
			    }
			    tweets.add(new MyTweet(text, tweet.getUser().getScreenName(), tweet.getCreatedAt()));
			}
		} catch (TwitterException te) {
			System.out.println("Failed to search tweets: " + te.getMessage());
			System.exit(1);
		}
		return tweets;
	}

}
