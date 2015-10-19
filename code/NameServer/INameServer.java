import java.rmi.Remote;

/**
 * 
 * @author Jens
 * @since 14/10/2015
 *
 */

public interface INameServer extends Remote {

	public boolean addNode(int id, String ip); // Add a node to the map
	// return true when success
	// return false with failure (incorrect IP, no unique id)

	public boolean deleteNode(int id); // Delete a node from the map
	// return true when success
	// return false when failure (unknown id)

	public String lookUpFile(String name); // Get the IP of the location of the
											// given file.
	// return null with failure

}
