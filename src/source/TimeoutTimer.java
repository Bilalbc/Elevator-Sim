package source;

public class TimeoutTimer implements Runnable {
	
	public static final int SLEEP_TIME = 4100;
	
	private Elevator elevator;

	public TimeoutTimer(Elevator elevator)
	{
		this.elevator = elevator;
	}
	
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
		}
	}

}
