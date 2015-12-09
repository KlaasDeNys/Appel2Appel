package node;

import java.util.HashMap;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class NodeGui extends JFrame {
	private static final long serialVersionUID = 1L;
	private HashMap <String, YPanel> panelMap;
	private JPanel mainPanel;

	public NodeGui () {
		super("System Y");
		setBounds (100,100,450,300);
		mainPanel = new JPanel ();
		mainPanel.setLayout (new BoxLayout ( mainPanel, BoxLayout.PAGE_AXIS));
		add(mainPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panelMap = new HashMap <String,YPanel> ();
	}
	
	
	public boolean addFile (String fileName, boolean local) {
		if(panelMap.containsKey(fileName)) {
			return false;
		}
		YPanel panel = new YPanel (fileName, local);
		panelMap.put(fileName, panel);
		mainPanel.add(panelMap.get(fileName));
		mainPanel.updateUI();
		return true;
	}
	
	public boolean deleteFile (String fileName) {
		if(!panelMap.containsKey(fileName))
			return false;
		mainPanel.remove(panelMap.get(fileName));
		panelMap.remove(fileName);
		mainPanel.updateUI();
		return true;
	}
}