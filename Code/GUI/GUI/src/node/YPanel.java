package node;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class YPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	String fileName;
	private JLabel nameField;
	private JButton butOpen;
	private JButton butDelete;
	private JButton butDeleteLocal;
	
	public YPanel (String fileName, boolean local) {
		super (new FlowLayout(FlowLayout.LEFT));
		this.fileName = fileName;
		nameField = new JLabel (fileName);
		add (nameField);
		
		butOpen = new JButton ("open");
		add (butOpen);
		butOpen.addActionListener(this);
		
		butDelete = new JButton ("delete");
		add (butDelete);
		butDelete.addActionListener(this);
		
		if (local) {
			butDeleteLocal = new JButton ("delete local");
			add (butDeleteLocal);
			butDeleteLocal.addActionListener(this);
		}
	}

	public void actionPerformed(ActionEvent e) {
		JButton b = (JButton)e.getSource();
		if (b == butOpen)
			open ();
		else if (b == butDelete)
			delete ();
		else if (b == butDeleteLocal)
			deleteLocal ();
	}
	
	private void open () {
		System.out.print("\n" + fileName + ": open\n\n> ");
	}
	
	private void delete () {
		System.out.print("\n" + fileName + ": delete\n\n> ");
	}
	
	private void deleteLocal () {
		System.out.print("\n" + fileName + ": delete local\n\n> ");
	}
}
