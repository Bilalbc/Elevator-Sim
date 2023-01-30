package source;

public class Request {
	private String time;
	private String direction;
	private int startFloor;
	private int destinationFloor;
	
	public Request(String time, int startFloor, String direction, int destinationFloor) {
		this.time = time;
		this.direction = direction;
		this.startFloor = startFloor;
		this.destinationFloor = destinationFloor;
	}
	
	public String getTime() {
		return this.time;
	}
	
	public String getDirection() {
		return this.direction;
	}
	
	public int startFloor() {
		return this.startFloor;
	}
	
	public int destinationFloor() {
		return this.destinationFloor;
	}
	
	
}
