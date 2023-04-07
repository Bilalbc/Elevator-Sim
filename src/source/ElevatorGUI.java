package source;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.WindowConstants;

import source.Elevator.ElevatorStates;

public class ElevatorGUI extends JFrame implements ElevatorView {
	
    private final static int FRAME_WIDTH = 1500;
    private final static int FRAME_HEIGHT = 980;
    private final static int GRID_X = 22;
    private final static int GRID_Y = 4;
    
    //Panels
    private JPanel floorsPanel;
    private JPanel lightPanel;
    private JPanel statePanel;
    
    private JLabel[][]floorCoordinates;
    private JLabel[][]lightCoordinates;
    private JLabel[]states;
	
	public ElevatorGUI(Scheduler sch)
	{
		super("Elevator");
	    this.setBackground(Color.red);
		sch.addElevatorView(this);
		this.floorCoordinates = new JLabel[GRID_X][GRID_Y];
		this.lightCoordinates = new JLabel[GRID_X][GRID_Y];
		this.states = new JLabel[GRID_Y];
		initializeFrame();
		initializeFloors();
		initializeLight();
		initializeState();
        this.revalidate();
	}
	
    private void initializeFrame() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        this.setVisible(true);
        this.setResizable(true);
    }
    
    private void initializeFloors()
    {
    	this.floorsPanel = new JPanel();
    	this.floorsPanel.setOpaque(true);
    	this.floorsPanel.setBackground(Color.CYAN);
    	this.floorsPanel.setLayout(new GridLayout(23, 4));
    	for (int i = 21; i >= 0; i--)
    	{
    		JLabel floor = new JLabel();
    		floor.setText("FLOOR " + (i+1));
    		this.floorsPanel.add(floor);
    		
    		for (int j = 0; j < 4; j++)
    		{
	    		JLabel elevator = new JLabel();
	    		elevator.setOpaque(true);
	    		elevator.setBackground(Color.CYAN);
	    		elevator.setText(" ");
	    		this.floorCoordinates[i][j] = elevator;
	    		this.floorsPanel.add(elevator);
    		}
    	}
    	    	    	
    	this.add(this.floorsPanel, BorderLayout.CENTER);
    	
    	/*
    	this.floorCoordinates[21][0].setText("ELEVATOR 1 HERE");
    	this.floorCoordinates[21][0].setBackground(Color.gray);
    	
    	this.floorCoordinates[11][1].setText("ELEVATOR 2 HERE");
    	this.floorCoordinates[11][1].setBackground(Color.red);
    	
    	this.floorCoordinates[21][2].setText("ELEVATOR 3 HERE");
    	this.floorCoordinates[21][2].setBackground(Color.green);
    	
    	this.floorCoordinates[0][3].setText("ELEVATOR 4 HERE");
    	this.floorCoordinates[0][3].setBackground(Color.pink); */
    }
    
    private void initializeLight()
    {
    	this.lightPanel = new JPanel();
    	this.lightPanel.setOpaque(true);
    	this.lightPanel.setBackground(Color.pink);
    	
    	this.lightPanel.setLayout(new GridLayout(23, 4));
    	
    	for (int i = 0; i < 4; i++)
    	{
    		JLabel l = new JLabel("Elevator " + (i+1) + " lights    ");
    		this.lightPanel.add(l);
    	}
	
        for (int j = 0 ; j < 22; j++)
        {
        	for (int k = 0; k < 4; k++)
        	{
        		JLabel l = new JLabel();
        		l.setOpaque(true);
        		l.setText("F " + (j+1));
        		this.lightCoordinates[j][k] = l;
        		this.lightPanel.add(l);
        	}
        }
	
    	this.add(this.lightPanel, BorderLayout.LINE_START);
    	
    	//this.lightCoordinates[18][3].setBackground(Color.yellow);
    }
        
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
    	
    	//this.states[2].setText("MOVING");
    }
    
    private void moveElevator(int elevatorNum, int currentFloor)
    {
    	for (int i = 0; i < 22; i++)
    	{
    		this.floorCoordinates[i][elevatorNum].setBackground(Color.blue);
    		this.floorCoordinates[i][elevatorNum].setText("");;
    	}
    	
		this.floorCoordinates[currentFloor][elevatorNum].setBackground(Color.gray);
		this.floorCoordinates[currentFloor][elevatorNum].setText("ELEVATOR " + (elevatorNum + 1));
    }

	@Override
	public void updateLight() {
		
	}

	@Override
	public void updateFloorAndState(int currentFloor, int elevatorNum, ElevatorStates elevatorState) {
		moveElevator(currentFloor-1, elevatorNum-1);
		this.states[elevatorNum-1].setText(elevatorState.toString());
	}

}
