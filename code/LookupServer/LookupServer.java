package server;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * 
 * @author Jens
 * @since 14/10/2015
 *
 */

public class LookupServer extends UnicastRemoteObject implements ILookupServer {
	
	private static final long serialVersionUID = 1L;
	private HashMap <Integer, String> nameRegister;

	LookupServer() throws RemoteException {
		super();
		nameRegister = new HashMap <Integer, String> ();
	}
	
	public String lookUp (int id) {
		return nameRegister.get(id);
	}
	
	public boolean add (int id, String adr) {
		System.out.println ("LookupServer: add procedure");
		if (nameRegister.containsKey(id)) {
			return false;
		}
		nameRegister.put(id, adr);
		
		return true;
	}
}
