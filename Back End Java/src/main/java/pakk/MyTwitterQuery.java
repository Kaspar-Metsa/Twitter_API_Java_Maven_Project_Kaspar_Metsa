package pakk;


public class MyTwitterQuery implements TwitterQuery {

	private Double latitude;
	private Double longitude;
	private Double radius;
	private String location;
	private Integer count;
	public String keyword;
	
	public MyTwitterQuery(String location) {
		this.location = location;
		this.latitude = Double.MIN_VALUE;
		this.longitude = Double.MIN_VALUE;
		this.radius = Double.MIN_VALUE;
		this.count = 15;
		this.keyword = "";
	}

	@Override
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	@Override
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	@Override
	public void setRadius(double radius) {
		this.radius = radius;
	}

	@Override
	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public void setCount(int count) {
		this.count = count;
	}
	
	@Override
	public double getLatitude() {
		return this.latitude;
	}

	@Override
	public double getLongitude() {
		return this.longitude;
	}

	@Override
	public double getRadius() {
		return this.radius;
	}

	@Override
	public String getLocation() {
		return this.location;
	}

	@Override
	public int getCount() {
		return this.count;
	}

	@Override
	public boolean isGeoSet() {
		return ( this.radius != Double.MIN_VALUE && this.latitude != Double.MIN_VALUE && this.longitude != Double.MIN_VALUE);
	}

	@Override
	public String toString() {
		return "MyTwitterQuery [latitude=" + latitude + ", longitude="
				+ longitude + ", radius=" + radius + ", location=" + location
				+ ", count=" + count + ", keyword=" + keyword + "]";
	}

}
