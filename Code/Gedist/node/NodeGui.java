package node;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class NodeGui extends JFrame {
	private static final long serialVersionUID = 1L;
	private HashMap<String, YPanel> panelMap; // Map with all the YPanel's;
	private JPanel mainPanel; // Main pannel, this will contains all the
								// YPanel's of the panelMap.
	private int nodeId; // The id of the node who's steering this GUI.

	public NodeGui(int nodeId) {
		super("System Y"); // Set the title of the GUI.
		this.nodeId = nodeId;
		setBounds(100, 100, 450, 300);
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		add(mainPanel);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // The functionality
															// of the node may
															// not stop when the
															// user press the
															// cros.
		addWindowListener(new WindowListener() { // Call the Node.shutdown()
													// procedure when closing
													// for a legal shutdown.
			public void windowClosing(WindowEvent arg0) { // When the user press
															// the cross.
				Node.shutdown();
			}

			public void windowActivated(WindowEvent e) {
			} // When the user pop open the window.

			public void windowClosed(WindowEvent e) {
			} // When the window disappear.

			public void windowDeactivated(WindowEvent e) {
			} // When the user minimize the window.

			public void windowDeiconified(WindowEvent e) {
			}

			public void windowIconified(WindowEvent e) {
			}

			public void windowOpened(WindowEvent e) {
			}
		});
		panelMap = new HashMap<String, YPanel>();

		Thread checkup = new Thread() { // Thread for update the GUI by the
										// lists of the file agent's.
			public void run() {
				while (true) {
					try {
						Thread.sleep(1100); // The lists in the file agent only
											// check up every 5 seconds.
					} catch (InterruptedException e) {
					}
					updateGui(); // Update the GUI.
				}
			}
		};
		checkup.start();

	}

	public void updateGui() { // Called every second to keep the gui up to date.
		new fileagent(); // The fileagent contains a map with all the files that
							// are circulating in the system.

		HashMap<String, Integer> presentItems = new HashMap<String, Integer>();
		for (Iterator<String> i = panelMap.keySet().iterator(); i.hasNext();) {
			presentItems.put(i.next(), 0); // Take all the file's who are
											// already represented on the GUI.
			// So when can later check witch file's are removed out of the
			// system.
		}

		for (Iterator<Integer> i = fileagent.localList.keySet().iterator(); i.hasNext();) {
			Integer id = i.next();
			try {
				// if (fileagent.localList.get(id).keySet().iterator() != null)
				// {
				for (Iterator<String> j = fileagent.localList.get(id).keySet().iterator(); j.hasNext();) {
					String element = j.next();
					if (presentItems.containsKey(element)) { // If element is
																// already
																// presented on
																// the GUI.
						if (id == nodeId) { // Check if the locality is still
											// right.
							presentItems.put(element, 2);
							changeLocality(element, true);
						} else {
							if (presentItems.get(element) == 0) {
								presentItems.put(element, 1);
								changeLocality(element, false);
							}
						}
					} else { // If element isn't yet presented on the GUI.
						if (id == nodeId) { // Check locality before add the
											// file.
							addFile(element, true);
							presentItems.put(element, 2);
						} else {
							addFile(element, false);
							presentItems.put(element, 1);
						}
					}
					// }
				}
			} catch (Exception e) {

			}

		}
		for (Iterator<String> i = presentItems.keySet().iterator(); i.hasNext();) {
			String element = i.next();
			if (presentItems.get(element) == 0) {
				deleteFile(element); // Delete all the item's who are not
										// presented in the system anymore.
			}
		}
	}

	private boolean addFile(String fileName, boolean local) { // Add a file to
																// the GUI.
		if (panelMap.containsKey(fileName)) {
			return false; // Return false if the file is already represented in
							// the GUI.
		}
		YPanel panel = new YPanel(fileName, local); // Make a new YPanel of the
													// file.
		panelMap.put(fileName, panel); // Put the YPanel in the map.
		mainPanel.add(panelMap.get(fileName)); // Add the panel from the map to
												// the gui (this has a global
												// location).
		mainPanel.updateUI(); // Update the GUI to make the changes visible.
		return true;
	}

	private boolean deleteFile(String fileName) { // Remove a file out of the
													// GUI.
		if (!panelMap.containsKey(fileName))
			return false; // When the file is not represented, false will be
							// returned.
		mainPanel.remove(panelMap.get(fileName));
		panelMap.remove(fileName); // Clear all the evidences that this YPanel
									// have ever exist.
		mainPanel.updateUI(); // Make the changes visible.
		return true;
	}

	public boolean changeLocality(String fileName, boolean local) { // Change
																	// the
																	// locality
																	// of one
																	// file.
		if (!panelMap.containsKey(fileName) || panelMap.get(fileName).getLocality() == local)
			return false; // When the given file doesn't exist or when the file
							// has alreay got the given locality, false will be
							// returned.
		panelMap.get(fileName).changeLocality(local);
		mainPanel.updateUI(); // Make the changes visible.
		return true;
	}

}
