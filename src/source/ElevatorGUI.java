/**
 * @Author: Mohamed Kaddour
 * @Date: 2023-04-12
 * @Version 5.0
 * 
 * GUI Class the acts as the view for the user. Displays the elevators moving and the current state of each elevator. Also displays the internal lights 
 * for each elevator displaying both the destinations and the starting floors. 
 */
package source;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.WindowConstants;

import source.Elevator.ElevatorStates;

public class ElevatorGUI extends JFrame implements ElevatorView {
	
    public final static int FRAME_WIDTH = 1000;
    public final static int FRAME_HEIGHT = 700;
    public final static int GRID_X = 22;
    public final static int GRID_Y = 4;
    
    public final static Color lightPink = new Color(249, 225, 225);  
    
    //Panels
    private JPanel floorsPanel;
    private JPanel lightPanel;
    private JPanel statePanel;
    
    private JLabel[][]elevatorCoordinates;
    private JLabel[][]lightCoordinates;
    private JLabel[]states;
    private JLabel[]floorCoordinates;
	
    /**
     * Constructor for ElevatorGUI that takes in the scheduler to add itself to as a view. Initializes all the Label arrays and the GUI.
     * 
     * @param sch Scheduler
     * */
	public ElevatorGUI(Scheduler sch)
	{
		super("Elevator");
	    this.setBackground(Color.red);
		sch.addElevatorView(this);
		this.elevatorCoordinates = new JLabel[GRID_X][GRID_Y];
		this.lightCoordinates = new JLabel[GRID_X][GRID_Y];
		this.states = new JLabel[GRID_Y];
		this.floorCoordinates = new JLabel[GRID_X];
		initializeFrame();
		initializeFloors();
		initializeLight();
		initializeState();
        this.revalidate();
	}
	
    /**
     * Initializes the main frame for the GUI
     * */
    private void initializeFrame() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        this.setVisible(true);
        this.setResizable(true);
    }
    
    /**
     * Initializes the floor displays for the GUI as well as the 4 elevators.
     * */
    private void initializeFloors()
    {
    	this.floorsPanel = new JPanel();
    	this.floorsPanel.setOpaque(true);
    	this.floorsPanel.setBackground(Color.CYAN);
    	this.floorsPanel.setLayout(new GridLayout(23, 4));
    	for (int i = 21; i >= 0; i--)
    	{
    		JLabel floor = new JLabel();
    		floor.setOpaque(true);
    		floor.setBackground(Color.CYAN);
    		floor.setText("FLOOR " + (i+1));
    		this.floorCoordinates[i] = floor;
    		this.floorsPanel.add(floor);
    		
    		for (int j = 0; j < 4; j++)
    		{
	    		JLabel elevator = new JLabel();
	    		elevator.setOpaque(true);
	    		elevator.setBackground(Color.CYAN);
	    		elevator.setText(" ");
	    		this.elevatorCoordinates[i][j] = elevator;
	    		this.floorsPanel.add(elevator);
    		}
    	}
    	    	    	
    	this.add(this.floorsPanel, BorderLayout.CENTER);
    	
    }
    
    /**
     * Initializes the light display for each elevator on the left side panel
     * */
    private void initializeLight()
    {
    	this.lightPanel = new JPanel();
    	this.lightPanel.setLayout(new GridLayout(23, 4));
    	
    	for (int i = 0; i < 4; i++)
    	{
    		JLabel l = new JLabel("Elevator " + (i+1) + " lights    ");
    		l.setOpaque(true);
    		l.setBackground(lightPink);
    		this.lightPanel.add(l);
    	}
	
        for (int j = 0 ; j < 22; j++)
        {
        	for (int k = 0; k < 4; k++)
        	{
        		JLabel l = new JLabel();
        		l.setOpaque(true);
        		l.setBackground(lightPink);
        		l.setText("F " + (j+1));
        		this.lightCoordinates[j][k] = l;
        		this.lightPanel.add(l);
        	}
        }
	
    	this.add(this.lightPanel, BorderLayout.LINE_START);
    	
    }
        
    /**
     * Initializes the state panel for each elevator on the top of the frame.
     * */
    private void initializeState()
    {
    	this.statePanel = new JPanel(new GridLayout(1, 8));
    	
    	for (int i = 0; i < 4; i++)
    	{
    		JLabel stateLabel = new JLabel("ELEVATOR " + (i+1) + " STATE");
    		JLabel state = new JLabel("OPEN");
    		this.states[i] = state;
    		this.statePanel.add(stateLabel);
    		this.statePanel.add(state);
    	}
    	
    	this.add(this.statePanel, BorderLayout.PAGE_START);
    	
    }
    
    /**
     * Moves the specified elevator the specified floor. This is done through modifying shading.
     * 
     * @param elevatorNum int
     * @param currentFloor int
     * */
    private void moveElevator(int elevatorNum, int currentFloor)
    {
    	for (int i = 0; i < 22; i++)
    	{
    		this.elevatorCoordinates[i][elevatorNum].setBackground(Color.cyan);
    		this.elevatorCoordinates[i][elevatorNum].setText("");;
    	}
    	
		this.elevatorCoordinates[currentFloor][elevatorNum].setBackground(Color.gray);
		this.elevatorCoordinates[currentFloor][elevatorNum].setText("ELEVATOR " + (elevatorNum + 1));
    }

    /**
     * Updates the light for the specified elevator indicating that either a request is ready at that floor or that floor is a destination.
     * The light is then either turned on or off depending on the passed in boolean
     * 
     * @param floorLight int
     * @param elevatorNum int
     * @param turnOn boolean
     * */
	public void updateLight(int floorLight, int elevatorNum, boolean turnOn) {
		
		if (turnOn)
		{
			this.lightCoordinates[floorLight-1][elevatorNum].setBackground(Color.yellow);    	
		}
		else
		{
			this.lightCoordinates[floorLight-1][elevatorNum].setBackground(lightPink);    	
		}
			
	}

	/**
	 * Updates the floor location of the specific elevator and also the state of the elevator.
	 * 
	 * @param currentFloor int
	 * @param elevatorNum int
	 * @param elevatorState ElevatorStates
	 * */
	public void updateFloorAndState(int currentFloor, int elevatorNum, ElevatorStates elevatorState) {
		moveElevator(elevatorNum-1, currentFloor-1);
		this.states[elevatorNum-1].setText(elevatorState.toString());
	}

	/**
	 * Flashes the specified floor when there is a request there and then flashes it off when another request appears.
	 * 
	 * @param startFloor int
	 * */
	@Override
	public void updateFloorRequest(int startFloor) {
		int floor = startFloor - 1;
		
    	for (int i = 0; i < 22; i++)
    	{
    		this.floorCoordinates[i].setBackground(Color.cyan);
    	}
    	    	
		this.floorCoordinates[floor].setBackground(Color.yellow);
	}
	
	/**
	 * Helper Method for testing to programmatically close the GUI
	 */
	public void close() {
		this.dispose();
	}

}
