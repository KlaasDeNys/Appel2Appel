package node;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

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
	private boolean local;
	
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
		
		this.local = local;
		if (local) {
			butDeleteLocal = new JButton ("delete local");
			add (butDeleteLocal);
			butDeleteLocal.addActionListener(this);
		}
	}
	
	public void changeLocality (boolean local) {	// Change locality of the panel.
		if (this.local != local) {
			this.local = local;
			if (local) {
				if (butDeleteLocal == null) {
					butDeleteLocal = new JButton ("delete local");
					butDeleteLocal.addActionListener(this);
				}
				add (butDeleteLocal);
			} else {
				remove(butDeleteLocal);
			}
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
		if (local)
			Node.openLocal(fileName);
		else
			Node.open(fileName);
	}
	
	private void delete () {
		Node.delete(fileName);
	}
	
	private void deleteLocal () {
		Node.delete(fileName);
	}
}
