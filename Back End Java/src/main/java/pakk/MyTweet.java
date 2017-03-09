package pakk;

import java.util.Date;

public class MyTweet implements Tweet {
	private String text;
	private String user;
	private Date timestamp;
	
	public MyTweet(String text, String user, Date timestamp) {
		this.text = text;
		this.user = user;
		this.timestamp = timestamp;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
}
