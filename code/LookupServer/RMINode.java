package NameServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMINode {
	public RMINode () {
		
	}
	
	public static void main (String [] args) {
		try {
			Node obj = new Node();
			obj.main(args);
			Registry registry = LocateRegistry.createRegistry (1098);
			registry.bind("Node", obj);
			System.out.println("System online");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
