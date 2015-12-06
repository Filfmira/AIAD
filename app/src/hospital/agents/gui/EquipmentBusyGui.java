package hospital.agents.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import hospital.agents.EquipmentAgent;

public class EquipmentBusyGui extends JFrame {	

	private static final long serialVersionUID = 1L;
	
	private EquipmentAgent myAgent;
	
	
	public EquipmentBusyGui(EquipmentAgent a) {
		super(a.getLocalName());
		
		myAgent = a;
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		
		JLabel l = new JLabel("Patient: "+this.myAgent.getCurrentPatient().getLocalName());
		p.add(l, BorderLayout.NORTH);
		
		JButton finishButton = new JButton("Finish "+this.myAgent.getTreatment().getName());
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
		p.add(finishButton, BorderLayout.SOUTH);

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
	
	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}	
}
