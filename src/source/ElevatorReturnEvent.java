package source;

public class ElevatorReturnEvent {
	
	private int readMessage;
	private boolean requestsComplete;
	private int queueSize;
	
	public ElevatorReturnEvent(int readMessage, boolean requestsComplete, int queueSize)
	{
		this.readMessage = readMessage;
		this.requestsComplete = requestsComplete;
		this.queueSize = queueSize;
	}
	
	public int readMessage()
	{
		return this.readMessage;
	}
	
	public boolean requestsComplete()
	{
		return this.requestsComplete;
	}
	
	public int getQueueSize()
	{
		return this.queueSize;
	}
}
