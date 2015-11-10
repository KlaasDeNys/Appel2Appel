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

/**
 * 
 * @author Jens
 * @since 14/10/2015
 *
 */

public class RMIServer {
	
	public RMIServer () {
		
	}
	
	public static void main (String [] args) {
		try {
			NameServer obj = new NameServer ();
			Registry registry = LocateRegistry.createRegistry (1099);
			registry.bind("LNS", obj);
			System.out.println("System online");
		} catch (Exception e) {
			System.out.println("RMIServer RMI: " + e);
		}
		
		MulticastSocket socket = null;
		DatagramPacket inPacket = null;
		byte [] inBuf = new byte [256];
		
		try {
			// Prepare to join multicast group
			socket = new MulticastSocket(8888);
			InetAddress address = InetAddress.getByName("224.2.2.3");
			socket.joinGroup(address);
			
			while (true) {
				inPacket = new DatagramPacket (inBuf, inBuf.length);
				socket.receive(inPacket);
				String msg = new String (inBuf, 0, inPacket.getLength());
				sendIp (msg);
			}
		} catch (IOException e) {
			System.out.println ("RMIServer Multicast: " + e);
		}
	}
	
	private static void sendIp (String nodeIp) {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(); // create socket

			String mes = ip ();
			byte[] m = mes.getBytes();
			InetAddress aHost = InetAddress.getByName(nodeIp);

			int serverPort = 6789;
			DatagramPacket request = new DatagramPacket(m, mes.length(), aHost, serverPort);
			aSocket.send(request); // send message

		} catch (SocketException e) {
			System.out.println("SocketException: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		}
	}
	
	private static String ip () {
		InetAddress address;
		String hostIp = "";
		try {
			address = InetAddress.getLocalHost();
			hostIp = address.getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println ("UnknownHostException: " + e);
			return null;
		}
		return hostIp;
	}
}
