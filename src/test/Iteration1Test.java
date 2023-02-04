package test;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

import source.Elevator;
import source.Floor;
import source.Message;
import source.Scheduler;

/**
 * @Author Bilal Chaudhry 101141634
 *
 * @Date: 2023-02-04
 * @Version 1.0
 *
 *
 * Test class for 
 */

public class Iteration1Test {

	private File testData;
	private Scheduler s;
	
	@Before
	public void setUp() {
		
		this.testData = new File("src//source//Requests.csv");
		
		s = new Scheduler();
		System.out.println("asd");
		
	}
	
	@Test 
	public void testSendAndRecieveResponse() {
		Message expectedRequest;
		
		try {
			Scanner scanner = new Scanner(testData);

			Thread f = new Thread(new Floor(s), "Floor");
			Thread e = new Thread(new Elevator(s), "Elevator");
			
			f.start();
			e.start();
						
			expectedRequest = createRequest(scanner.nextLine());	
			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Test public void testPassRequest() {
	
		Message expectedRequest;
		
		try { 
			Scanner scanner = new Scanner(testData); Thread f = new Thread(new
			
			Floor(s), "Floor");
			
			f.start();
			
			expectedRequest = createRequest(scanner.nextLine());
			assertTrue(s.readMessage().equals(expectedRequest));
			
			scanner.close();
			
		} catch (FileNotFoundException e) { 
			e.printStackTrace(); 
			}
		}
	
	@Test public void testPassReply() {
	
		Message expectedResponse;
		
		try { 
			Scanner scanner = new Scanner(testData);
			
			Thread f = new Thread(new Floor(s), "Floor"); Thread e = new Thread(new
			Elevator(s), "Elevator");
			
			f.start(); e.start();
			
			expectedResponse = createRequest(scanner.nextLine());
			assertTrue(s.readReply().equals(expectedResponse));
			
			scanner.close();
		
		} catch (FileNotFoundException e) { 
			e.printStackTrace(); 
		}
		
	}
	
	private Message createRequest(String request) {
		String[] values = request.split(", ");
		Message req = new Message(values[0],  Integer.parseInt(values[1]), values[2], Integer.parseInt(values[3]));
		
		return req;
	}
}
