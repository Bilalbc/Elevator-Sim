package source;

public class Message {

	private String time;
	private String direction;
	private int startFloor;
	private int destinationFloor;
	private String returnMessage;
		
	public Message(String time, int startFloor, String direction, int destinationFloor) {
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
	
	public void setReturnMessage(String message) {
		this.returnMessage = message;
	}
	
	public String getReturnMessage() {
		return this.returnMessage;
	}
	
	@Override
	public String toString() {
		return this.direction + " " + this.time + " " + this.destinationFloor + " " + this.startFloor;
	}
	
	@Override
	public boolean equals(Object msg) {
		
		if(!(msg instanceof Message)) {
			return false;
		}
		
		Message dupe = (Message) msg;
		
		return ((this.startFloor == dupe.startFloor) &&
				(this.destinationFloor == dupe.destinationFloor) &&
				(this.direction.equals(dupe.direction)) &&
				(this.time.equals(dupe.time)));
	}
	
}
