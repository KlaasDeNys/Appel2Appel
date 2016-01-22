  package node;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


/*
 *
 *	NodeMain have the usage to launch the RMI service of a node.
 *
 *	Their is only a main method.
 *
 */

public class NodeMain {
	public static boolean RMIdone= false;
	
	public static void main (String [] args) throws InterruptedException, IOException {
		Node node = new Node ();	// Create a new Node
		//agent.FileListAgent fileAgent = new FileListAgent(); //Create a new FileListAgent
		try {	// Launch RMI
			Registry registry = LocateRegistry.createRegistry(1099);
			registry.bind ("node", node);
			
		} catch (Exception e) {
			new errorReport ("RMI error","Failed to launch RMI service.");
		}
		while (node.bootstrap);	// The RMI service may only launch when the node has started up for 100 %
		(new ThreadFiles()).start();
		
		node.setNextNode();
		node.setPrevNode();
		RMIdone=true;
	}
}
