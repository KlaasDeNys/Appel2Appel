package NameServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMINode {
	public RMINode () {
		
	}
	
	public static void main (String [] args) {
		try {
			Node obj = new Node();
			Registry registry = LocateRegistry.createRegistry (1099);
			registry.bind("LNS", obj);
			System.out.println("System online");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
