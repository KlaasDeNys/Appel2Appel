package NameServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * 
 * @author Jens
 * @since 14/10/2015
 *
 */

public class RMIServer {
	
	public RMIServer () {
		
	}
	
	public static void main (String [] args) {
		try {
			NameServer obj = new NameServer ();
			Registry registry = LocateRegistry.createRegistry (1099);
			registry.bind("LNS", obj);
			System.out.println("System online");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
