import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * 
 * @author Klaas
 * @since 14/10/2015
 *
 */

public class RMIServer {

	public RMIServer() {

	}

	public static void main(String[] args) {
		try {
			NameServer obj = new NameServer();
			Registry registry = LocateRegistry.createRegistry(1099);
			registry.rebind("NameServer", obj);
		} catch (Exception e) {
			System.out.println("RMIserver error: " + e);
		}
	}
}
