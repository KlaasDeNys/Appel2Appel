package node;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class NodeGui extends JFrame {
	private static final long serialVersionUID = 1L;
	private HashMap <String, YPanel> panelMap;
	private JPanel mainPanel;
	private int nodeId;

	public NodeGui (int nodeId) {
		super("System Y");
		this.nodeId = nodeId;
		setBounds (100,100,450,300);
		mainPanel = new JPanel ();
		mainPanel.setLayout (new BoxLayout ( mainPanel, BoxLayout.PAGE_AXIS));
		add(mainPanel);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowListener() {
			public void windowClosing(WindowEvent arg0) {
				try {
					Node.shutdown();
				} catch (IOException e) {
					System.out.println("Fault in GUI: failed to shutdown node:\n" + e);
				}
			}
			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
		});
		panelMap = new HashMap <String,YPanel> ();
		
		Thread checkup = new Thread(){
			public void run () {
				while (true) {
					try {
						Thread.sleep(5100);	// The lists in the file agent only check up every 5 seconds.
					} catch (InterruptedException e) {
						System.out.println("failed to freeze checkup thread. \n" + e);
					}
					updateGui();
				}
			}
		};
		checkup.start();
		
	}
	
	public void updateGui() {
		new fileagent();
		
		HashMap <String, Integer> presentItems = new HashMap <String, Integer> ();
		for (Iterator<String> i = panelMap.keySet().iterator(); i.hasNext();){
			presentItems.put(i.next(), 0);
		}
		
		for (Iterator<Integer> i = fileagent.localList.keySet().iterator(); i.hasNext();) {
			Integer id = i.next();			
			for (Iterator<String> j = fileagent.localList.get(id).keySet().iterator(); j.hasNext();) {
				String element = j.next();
				if (presentItems.containsKey(element)) {
					if(id == nodeId) {
						presentItems.put(element, 2);
						changeLocality(element, true);
					} else {
						if (presentItems.get(element) == 0) {
							presentItems.put(element, 1);
							changeLocality(element, false);
						}
					}
				} else {
					if (id == nodeId) {
						addFile(element, true);
						presentItems.put(element,2);
					} else {
						addFile(element, false);
						presentItems.put(element, 1);
					}
				}			
			}
		}
		for (Iterator <String> i = presentItems.keySet().iterator(); i.hasNext();) {
			String element = i.next();
			if (presentItems.get(element) == 0) {
				deleteFile(element);
			}
		}
	}
	
	private boolean addFile (String fileName, boolean local) {
		if(panelMap.containsKey(fileName)) {
			return false;
		}
		YPanel panel = new YPanel (fileName, local);
		panelMap.put(fileName, panel);
		mainPanel.add(panelMap.get(fileName));
		mainPanel.updateUI();
		return true;
	}
	
	private boolean deleteFile (String fileName) {
		if(!panelMap.containsKey(fileName))
			return false;
		mainPanel.remove(panelMap.get(fileName));
		panelMap.remove(fileName);
		mainPanel.updateUI();
		return true;
	}
	
	public boolean changeLocality (String fileName, boolean local) {
		if(!panelMap.containsKey(fileName))
			return false;
		panelMap.get(fileName).changeLocality(local);
		mainPanel.updateUI();
		return true;
	}

}
