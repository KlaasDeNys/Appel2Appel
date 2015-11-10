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
	private HashMap<Integer, String> nameRegister; // Here we will map te ip's
													// to the id's

	NameServer() throws RemoteException { // default constructor.

		// LookupServer() throws RemoteException { // default constructor.

		super();
		nameRegister = new HashMap<Integer, String>();
	}

	public String lookUp(int id) { // return the ip by the given id.
		System.out.print("NameServer: lookUp " + id);
		String ip = nameRegister.get(id);
		System.out.println("\tip: " + ip);
		return ip;
	}

	public boolean add(int id, String adr) { // Add a node to the map.
		System.out.println("NameServer: add procedure\tid: " + id + "\tip: " + adr);
		if (nameRegister.containsKey(id)) {
			return false;
		}
		nameRegister.put(id, adr);

		return true;
	}

	public boolean delete(int id) {
		System.out.println("NameServer: delete " + id);
		if (nameRegister.containsKey(id)) {
			nameRegister.remove(id);
			return true;
		}
		return false;
	}

	public int getPrev(int id) {
		System.out.print("NameServer: " + id + " entered getPrev\tresult: ");
		if (!nameRegister.containsKey(id) || nameRegister.size() <= 1) {
			System.out.println();
			return 0;
		}

		Set<Integer> keySet = nameRegister.keySet();
		Iterator<Integer> i = keySet.iterator();

		int prevId = i.next();
		if (prevId == id) { // When the given id is the first element of the
							// map...
			for (; i.hasNext(); prevId = i.next())
				;
			System.out.println(prevId);
			return prevId; // We must get the latest element of the set.
		}

		while (true) {
			int temp = i.next();
			if (temp == id) {
				System.out.println(prevId);
				return prevId;
			}
			prevId = temp;
		}
	}

	public int getNext(int id) {
		System.out.println("NameServer: " + id + " entered getNext\tresult: ");
		if (!nameRegister.containsKey(id) || nameRegister.size() <= 1)
			return 0;

		Set<Integer> keySet = nameRegister.keySet();
		Iterator<Integer> i = keySet.iterator();

		int firstId = i.next();
		int currentId = firstId;

		while (true) {
			if (!i.hasNext()) {
				System.out.println(firstId);
				return firstId;
			}
			if (currentId == id) {
				int result = i.next();
				System.out.println(result);
				return result;
			}
			currentId = i.next();
		}
	}
	
	public int getNode(String filename) throws RemoteException {
		int hashid = Math.abs(filename.hashCode() % 32768);
		System.out.print("NameServer: " + filename + " entered getNode\tresult:\thash: " + hashid + ", IdNode: ");
		
		Set<Integer> keySet = nameRegister.keySet();
		Iterator<Integer> i = keySet.iterator();

		int firstId = i.next();
		int currentId = firstId;

		while (true) {
			if (!i.hasNext()) {
				System.out.println(firstId);
				return firstId;
			}
			if (currentId >= hashid) {
				int result = i.next();
				System.out.println(result);
				return result;
			}
			currentId = i.next();
		}

	}

}
