package pakk;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.math.NumberUtils;


public class MyCache implements Cache {

	private class Row {

		TwitterQuery tQ;
		List<String> altName = new ArrayList<>();

		Row(CSVRecord record) {
			// Create a ROW from CSV Record
			//get method is defined here https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVRecord.html
			//get(0) returns first column which is always location
			tQ = new MyTwitterQuery(record.get(0));
			//get(1) is first column etc
			String lat = record.get(1), lon = record.get(2), radius = record.get(3);
			//Here I check whether these are really numbers, if they are I save them into my new MyTwitterQuery object.
			if(NumberUtils.isNumber(lat))
				tQ.setLatitude(Double.valueOf(lat));
			
			if(NumberUtils.isNumber(lon))
				tQ.setLongitude(Double.valueOf(lon));
			
			if(NumberUtils.isNumber(radius))
				tQ.setRadius(Double.valueOf(radius));
			
			//From column 4 i save the aliases for locations
			
			for(int i=4; i<record.size(); i++){
				altName.add(record.get(i));
			}
		}
		Row(TwitterQuery tQ) {
			this.tQ = tQ;
		}
		
		List<String> getRow(){
			List<String> row = new ArrayList<String>();
			row.add(tQ.getLocation());
			row.add(String.valueOf(tQ.getLatitude()));
			row.add(String.valueOf(tQ.getLongitude()));
			row.add(String.valueOf(tQ.getRadius()));
			row.addAll(altName);
			return row;
		}
		boolean containsLoc(String s){
			return altName.contains(s);
		}
	}
//We define fileName variable here, we save the fileName location in twitter class line 30
	private String fileName;
	private CSVFormat format;
	
	
	@Override
	public void init() {
		//Here I define the format to be used
		//RFC4180 is a predefined comma separated format field
		//withRecordSeparator is a CSVFormat method which separats with /r/n. /r - moves cursor to beginning of line. /n - moves cursor to the next line without moving to beginning
		//lineSeparator() returns /r/n line separator string
		this.format = CSVFormat.RFC4180.withRecordSeparator(System.lineSeparator());
	}

	@Override
	public TwitterQuery getQueryFromCache(String location) {
		//If line 419 in Twitter.java class doesn't find anything saved here for a particular location then this null is returned
		TwitterQuery tQ = null;
		
		// Read the file and create list of Rows
		List<Row> rows = new ArrayList<>();
		// Here I define that the format to be used is a predefined comma separated format field
		CSVFormat format = CSVFormat.RFC4180;
		
		try{
			//Filename was saved in twitter.java class then saved with setCacheFilename method and now we use it to write into there
			// Here I start a new file instance, fileName parameter is the file path in system which was defined on row 30 in Twitter.java
			File file = new File(fileName);
			
			//Here I start a new instance of FileReader class for reading files
			FileReader r = new FileReader(file);
			
			// Initialize the parser
			// Instructions here https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVParser.html
			// FROM: "Parsing record wise"
			CSVParser parser = new CSVParser(r, format);
	        //Get a list of CSV file records & add them to the List of Rows
			// Here I define record variable and use methods getRecords()from CSVParser library, source: https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVParser.html
            for(CSVRecord record: parser.getRecords())
				rows.add(new Row(record));
            
			
			r.close();
			parser.close();

		} catch(IOException e){
			System.out.println("Error while reading the Cache: " + e); 
			Twitter.printUsage(true);
			System.exit(1);
		}
		
		for(Row row: rows){
			
			if(row.tQ.getLocation().equals(location)){
				// If location named matched, best case scenario
				tQ = row.tQ;
				break;
			}
			else if(row.containsLoc(location)){
				// We'll set the Query for this row, but continue to look for a better match
				tQ = row.tQ;
			}
			
		}
		
		return tQ;
	}

	@Override
	public void updateLocation(TwitterQuery query) {
		// Read the file and create list of Rows
		List<Row> rows = new ArrayList<>();

		try{
			//Filename was saved in twitter.java class then saved with setCacheFilename method and now we use it to write into there

			File file = new File(fileName);

			FileReader r = new FileReader(file);

			// Initialize the parser
			CSVParser parser = new CSVParser(r, format);
			//Get a list of CSV file records & add them to the List of Rows
			// Here I define record variable and use methods getRecords()from CSVParser library, source: https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVParser.html
			for(CSVRecord record: parser.getRecords())
				rows.add(new Row(record));


			r.close();
			parser.close();

		} catch(IOException e){
			System.out.println("Error while reading the Cache: " + e); 
			Twitter.printUsage(true);
			System.exit(1);
		}

		int i = 0;
		while (i<rows.size()) {
			
			Row row = rows.get(i);
			
			// If we found a row with matching location, update and break
			if(row.tQ.getLocation().equals(query.getLocation())){
				row.tQ = query;
				rows.set(i, row);
				System.out.println("Updated location.");
				break;
			}
			
			i++;
		}

		if (i == rows.size()) {
			// This means the location doesn't exist in file
			// Add this row to list
			rows.add(new Row(query));
			System.out.println("Added location.");
		}
		
		// Write to file
		try{
			//Filename was saved in twitter.java class then saved with setCacheFilename method and now we use it to write into there
			File file = new File(fileName);
			Writer w = new FileWriter(file);
			CSVPrinter printer  = new CSVPrinter(w, format);
			for (Row row: rows) {
				// Write the row to file
				printer.printRecord(row.getRow());
			}
			printer.close();
			w.close();
		} catch(IOException e){
			System.out.println("Error while writing CSV to file: " + e);
			Twitter.printUsage(true);
			System.exit(1);
		}
	}

	@Override
	public void setCacheFilename(String filename) {
		//This here saves the filename received from Twitter.java class to variable fileName
		this.fileName = filename;
	}

	@Override
	public String getCacheFilename() {
		//This here returns the fileName location which was saved with method setCacheFilename
		return this.fileName;
	}

}
