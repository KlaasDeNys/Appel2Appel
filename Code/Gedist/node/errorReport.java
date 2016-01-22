package node;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class errorReport extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	private JPanel errorPanel;
	private JButton butOk;
	private ArrayList <JLabel> errorMessages;
	
	public errorReport (String message) {	// Use error report with a default title.
		super ("System Y Node Error");	// Set default title
		setGui(message);
	}

	public errorReport (String title, String message) {	// Constructor
		super (title);
		setGui(message);
	}
	
	public void actionPerformed(ActionEvent e) {	// Action performed method.
		JButton b = (JButton)e.getSource();
		if (b == butOk) {	// When the ok button was pressed,
			dispose();	// dispose this window.
		}
	}
	
	private void setGui(String message){
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);	// Just close this window (EXIT_ON_CLOSE will end all processes.
		setBounds(250,300,400,190);
		
		errorPanel = new JPanel();
		errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.PAGE_AXIS));	// Vertical box layout.
		
		
		errorPanel.add(Box.createRigidArea(new Dimension(0,17)));	// Space between message and upper boundry
		
		errorMessages = new ArrayList <JLabel> ();
		
		int j = 0;
		while (message.length() > 65) {	// When the message is larger than 65 characters, it must be split over multiple lines.
			int lastSpace = 65;
			for (int i = 0; i < 66; i ++) {	// Look for a space to split the message.
				if (message.charAt(i) == ' ') {
					lastSpace = i;
				}
			}
			JLabel errorMessage = new JLabel(message.substring(0, lastSpace));
			errorMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
			errorMessages.add(errorMessage);
			errorPanel.add(errorMessages.get(j));
			
			j++;
			message = message.substring(lastSpace+1);
		}
		JLabel errorMessage = new JLabel (message);
		errorMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
		errorMessages.add(errorMessage);
		errorPanel.add(errorMessages.get(j));
		
		errorPanel.add(Box.createVerticalGlue());	// Glue text to upper boundry, and button to the lower boundry.
		
		butOk = new JButton("Ok");
		butOk.setAlignmentX(Component.CENTER_ALIGNMENT);
		errorPanel.add(butOk);
		butOk.addActionListener(this);	
		
		errorPanel.add(Box.createRigidArea(new Dimension(0,10)));
		
		add(errorPanel);
	}
}
