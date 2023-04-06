package source;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class ElevatorGUI extends JFrame implements ElevatorView {
	
    private final static int FRAME_WIDTH = 1025;
    private final static int FRAME_HEIGHT = 1000;
	
	public ElevatorGUI(Scheduler sch)
	{
		super("Elevator");
		sch.addElevatorView(this);
		initializeFrame();
	}
	
    private void initializeFrame() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        this.setLayout(new BorderLayout()); 
        this.setVisible(true);
        this.setResizable(false);
    }

	@Override
	public void update(ElevatorUpdateEvent eue) {
		
	}
	
}
