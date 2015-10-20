package NameServer;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

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
		
		return true;
	}

	public boolean delete (int id){
		System.out.println ("LookupServer: delete procedure");
		if (nameRegister.containsKey(id)) {
			nameRegister.remove(id);			
			return true;
		}
		return false;
	}
	
	public int getPrev (int id) {
		if (!nameRegister.containsKey (id) || nameRegister.size() <= 1) return 0;
		
		Set <Integer> keySet = nameRegister.keySet ();
		Iterator <Integer> i = keySet.iterator();
		
		int prevId = i.next();
		if (prevId == id) {	// When the given id is the first element of the map...
			for (;i.hasNext();prevId = i.next());
			return prevId;	// We must get the latest element of the set.
		}
		
		while (true) {
			int temp = i.next();
			if (temp == id) return prevId;
			prevId = temp;
		}
		
		return 0;
	}
	
	public int getNext (int id) {
		if (!nameRegister.containsKey (id) || nameRegister.size() <= 1) return 0;
		
		Set <Integer> keySet = nameRegister.keySet ();
		Iterator <Integer> i = keySet.iterator();
		
		int firstId = i.next();
		int currentId = firstId;
		
		while (true) {
			if (!i.hasNext()) return firstId;
			if (currentId == id) return i.next();
			currentId = i.next();
		}
		
		return 0;
	}
}
