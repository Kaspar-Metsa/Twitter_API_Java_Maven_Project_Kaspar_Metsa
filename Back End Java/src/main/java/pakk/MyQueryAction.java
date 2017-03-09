package pakk;


public class MyQueryAction implements QueryAction {
	private String location;
	private Integer count;
	MyQueryAction(String location, int count){
		setLocation(location);
		setCount(count);
	}
	public MyQueryAction() {
		count = 15;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
}
