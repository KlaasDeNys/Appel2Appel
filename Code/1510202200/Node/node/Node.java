package node;

import NameServer.INameServer;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.awt.List;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class Node extends UnicastRemoteObject implements INode {
	private static final long serialVersionUID = 1L;
	private String name;
	private String lnsIp;

	private int idNext;
	private String ipNext;
	private int idPrev;
	private String ipPrev;

	private String pathLokaal = "c://lokaal/";
	private String pathReplica = "c://replica/";
	public static HashMap<String, Integer> local = new HashMap<String, Integer>();
	public static HashMap<String, Integer> replica = new HashMap<String, Integer>();

	public boolean bootstrap;

	public Node(String name) throws RemoteException, InterruptedException { // Constructor
		super();
		bootstrap = true;
		this.name = name; // Save name of node.
		getNameServerIp(); // Look for the ip of the name Server.
		while (!addToSystem()) { // Try to add this node to the system.
			this.name = this.name + "_"; // When failed: auto change name, and
											// start over.
		}
		setNextNode(); // Make connection with the next node.
		setPrevNode(); // Make connection with the previous node.

		bootstrap = false; // Bootstrap done
		
		//final File folder = new File(pathLokaal);
		//HashMap<String, Integer> local = listLocalFiles(folder);
		//final File folder1 = new File(pathReplica);
		//HashMap<String, Integer> replica = listReplicaFiles(folder1);
	    System.out.println("Contents of local files: " + local);
	    System.out.println("Contents of replica files: " + replica);
	    doubles(local,replica);
	}

	public void changeNextNode(int id, String ip) { // Remote function to change
													// the next node of this
													// node.
		idNext = id;
		ipNext = ip;
		System.out.println("Node message: next node is changed: id: " + id + " ip: " + ip);
	}

	public void changePrevNode(int id, String ip) { // Remote function to change
													// the previous node of this
													// node.
		idPrev = id;
		ipPrev = ip;
		System.out.println("Node message: prev node is changed: id: " + id + " ip: " + ip);

	}

	public void shutdown() { // Call to shut down this node.
		try {
			INode nextNode = (INode) Naming.lookup("//" + ipNext + "/node");
			nextNode.changePrevNode(idPrev, ipPrev);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			System.out.println("failed to connect to nextNode");
		}

		try {
			INode prevNode = (INode) Naming.lookup("//" + ipPrev + "/node");
			prevNode.changeNextNode(idNext, ipNext);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			System.out.println("failed to connect to prevNode");
		}

		try {
			INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
			lns.delete(hasher(name));
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			System.out.println("failed to connect the server");
		}
	}

	private int hasher(String hashName) { // Make a hash from the given String
		return Math.abs(hashName.hashCode() % 32768);
	}

	private void getNameServerIp() { // Look for the ip of the name server
		DatagramSocket socket = null;
		DatagramPacket outPacket = null;
		byte[] outBuf;
		try {
			socket = new DatagramSocket(); // Send this ip by unicasting to the
											// server.
			outBuf = ip().getBytes();
			InetAddress address = InetAddress.getByName("224.2.2.3");
			outPacket = new DatagramPacket(outBuf, outBuf.length, address, 8888);
			socket.send(outPacket);
		} catch (IOException e) {
			System.out.println("multicast: IOException\n" + e);
		}
		socket.close();

		DatagramSocket aSocket = null;
		try { // Open UDP port, server will answer after 5 seconds with his ip.
			aSocket = new DatagramSocket(6789);
			byte[] buffer = new byte[1000];
			DatagramPacket request = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(request);
			lnsIp = new String(request.getData());
			lnsIp = trim(lnsIp);
		} catch (SocketException e) {
			System.out.println("UDP: SocketException: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("UDP: IOException: " + e.getMessage());
		}
		aSocket.close();
	}

	private boolean addToSystem() { // Add this node to the system.

		try {
			INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
			if (lns.add(hasher(name), ip())) { // add to server by de hash of
												// the node's name and his ip.
				return true;
			}
		} catch (MalformedURLException e) {
			System.out.println("Node.addToSystem (): MalformedURLException\n\n" + e);
		} catch (RemoteException e) {
			System.out.println("Node.addToSystem (): RemoteException\n\n" + e);
		} catch (NotBoundException e) {
			System.out.println("Node.addToSystem (): NotBoundException\n\n" + e);
		}
		System.out.println("Node.addToSystem ():\tfailled to add id");
		return false;
	}

	private boolean setNextNode() {
		try {
			INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
			idNext = lns.getNext(hasher(name));
			if (idNext == 0) {
				ipNext = null;
				return false;
			} else {
				ipNext = lns.lookUp(idNext);
				System.out.println("Node message: Next node: ip: " + ipNext + " id: " + idNext);
			}
		} catch (MalformedURLException e) {
			System.out.println("Node: setNextNode (): MalformedURLException:\n" + e);
			return false;
		} catch (RemoteException e) {
			System.out.println("Node: setNextNode (): RemoteException:\n" + e);
			return false;
		} catch (NotBoundException e) {
			System.out.println("Node: setNextNode (): NotBoundException:\n" + e);
			return false;
		}

		try {
			INode nextNode = (INode) Naming.lookup("//" + ipNext + "/node");
			nextNode.changePrevNode(hasher(name), ip());
			return true;
		} catch (MalformedURLException e) {
			System.out.println("Node: setNextNode (): MalformedURLException: (to node)\n" + e);
		} catch (RemoteException e) {
			System.out.println("Node: setNextNode (): RemoteException: (to node)\n" + e);
		} catch (NotBoundException e) {
			System.out.println("Node: setNextNode (): NotBoundException: (to node)\n" + e);
		}
		return false;
	}

	private boolean setPrevNode() {
		try {
			INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
			idPrev = lns.getPrev(hasher(name));
			if (idPrev == 0) {
				ipPrev = null;
				return false;
			} else {
				ipPrev = lns.lookUp(idPrev);
				System.out.println("Node message: Prev node: ip: " + ipPrev + " id: " + idPrev);
			}
		} catch (MalformedURLException e) {
			System.out.println("Node: setPrevNode (): MalformedURLException:\n" + e);
			return false;
		} catch (RemoteException e) {
			System.out.println("Node: setPrevNode (): RemoteException:\n" + e);
			return false;
		} catch (NotBoundException e) {
			System.out.println("Node: setPrevNode (): NotBoundException:\n" + e);
			return false;
		}

		try {
			INode prevNode = (INode) Naming.lookup("//" + ipPrev + "/node");
			prevNode.changeNextNode(hasher(name), ip());
			System.out.println("Node message: prev node know me");
			return true;
		} catch (MalformedURLException e) {
			System.out.println("Node: setPrevNode (): MalformedURLException: (to node)\n" + e);
		} catch (RemoteException e) {
			System.out.println("Node: setPrevNode (): RemoteException: (to node)\n" + e);
		} catch (NotBoundException e) {
			System.out.println("Node: setPrevNode (): NotBoundException: (to node)\n" + e);
		}
		return false;
	}

	private String ip() { // Returns the ip of this system.
		InetAddress address;
		String hostIp = "";
		try {
			address = InetAddress.getLocalHost();
			hostIp = address.getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println("UnknownHostException: " + e);
			e.printStackTrace();
			return null;
		}

		return hostIp;
	}

	private String trim(String sIn) { // Trim's a message to a pure ip address.
		int endIndex;
		for (endIndex = 0; (sIn.charAt(endIndex) >= '0' && sIn.charAt(endIndex) <= '9')
				|| sIn.charAt(endIndex) == '.'; endIndex++)
			;
		return sIn.substring(0, endIndex);
	}

	public int lookupFile(String filename) {
		try {
			INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
			return lns.getNode(filename);

		} catch (MalformedURLException e) {
			System.out.println("Node.lookupFile (): MalformedURLException\n\n" + e);
		} catch (RemoteException e) {
			System.out.println("Node.lookupFile (): RemoteException\n\n" + e);
		} catch (NotBoundException e) {
			System.out.println("Node.lookupFile (): NotBoundException\n\n" + e);
		}
		return (Integer) null;
	}

	public void deletefile(String filename) throws RemoteException {
		try {

			File file = new File(pathReplica + filename);

			if (file.delete()) {
				System.out.println(file.getName() + " is deleted!");
			} else {
				System.out.println("Delete operation is failed.");
			}

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	// __________________________________________________________________________
	public HashMap<String, Integer> listLocalFiles(final File folder) {
		HashMap<String, Integer> local = new HashMap<String, Integer>();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listLocalFiles(fileEntry);
			} else {
				local.put(fileEntry.getName(), hasher(fileEntry.getName()));

			}
		}
		return local;
	}

	public HashMap<String, Integer> listReplicaFiles(final File folder1) {
		HashMap<String, Integer> replica = new HashMap<String, Integer>();
		for (final File fileEntry : folder1.listFiles()) {
			if (fileEntry.isDirectory()) {
				listReplicaFiles(fileEntry);
			} else {
				replica.put(fileEntry.getName(), hasher(fileEntry.getName()));
			}
		}

		return replica;
	}
	public void doubles(HashMap<String, Integer> local,HashMap<String, Integer> replica ) throws InterruptedException
	{
		TimeUnit.SECONDS.sleep(10);
		final File folder = new File(pathLokaal);
		HashMap<String, Integer> localNew = listLocalFiles(folder);
		ArrayList<String> localNewList = new ArrayList<String>(localNew.keySet());
		ArrayList<String> localList = new ArrayList<String>(local.keySet());
		compare(localList,localNewList);
	    System.out.println("Contents of local files: " + localList);
	    System.out.println("Contents of local new files: " + localNewList); 
	    //local= hash( localNewList);
	}
	
	public  void compare(ArrayList<String> al1,ArrayList<String> al2){

        //Check copy to node
        ArrayList<Integer> al3= new ArrayList<Integer>();
        for (String temp1 : al2)
             al3.add(al1.contains(temp1) ? 1 : 0);
        System.out.println("check to copy: "+al3);
        
        //Check delete file
        ArrayList<Integer> al4= new ArrayList<Integer>();
        for (String temp2 : al1)
             al4.add(al2.contains(temp2) ? 1 : 0);
        System.out.println("check to delete: "+al4);
        
        
        for (int i = 0; i < al3.size(); i++) {
			int bool = (al3.get(i));
			if (bool == 0)
			{
	  			String toevoegen = al2.get(i);
	  			copyToNode(toevoegen);
	  			System.out.println("toevoegen: "+toevoegen);
			}
        }
		for (int ii = 0; ii < al4.size(); ii++) {
			int bool1 = (al4.get(ii));
			
	  		if (bool1 == 0)
	  		{
	  			String verwijder = al1.get(ii);
	  			System.out.println("Verwijder: "+verwijder);
	  			//deletefile(verwijder);
	  		}
		}
	}

	public void copyToNode(String filename) {
		String ipfilenode = "";
		try {
			INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
			int idnode = lns.getNode(filename);
			if (idnode == hasher(name)) {
				idnode = idNext;
			}
			ipfilenode = lns.lookUp(idnode);
			System.out.println(ipfilenode + ", kopieren wordt gestart, " + filename);
		} catch (MalformedURLException e) {
			System.out.println("Node.getNode (): MalformedURLException\n\n" + e);
		} catch (RemoteException e) {
			System.out.println("Node.getNode (): RemoteException\n\n" + e);
		} catch (NotBoundException e) {
			System.out.println("Node.getNode (): NotBoundException\n\n" + e);
		}

		try {
			INode copynode = (INode) Naming.lookup("//" + ipfilenode + "/node");
			copynode.getFile(ip(), filename);

		} catch (MalformedURLException e) {
			System.out.println("Node: getFile (): MalformedURLException: (to node)\n" + e);
		} catch (RemoteException e) {
			System.out.println("Node: getFile (): RemoteException: (to node)\n" + e);
		} catch (NotBoundException e) {
			System.out.println("Node: getFile (): NotBoundException: (to node)\n" + e);
		}

	}
	 public void run() throws InterruptedException {
		   boolean flag = true;
		   System.out.println("Thread control files started...");
		   try{
			   
			   Thread.sleep(5000);	//Checks every 5s
		   } catch(Exception e){
			   System.out.println("Thread is ended! The error is " + e.getMessage());
		   }
		   if(!flag)		//Only when the flag is false, the thread ends
			   return;
		   else
			   run();
		   final File folder = new File(pathLokaal);
			/*HashMap<String, Integer> local = listLocalFiles(folder);
			final File folder1 = new File(pathReplica);
			HashMap<String, Integer> replica = listReplicaFiles(folder1);
		   */
		   System.out.println("Contents of local files: " + local);
		    System.out.println("Contents of replica files: " + replica);
		    doubles(local,replica);
	   }
	
	@Override
	public void getFile(String ip, String filename) {
		// TODO Auto-generated method stub

	}

}