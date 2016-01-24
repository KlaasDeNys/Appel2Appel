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

/*
 * The RMIServer has 2 services who's running simultaneously.
 * 
 * The RMI: Node's can manage the server by calling his methods via Remote method invocation.
 * For this service, the Node's has to know the ip of the server.
 * When a node entered the system, he can send a multicast message to ask the ip.
 */

public class RMIServer {
	
	private static final int RMI_PORT = 1099;	// Port for RMI service
	private static final int MULTICAST_PORT = 8888;	// Port for multicast service
	private static final String MULTICAST_IP = "224.2.2.3";	// Multicast messages wil be sended to this address 
	private static final int TCP_PORT = 6789;	// Port use for TCP connections.
	
	public RMIServer () {
		
	}
	
	public static void main (String [] args) {	// Main program.
		try {		// launch RMI service
			NameServer obj = new NameServer ();
			Registry registry = LocateRegistry.createRegistry (RMI_PORT);
			registry.bind("LNS", obj);
			new errorReport ("Server message", "System Y online");
		} catch (Exception e) {
			new errorReport ("RMI error", "Failure when launching RMI service.");
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
				socket.receive(inPacket);	// Wait for an incoming package of a new node.
				String msg = new String (inBuf, 0, inPacket.getLength());	// The content of the message is the requesting node.
				sendIp (msg);	// Send server's ip to the node.
			}
		} catch (IOException e) {
			new errorReport ("Multicast error","Failed to receive message in multicast Socket at port " + MULTICAST_PORT + ". (IOException)");
		}
	}
	
	private static void sendIp (String nodeIp) {	// Send own ip to the node who use the given ip.
		DatagramSocket aSocket = null;
		try {	// Send ip over TCP connection.
			aSocket = new DatagramSocket();

			String message = ip ();	// Server's ip is the message
			byte[] bMessage = message.getBytes();
			InetAddress aNode = InetAddress.getByName(nodeIp);

			DatagramPacket request = new DatagramPacket(bMessage, message.length(), aNode, TCP_PORT);	// TCP package with the ip address.
			aSocket.send(request);

		} catch (SocketException e) {
			new errorReport ("TCP error","Failed to send server's ip to " + nodeIp + " on port " + TCP_PORT + ". (SocketException)");
		} catch (IOException e) {
			new errorReport ("TCP error","Failed to send server's ip to " + nodeIp + " on port " + TCP_PORT + ". (IOException)");
		}
	}
	
	private static String ip () {	// Return ip of this device.
		InetAddress address;
		String hostIp = "";
		try {
			address = InetAddress.getLocalHost();
			hostIp = address.getHostAddress();
		} catch (UnknownHostException e) {
			new errorReport ("Server can't determine system's ip. Be sure your network interface is configured. (UnknownHostException)");
			return null;
		}
		return hostIp;
	}
}
