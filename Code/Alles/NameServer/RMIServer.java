package NameServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
 
public class RMIServer {
	
	private static final int RMI_PORT = 1025;	// Port for RMI service
	private static final int MULTICAST_PORT = 8888;	// Port for multicast service
	private static final String MULTICAST_IP = "224.2.2.3";	// Multicast messages wil be sended to this address 
	private static final int TCP_PORT = 6789;	// Port use for TCP connections.
	
	public RMIServer () {
		
	}
	
	public static void main (String [] args) {
		try {		// launch RMI service
			NameServer obj = new NameServer ();
			Registry registry = LocateRegistry.createRegistry (RMI_PORT);
			registry.bind("LNS", obj);
			System.out.println("System online");	// -----report
		} catch (Exception e) {
			System.out.println("RMIServer main error:\nfailed to start RMI service.");
		}
		
		MulticastSocket socket = null;
		DatagramPacket inPacket = null;
		byte [] inBuf = new byte [256];
		
		try {	// share ip service
			// Prepare to host multicast group
			socket = new MulticastSocket(MULTICAST_PORT);
			InetAddress address = InetAddress.getByName(MULTICAST_IP);
			socket.joinGroup(address);
			
			while (true) {	// Run multicast service
				inPacket = new DatagramPacket (inBuf, inBuf.length);
				socket.receive(inPacket);
				String msg = new String (inBuf, 0, inPacket.getLength());
				sendIp (msg);
			}
		} catch (IOException e) {
			System.out.println ("RMIServer main error:\nMulticast service failed.");
		}
	}
	
	private static void sendIp (String nodeIp) {	// called in the main, when a node asks for the server's ip
		DatagramSocket aSocket = null;
		try {	// Send ip over TCP connection.
			aSocket = new DatagramSocket();

			String message = ip ();	// Server's ip is the message
			byte[] bMessage = message.getBytes();
			InetAddress aNode = InetAddress.getByName(nodeIp);

			DatagramPacket request = new DatagramPacket(bMessage, message.length(), aNode, TCP_PORT);
			aSocket.send(request);

		} catch (SocketException e) {
			System.out.println("RMIServer sendIp error:\nSocketException");
		} catch (IOException e) {
			System.out.println("RMIServer sendIp error:\nIOException");
		}
	}
	
	private static String ip () {	// Return ip of this device.
		InetAddress address;
		String hostIp = "";
		try {
			address = InetAddress.getLocalHost();
			hostIp = address.getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println ("RMIServer ip error:\nFailed to discover server ip");
			return null;
		}
		return hostIp;
	}
}
