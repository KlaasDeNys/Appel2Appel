package agent;

public class RMIObject {
	private static final int RMI_PORT = 1099;	// Port for RMI service
	private static final int MULTICAST_PORT = 8889;	// Port for multicast service
	private static final String MULTICAST_IP = "224.2.2.3";	// Multicast messages wil be sended to this address 
	private static final int TCP_PORT = 6790;	// Port use for TCP connections.
	
	public RMIObject() {
		
	}
}
