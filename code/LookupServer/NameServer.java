package NameServer;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * 
 * @author Jens
 * @since 14/10/2015
 *
 */

public class NameServer extends UnicastRemoteObject implements INameServer {
	
	private static final long serialVersionUID = 1L;
	private HashMap <Integer, String> nameRegister;	// Here we will map te ip's to the id's


	NameServer() throws RemoteException {	// default constructor.

	LookupServer() throws RemoteException {	// default constructor.

		super();
		nameRegister = new HashMap <Integer, String> ();
	}
	
	public String lookUp (int id) {	// return the ip by the given id.
		return nameRegister.get(id);
	}
	
	public boolean add (int id, String adr) {	// Add a node to the map.
		System.out.println ("LookupServer: add procedure");
		if (nameRegister.containsKey(id)) {
			return false;
		}
		nameRegister.put(id, adr);
		
		return "66.66.66.66";
	}

	public boolean delete (int id){
		System.out.println ("LookupServer: delete procedure");
		if (nameRegister.containsKey(id)) {
			nameRegister.remove(id);			
			return true;
		}
		return false;
	}
}
