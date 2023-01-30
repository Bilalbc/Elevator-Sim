package source;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Floor implements Runnable{
	Scheduler scheduler;
	
	public Floor(Scheduler sch) {
		this.scheduler = sch;
	}
	
	
	private Request createRequest(Scanner reader) {

		String data = reader.nextLine();
		String[] values = data.split(" ");
		Request req = new Request(values[0],  Integer.parseInt(values[1]), values[2], Integer.parseInt(values[3]));
		
		return req;
	}
	
	@Override
	public void run() {
		try {
			File file = new File("src//source//Requests.txt");
			Scanner reader = new Scanner(file);
			while(reader.hasNextLine()) {
				Request req = createRequest(reader);
				System.out.println(req.getDirection() + " " + req.getTime() + " " + req.destinationFloor() + " "+req.startFloor());
				//send to SCH
				
			}
			reader.close();
		} catch (FileNotFoundException e) {}
	}
	
}
