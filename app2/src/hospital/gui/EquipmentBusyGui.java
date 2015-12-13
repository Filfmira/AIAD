package hospital.gui;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;

import javax.swing.*;

import hospital.agents.Equipment;

public class EquipmentBusyGui extends JFrame {	

	private static final long serialVersionUID = 1L;
	
	private Equipment myAgent;
	
	private long timeLeft = 10000;
	private Timer timer;
	private JButton finishButton;
	
	public EquipmentBusyGui(Equipment a) {
		super(a.getLocalName());
		timeLeft = a.getTreatment().getTime()*1000;
		myAgent = a;
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(3,1));
		
		JLabel l = new JLabel("Patient: "+this.myAgent.getCurrentPatient().getLocalName());
		p.add(l);
		
		// Count down clock with treatment time
		JLabel clockLabel = new JLabel();
		
		ActionListener countDown = new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        timeLeft -= 100;
		        SimpleDateFormat df=new SimpleDateFormat("mm:ss:S");
		        clockLabel.setText("Time left: "+df.format(timeLeft));
		        if(timeLeft<=0)
		        {
		            timer.stop();
		        }
		    }
		};
		
		this.timer = new Timer(100, countDown);
		
		p.add(clockLabel);
		
		// Button to finish the treatment
		finishButton = new JButton("Finish "+this.myAgent.getTreatment().getName());
		finishButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					myAgent.finishTreatment();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		} );
		finishButton.setPreferredSize(new Dimension(300,100));
		finishButton.setEnabled(false);
		p.add(finishButton);

		this.add(p);
		
		
		// Make the agent terminate when the user closes 
		// the GUI using the button on the upper right corner	
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		} );
		
		setResizable(false);
	}
	
	public void startTimer(){
		this.timer.start();
		finishButton.setEnabled(true);
	}
	
	
	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		this.setSize(new Dimension(300,150));
		super.setVisible(true);
	}	
}
