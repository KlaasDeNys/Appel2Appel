package node;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class NodeMain {
	public static void main (String [] args) throws RemoteException {
		Node node = new Node ("node1");
		
		while (node.bootstrap);
		
		try {
			Registry registry = LocateRegistry.createRegistry(1099);
			registry.bind ("node", node);
		} catch (Exception e) {
			System.out.println ("Node message: RMI: Exception:\n" + e);
		}
		
		System.out.println ("Press 0 to shut down\n > ");
		Scanner scanner = new Scanner (System.in);
		
		scanner.next();
		
		node.shutdown ();
		
		scanner.close();
		System.exit (0);
	}
}
