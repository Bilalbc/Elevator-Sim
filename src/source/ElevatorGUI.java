package source;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.WindowConstants;

public class ElevatorGUI extends JFrame implements ElevatorView {
	
    private final static int FRAME_WIDTH = 1500;
    private final static int FRAME_HEIGHT = 980;
    private final static int GRID_X = 23;
    private final static int GRID_Y = 2;
    
    //Panels
    private JPanel floorsPanel;
    private JPanel lightPanel;
    private JPanel statePanel;
    
    private JLabel[][]floorCoordinates;
	
	public ElevatorGUI(Scheduler sch)
	{
		super("Elevator");
	    this.setBackground(Color.red);
		sch.addElevatorView(this);
		this.floorCoordinates = new JLabel[GRID_X][GRID_Y];
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
        this.setResizable(false);
    }
    
    private void initializeFloors()
    {
    	this.floorsPanel = new JPanel();
    	this.floorsPanel.setOpaque(true);
    	this.floorsPanel.setBackground(Color.CYAN);
    	this.floorsPanel.setLayout(new GridLayout(23, 1));
    	for (int i = 22; i > 0; i--)
    	{
    		JLabel floor = new JLabel();
    		floor.setText("FLOOR " + i);
    		this.floorCoordinates[i][0] = floor;
    		this.floorsPanel.add(floor);
    		
    		JLabel elevator = new JLabel();
    		elevator.setText("");
    		this.floorCoordinates[i][1] = elevator;
    		this.floorsPanel.add(elevator);
    	}
    	    	    	
    	this.add(this.floorsPanel, BorderLayout.CENTER);
    	
    	this.floorCoordinates[21][1].setText("ELEVATOR HERE");
    }
    
    private void initializeLight()
    {
    	this.lightPanel = new JPanel();
    	JLabel lights = new JLabel("LIGHTS");
    	this.lightPanel.add(lights);
    	
    	this.add(this.lightPanel, BorderLayout.LINE_START);
    }
    
    private void initializeState()
    {
    	this.statePanel = new JPanel();
    	JLabel state = new JLabel("ELEVATOR STATE");
    	this.statePanel.add(state);
    	
    	this.add(this.statePanel, BorderLayout.PAGE_START);
    }

	@Override
	public void update(ElevatorUpdateEvent eue) 
	{
		
	}
	
}
