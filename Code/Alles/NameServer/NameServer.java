package NameServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

public class NameServer extends UnicastRemoteObject implements INameServer {

	private static final long serialVersionUID = 1L;
	private TreeMap <Integer, String> nameRegister; // key: node's id; val: node's ip

	NameServer() throws RemoteException { // default constructor.
		super();
		nameRegister = new TreeMap <Integer, String>();
	}

	public String lookUp(int id) { // return the ip by the given id.
		System.out.print("NameServer: lookUp " + id);	// -----report
		String ip = nameRegister.get(id);
		System.out.println("\tip: " + ip);	// ----report
		return ip;	// When the given id isn't represented in the system => null will be returned.
	}

	public boolean add(int id, String adr) { // Add a node to the system; id => node's id; adr => node's ip
		System.out.println("NameServer: add procedure\tid: " + id + "\tip: " + adr);	// ----report
		if (nameRegister.containsKey(id)) {
			return false;	// When the id is already represented in the system.
		}
		nameRegister.put(id, adr);
		System.out.println (nameRegister);
		return true;	// When success
	}

	public boolean delete(int id) {	// Delete a node out of the system.
		System.out.println("NameServer: delete " + id);
		if (nameRegister.containsKey(id)) {
			nameRegister.remove(id);
			System.out.println (nameRegister);
			return true;	// Return true when success.
		}
		return false;	// return false, when the given node didn't exist.
	}

	public int getPrev(int id) {	// Returns the lower neighbor of the given node
		//System.out.println (nameRegister);
		System.out.print("NameServer: " + id + " entered getPrev\tresult: ");	// ----report
		if (!nameRegister.containsKey(id) || nameRegister.size() <= 1) {
			System.out.println("no lower neighbor detected.");	// ----report
			return 0;	// When failure
		}

		Set<Integer> keySet = nameRegister.keySet();	
		Iterator<Integer> i = keySet.iterator();

		int prevId = i.next();
		if (prevId == id) { // When the given id is the first element of the map...
			for (; i.hasNext(); prevId = i.next());
			System.out.println(prevId);
			return prevId; // The lates id is the lower neighbor
		}

		while (true) {	// Check all the node's
			int temp = i.next();
			if (temp == id) {
				System.out.println(prevId);	// ----report
				return prevId;	// Return the result
			}
			prevId = temp;
		}
	}

	public int getNext(int id) {	// Returns the upper neighbor of the given node
		//System.out.println (nameRegister);
		System.out.print("NameServer: " + id + " entered getNext\tresult: ");	// ----report
		if (!nameRegister.containsKey(id) || nameRegister.size() <= 1)
			return 0;	// When their is no upper neighbor

		Set<Integer> keySet = nameRegister.keySet();
		Iterator<Integer> i = keySet.iterator();
		//System.out.println (keySet);
		int firstId = i.next();
		int currentId = firstId;

		while (true) {
			if (!i.hasNext()) {	// When the given node is the latest one ...
				System.out.println(firstId);	// ----report
				return firstId;	// The first node is the upper neighbor.
			}
			if (currentId == id) {	// If the given node is found in the set:
				int result = i.next();
				System.out.println(result);	// ----report
				return result;
			}
			currentId = i.next();
		}
	}
	
	public int getNode(String filename) throws RemoteException {	// Calculate the rightful owner of the given file.
		int hashid = Math.abs(filename.hashCode() % 32768);	// Calc the hash of the file.
		System.out.print("NameServer: " + filename + " entered getNode\tresult:\thash: " + hashid + ", IdNode: "); // ----report
		
		Set<Integer> keySet = nameRegister.keySet();
		Iterator<Integer> i = keySet.iterator();

		int firstId = i.next();
		int currentId = firstId;
		
		while (true){
			if (!i.hasNext()){
				System.out.println(firstId);
				return firstId;
			}
			if (currentId > hashid) {
				System.out.println(currentId);	// ----report
				return currentId;	// The rightful owner is the one with the smallest id higher than the hash.
			}
			currentId = i.next();
		}
		
	}

}
