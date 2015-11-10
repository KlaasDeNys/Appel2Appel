package NameServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMINode {
	public RMINode () {
		
	}
	
	public static void main (String [] args) {
		try {
			System.out.println("System online");
			Node obj = new Node();
			obj.main(args);
			Registry registry = LocateRegistry.createRegistry (1099);
			registry.bind("Node", obj);
			
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
