/**
 * @Author: Mohamed Kaddour

 * @Date: 2023-03-25
 * @Version 4.0
 * 
 * Runnable class that primarily sleeps for the SLEEP-TIME and if it's expired, then a timeout event is triggered
 * in the calling elevator. 
 */


package source;

public class TimeoutTimer implements Runnable {
	
	public static final int SLEEP_TIME = 4500;
	
	private Elevator elevator;

	/**
	 * Constructor to initialize the elevator
	 * 
	 * @param elevator Elevator
	 * */
	public TimeoutTimer(Elevator elevator)
	{
		this.elevator = elevator;
	}
	
	/**
	 * Main run method that sleeps and then calls timeout when the 
	 * timer expires.
	 * */
	@Override
	public void run() {
		
		while (true)
		{
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				continue;
			}
			
			elevator.setTimeout();
			// when elevator times out, associated timer thread should exit
			break;
		}
	}

}
