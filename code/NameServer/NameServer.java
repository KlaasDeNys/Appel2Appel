import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

/**
 * 
 * @author Jens
 * @since 14/10/2015
 *
 */

public class NameServer extends UnicastRemoteObject implements INameServer {

	private Map<Integer, String> nodeMap; // A map where an id is linked to an
											// IP address.

	protected NameServer() throws RemoteException {	// default constructor
		super();
	}

	public boolean addNode(int id, String ip) { // Add a node to the map
		if (!checkIp(ip) || !nodeMap.containsKey(id) || id < 0 || id > 32767)
			// The given String has to be an IP address
			// The given id has to be unique
			// The given id has to be in the range [0;32767]
			return false;
		nodeMap.put(id, ip); // map the IP address to the given id.
		return true;
	}

	public boolean deleteNode(int id) { // Delete a node from the map
		if (!nodeMap.containsKey(id))
			return false;	// return false on unknown id
		nodeMap.remove(id);
		return true;
	}

	public String lookUpFile(String name) { // Get the IP of the location of the
											// given file
		int hash = hasher(name);	// hash the given file name
		int i = hash;
		do {
			if (nodeMap.containsKey(i))
				return nodeMap.get(i);

			i++;
			if (i == 32768 || i < 0)
				i = 0;
		} while (i != hash);

		return null;
	}

	private boolean checkIp(String ip) {	// Check if given String is a correct IP address
		int n = 0;
		int m = 0;
		for (int i = 0; i < ip.length(); i++) {
			if (ip.charAt(i) >= '0' && ip.charAt(i) <= '9') {
				n = n * 10 + (ip.charAt(i) - '0');
			} else if (ip.charAt(i) == '.') {
				m++;
				if (n > 255)	// Numbers has to be in the range [0;255]
					return false;
				n = 0;
			} else {	// '.' and numbers are the only committed characters.
				return false;
			}
		}
		if (n > 255 || m != 3)	// There may only be 4 numbers in the address
			return false;
		return true;
	}

	private int hasher(String fileName) { // Create has code from the given file
											// name
		return Math.abs(fileName.hashCode() % 32768);
	}
}
