package node;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This class is a representation of one File in the gui.
 * It contains:
 * 		The name of the file.
 * 		A button to open the file
 * 		A button to delete the file out of the system.
 * 		A button to delete the file out of the local map. (only when their is a copy of the file in the local map).
 */

public class YPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	String fileName;
	private JLabel nameField;
	private JButton butOpen;
	private JButton butDelete;
	private JButton butDeleteLocal;
	private boolean local;
	
	public YPanel (String fileName, boolean local) {
		super (new FlowLayout(FlowLayout.LEFT));
		this.fileName = fileName;
		nameField = new JLabel (fileName);	// Add file name.
		add (nameField);
		
		butOpen = new JButton ("open");
		add (butOpen);	// Add button to open the file.
		butOpen.addActionListener(this);
		
		butDelete = new JButton ("delete");
		add (butDelete);	// Add button to delete the file out of the system.
		butDelete.addActionListener(this);
		
		this.local = local;
		if (local) {	// Following steps has only be taken when the file is presented in the local map.
			butDeleteLocal = new JButton ("delete local");
			add (butDeleteLocal);	// Add button to delete the file out of the local file.
			butDeleteLocal.addActionListener(this);
		}
	}
	
	public void changeLocality (boolean local) {	// Change locality of the panel.
		if (this.local != local) {	// Their must only something happen when the given locality difference of the actual locality.
			this.local = local;
			if (local) {	// When it become local.
				if (butDeleteLocal == null) {
					butDeleteLocal = new JButton ("delete local");
					butDeleteLocal.addActionListener(this);
				}
				add (butDeleteLocal);	// Add the button.
			} else {	// Otherwise
				remove(butDeleteLocal);	// Delete the button.
			}
		}
	}
	
	public boolean getLocality() {
		return local;
	}
	
	/**
	 * Next action's will be performed when the user press a button.
	 */

	public void actionPerformed(ActionEvent e) {	// When the user press a button.
		JButton b = (JButton)e.getSource();
		if (b == butOpen)
			open ();
		else if (b == butDelete)
			delete ();
		else if (b == butDeleteLocal)
			deleteLocal ();
	}
	
	private void open () {	// Open this file.
		if (local)	// The function that is called inside the main depends on the locality of the file.
			Node.openLocal(fileName);
		else
			Node.open(fileName);
	}
	
	private void delete () {	// Remove this file out of the system.
		Node.delete(fileName);
	}
	
	private void deleteLocal () {	// Delete the file from the local map.
		Node.deleteLocal(fileName);
	}
}
