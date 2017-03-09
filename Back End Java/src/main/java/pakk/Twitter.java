package pakk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;


public class Twitter implements TwitterApplication {

	private Cache cache;
	private TwitterQuery tQ;
	private List<Tweet> tweets;
	private LocationSearch locationSearch;
	private TwitterSearch twitterSearch;
	// Global variable to store the value of count in interactive mode
	private int gCount = 15;
	// Boolean to check whether global count is set or not
	boolean gCountSet = false;

	public Twitter(){
		// Our constructor, called when Twitter object created

		// Initialize the cache
		Cache cache = new MyCache();
		cache.setCacheFilename("D:\\2places.csv");
		// Set cache to this object
		this.setCache(cache);
		// Set LocationSearch to a new MyLocationSearch object
		this.setLocationSearch(new MyLocationSearch());
		// Set TwitterSearch to a new MyTwitterSearch object
		this.setTwitterSearch(new MyTwitterSearch());
	}

	public static void printUsage(boolean exit){
		// This function prints the usage & optionally exits the program if boolean exit is set to true
		System.out.println(
				"\nMinimal Usage: java Twitter [location]"
				+ "\nAdvanced Usage: java Twitter [location] -count [num] -sort [field] [asc|desc] -search [keyword]"
				+ "\n\n\t-help Displays this message."
				+ "\n\t-count num defines the number of tweets that are going to be displayed. The default is 15."
				+ "\n\t-sort field [asc|desc]: Defines sorting. Default sorting is ascending."
				+ "\n\tField can be author, date, tweet."
				+ "\n\t-search keyword Only tweets with keywords are displayed.");

		if(exit)
			System.exit(1);
	}

	@Override
	public void runWithArgs(String[] args) {

		// Invoke the getActionsFromArguments to get actions from arguments
		List<Action> actions = getActionsFromArguments(args);

		// Execute each action in the list
		for(Action action: actions){
			executeAction(action);
		}
		// Execute the print action finally
		executeAction(new MyPrintAction());

	}

	@Override
	public void runInteractive() {
		System.out.println("Interactive Mode..");

		// Scanner is used to take inputs from command line
		Scanner in = new Scanner(System.in);
		// Temporary string to store the incoming commands
		String input;

		do{
			// NextLine function reads next line from console & stores to input variable
			input = in.nextLine();

			// Get actions from input, same as above
			List<Action> actions = getActionsFromInput(input);

			// Execute each action, same as above
			for(Action action:actions)
				executeAction(action);

			// Keep asking input till exit
			// This loop will not end until the value of "input" variable equals "exit"
		} while(!input.equals("exit")) ;

		// We close the above created scanner object to avoid resource leaks
		in.close();

	}

	@Override
	public List<Action> getActionsFromInput(String action) {

		// This function creates a list of Action objects from

		// We split the incoming string line into words. So "query 1 hi hello" becomes an array containg query,1,hi & hello.

		String[] args = action.split("\\s+"); // Split on space

		// An empty list of actions
		List<Action> actions = new ArrayList<>();

		switch(args[0]){

		case "query":
			// Make a new query action object
			MyQueryAction a = new MyQueryAction();
			// Set location to an empty string
			String location = "";
			// Set index to 1, as we have the string "query" on 0th position
			int index = 1;

			// Check if global count is set
			if(gCountSet)
				// If global count is set, set the current count to global count
				a.setCount(gCount);

			// Loop through other strings in the array, this loop is to take care of multi-worded cities like New York
			while ( index < args.length ) {
				// This function checks whether the given string is a number
				if ( StringUtils.isNumeric(args[index]) ){
					// If it's a number, set the count & break
					a.setCount(Integer.parseInt(args[index]));
					break;
				}
				// Add this word to the location string
				location = location + " " + args[index];
				// Increment index
				index++;
			}
			// If the location string is empty or the count is less than 1, the action is invalid
			if (location.isEmpty() || a.getCount() < 1) {
				System.out.println("Invalid action specified");
				return actions;
			}
			// Set the location of the action object after trimming it
			a.setLocation(location.trim());
			// Add action to the list
			actions.add(a);
			break;

		case "setcount":

			// args.length is used to check the number of strings in the array "args"
			// "setcount 1" has length 2
			// We also check that the 2nd element is a number
			if(args.length == 2 && StringUtils.isNumeric(args[1])) {
				// setcount command sets the global variable
				// We use Integer.parseInt method to get the integer value from string
				gCount = Integer.parseInt(args[1]);
				// We also set the boolean gCountSet to true
				gCountSet = true;
			}
			else {
				System.out.println("Invalid action specified");
				return actions;
			}
			if (gCount < 1) {
				// If the count was less than 1, then it's an invalid value
				System.out.println("Invalid count specified");
				return actions;
			}
			System.out.println("Count set: " + gCount);
			break;

		case "print":

			// This is for printing
			MyPrintAction p = new MyPrintAction();

			// Set count to global value initially
			if(gCountSet)
				p.setCount(gCount);

			// If there are 2 elements, this means that count is also specified, same as above
			if(args.length == 2 && StringUtils.isNumeric(args[1]))
				p.setCount(Integer.parseInt(args[2]));
			else if(args.length == 2 && !StringUtils.isNumeric(args[1])) {
				System.out.println("Invalid action specified");
				return actions;
			}
			else if (args.length > 2){
				System.out.println("Invalid action specified");
				return actions;
			}
			actions.add(p);
			break;

		case "search":
			// A search action will always have 2 arguments
			// First is the word search & second is the keyword
			MySearchAction s = null;
			if(args.length == 2){
				s = new MySearchAction(args[1]);
			}
			else {
				System.out.println("Invalid action specified");
				return actions;
			}
			actions.add(s);
			break;

		case "sort":
			int field = 0, order = 0;
			// Sort action has either 2 or 3 arguments
			// Example 2: sort author
			// Example 3: sort author desc
			if(args.length == 2 || args.length == 3) {
				// Get the field & order
				// First we match the first argument & check the field to sort with
				// Please check the Syntax of switch statements for better understanding
				switch(args[1].toLowerCase()){
				case "author": field = SortAction.FIELD_AUTHOR; break;
				case "tweet": field = SortAction.FIELD_TWEET; break;
				case "date": field = SortAction.FIELD_DATE; break;
				default:
					System.out.println("Invalid sorting field specified.");
					return actions;
				}
				// If the length is 3, we also have the order specified
				if(args.length == 3){
					// Order is also specified
					if(args[2].equalsIgnoreCase("desc"))
						order = SortAction.ORDER_DESCENDING;
					else if(args[2].equalsIgnoreCase("asc"))
						order = SortAction.ORDER_ASCENDING;
					else {
						System.out.println("Invalid sorting order specified.");
						return actions;
					}
				}
			}

			else {
				System.out.println("Invalid action specified");
				return actions;
			}

			// All went well, add this sort action to the list
			actions.add(new MySortAction(field, order));
		}
		// Finally return this list of actions
		return actions;

	}

	@Override
	public List<Action> getActionsFromArguments(String[] args) {

		// Most of the content is same as the method above
		List<Action> actions = new ArrayList<>();
		// We'll use this variable to traverse the arguments array
		int index = 0;
		// Our Query Action
		MyQueryAction q = new MyQueryAction();

		if (args[0].equalsIgnoreCase("-help")){
			// Display the usage if -help specified
			printUsage(true);
		}
		else if(!args[0].startsWith("-")) {
			// Create location string
			String location = "";
			while ( index < args.length ) {
				// Break if number or another argument
				if ( StringUtils.isNumeric(args[index]) || args[index].startsWith("-") )
					break;

				location = location + " " + args[index];
				index++;
			}
			q.setLocation(location.trim());

			System.out.println("Location set: " + q.getLocation());
		}
		else{
			// A string that starts with "-" and isn't "-help"
			printUsage(true);
		}


		// Parse all arguments
		while (index<args.length) {

			// If the count specified, take the next value as count
			if (args[index].equalsIgnoreCase("-count")){
				index++;
				if(args.length < index+1)
					printUsage(true);
				// Count is specified too
				String count = args[index];
				if (StringUtils.isNumeric(count)) {
					q.setCount(Integer.valueOf(count));
				}
				else
					printUsage(true);

				if (q.getCount() < 1)
					printUsage(true);

				index++;
				System.out.println("Count set: " + q.getCount());

			}

			// If sort specified, input the field and/or the order
			else if(args[index].equalsIgnoreCase("-sort")){

				index++;
				if(args.length < index+1)
					printUsage(true);

				int field = 0, order = 0;
				// Get the field & order
				switch(args[index].toLowerCase()){
				case "author": field = SortAction.FIELD_AUTHOR; break;
				case "tweet": field = SortAction.FIELD_TWEET; break;
				case "date": field = SortAction.FIELD_DATE; break;
				default:
					System.out.println("Invalid sorting field specified.");
					printUsage(true);
				}
				index++;
				if(index <args.length && !args[index].startsWith("-")){
					// Order is also specified
					if(args[index].equalsIgnoreCase("desc"))
						order = SortAction.ORDER_DESCENDING;
					else if(args[index].equalsIgnoreCase("asc"))
						order = SortAction.ORDER_ASCENDING;
					else
						printUsage(true);
					index++;
				}

				// All went well, add this sort action to the list
				actions.add(new MySortAction(field, order));

				System.out.println("Sort set: " + field + ", " + order);
			}
			// If search specified, set the keyword
			else if(args[index].equalsIgnoreCase("-search")){
				index++;
				if(args.length < index+1)
					printUsage(true);
				String keyword = args[index];
				actions.add(new MySearchAction(keyword));
				index++;
				System.out.println("Keyword set: " + keyword);
			}

			else {
				// Invalid
				printUsage(true);
			}
		}
		// Add query to front
		actions.add(0,q);
		return actions;
	}


	@Override
	public void executeAction(Action action) {
		// We created a list of actions in above methods, now we execute them individually
		if (action instanceof MyQueryAction) {
			// Checks if this is MyQueryAction
			// Location stores the location
			String location = ((MyQueryAction) action).getLocation();
			// Get the twitter query from the cache for this location
			MyTwitterQuery tQ = (MyTwitterQuery) cache.getQueryFromCache(location);

			// If the response is null, this means not in cache
			// OR If the co-ordinates not present in cache
			// In both of above cases, we need to fetch from OSM(OpenStreetMaps) & update in cache
			if (tQ == null || !tQ.isGeoSet()){
				// Need to fetch whole data from OSM & update in cache
				System.out.println("Fetching data from OSM...");
				if (tQ != null)
					location = tQ.getLocation();

				tQ = (MyTwitterQuery) locationSearch.getQueryFromLocation(location);

				// Check if valid location found
				if ( !tQ.isGeoSet() ){
					System.out.println("Invalid location entered..");
					printUsage(true);
				}
				// If valid location, add the location to cache
				cache.addLocation(tQ);
			}
			else
				System.out.println("Found in cache");


			// Set the counts
			tQ.setCount(((MyQueryAction) action).getCount());

			// Set the TwitterQuery object
			this.tQ = tQ;
			// Perform Query
			this.tweets = twitterSearch.getTweets(this.tQ);

			System.out.println("Location search result: " + tQ.toString());

		}
		else if(action instanceof MySortAction){
			// Sort List<Tweet>
			// We use a custom sorting function for this
			Collections.sort(tweets, new Comparator<Tweet>() {
				@Override
				public int compare(Tweet t1, Tweet t2) {
					int result = 0;
					switch(((MySortAction) action).getSortField()){

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
					if (((MySortAction) action).getSortOrder() == SortAction.ORDER_DESCENDING)
						result = -result;

					return result;
				}

			});
		}
		else if(action instanceof MySearchAction){
			// Filter using keyword
			String keyword = ((MySearchAction) action).getSearchKeyword();
			// Set keyword
			((MyTwitterQuery) tQ).keyword = keyword;
			// Perform Query
			this.tweets = twitterSearch.getTweets(this.tQ);
		}
		else if(action instanceof MyPrintAction){
			// Print the list
			((MyPrintAction) action).print(tweets);
		}
	}

	@Override
	public void setLocationSearch(LocationSearch locationSearch) {
		this.locationSearch = locationSearch;
	}

	@Override
	public LocationSearch getLocationSearch() {
		return this.locationSearch;
	}

	@Override
	public void setTwitterSearch(TwitterSearch twitterSearch) {
		this.twitterSearch = twitterSearch;
	}

	@Override
	public TwitterSearch getTwitterSearch() {
		return twitterSearch;
	}

	@Override
	public void setCache(Cache cache) {
		this.cache = (MyCache) cache;
	}

	@Override
	public Cache getCache() {
		return cache;
	}

	@Override
	public void setTweets(List<Tweet> tweets) {
		this.tweets = tweets;
	}

	@Override
	public List<Tweet> getTweets() {
		return tweets;
	}

	public static void main(String[] args) {
		// Create the Twitter object
		// As soon as we create the object, it'll call the constructor
		TwitterApplication app = new Twitter();
		// Initialize the cache
		app.getCache().init();
		// Call the run method
		// This run method is from TwitterApplication.java
		app.run(args);

	}
}
