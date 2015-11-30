package node;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/*
 *
 *	NodeMain have the usasge to launch te RMI service of a node.
 *
 *	Their is only a main methode.
 *
 */

public class NodeMain {
	public static boolean RMIdone= false;
	public static void main (String [] args) throws InterruptedException, IOException {
		Node node = new Node ();	// Create a new Node
		try {	// Launch RMI
			Registry registry = LocateRegistry.createRegistry(1099);
			registry.bind ("node", node);
			
		} catch (Exception e) {
			System.out.println ("Node message: RMI: Exception:\n" + e);
		}
		while (node.bootstrap);	// The RMI service may only launch when the node has started up for 100 %
		(new ThreadFiles()).start();
		
		
		RMIdone=true;
		
		while (true) {	// Standard  work process.
			System.out.println ("\n[0] shut down\n > ");
			Scanner scanner = new Scanner (System.in);
			int choise = scanner.nextInt();
			switch (choise) {
			case 0:		// Remove this node from the system.
				node.shutdown ();
				scanner.close();
				System.exit(0);
				break;
			default:	// Wrong input.
				System.out.println (choise + " is not an option.");
				break;
			}
		}
		
	}
}
