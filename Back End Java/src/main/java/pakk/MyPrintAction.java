package pakk;

import java.util.List;


public class MyPrintAction implements PrintAction {

	private int count = Integer.MAX_VALUE;
	
	public void setCount(int count) {
		this.count = count;
	}
	public void print(List<Tweet> tweets){
		int i=0;
		while (i<count && i <tweets.size()){
			Tweet t = tweets.get(i);
			System.out.println("@" + t.getUser() + " - " + t.getText() + " at " + t.getTimestamp().toString());
			i++;
		}
	}
}
