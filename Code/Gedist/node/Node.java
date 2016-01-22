package node;

import NameServer.INameServer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Node extends UnicastRemoteObject implements INode {
	private static final long serialVersionUID = 1L;
	private static String name; // Name of node, used to calc the id.
	public static String lnsIp;

	public static int idNext; // Id number of the next node.
	public static String ipNext; // ip of the next node.
	public static int idPrev; // Id number of the previous node.
	public static String ipPrev; // Ip of the previous node.
	public static int idOwn;

	public static String pathLokaal = "c://lokaal/";
	public static String pathReplica = "c://replica/";
	public static HashMap<String, Integer> local = new HashMap<String, Integer>();
	public static HashMap<String, Integer> replica = new HashMap<String, Integer>();
	public static HashMap<String, Boolean> filesSystemNode = new HashMap<String, Boolean>();
	// private static Agent ag = new Agent(){;
	// public void run(){}};
	public boolean bootstrap;

	public static NodeGui gui;

	/*************************
	 * public Process Methods
	 *************************/

	public Node() throws InterruptedException, IOException { // Constructor
		super();
		deleteReplica();
		bootstrap = true;
		Random randomGenerator = new Random();
		name = "" + randomGenerator.nextInt(3000); // Save name of node.
		getNameServerIp(); // Look for the ip of the name Server.
		while (!addToSystem()) { // Try to add this node to the system.
			Node.name = Node.name + "_"; // When failed: auto change name, and
		} // start over.
		idOwn = hasher(name);
		System.out.println("Eigen ID: " + idOwn);
		bootstrap = false; // Bootstrap done
		// while (NodeMain.RMIdone==false);
		// setNextNode(); // Make connection with the next node.
		// setPrevNode(); // Make connection with the previous node.

		gui = new NodeGui(idOwn);
		gui.setVisible(true);
	}

	public static void shutdown() throws IOException { // Call to shut down this
														// node.
		/*
		 * 
		 * Steps to shutdown a node: Remove itself out of the nameServer. Change
		 * the prev and next node in the neighbor nodes; Transfer replicas to
		 * next node. Ask to agent if their are any other local copy's of the
		 * files in own local folder. Remove the replicas of this node's local
		 * files in other node's replica folders.
		 * 
		 */

		// Remove own Hashmap
		fileagent.localList.remove(idOwn);
		fileagent.replicaList.remove(idOwn);

		// Send Hashmap to NextNode and catch
		if (ipNext != null) {
			try {
				INode nextnode = (INode) Naming.lookup("//" + ipNext + "/node");
				nextnode.refreshAgent(fileagent.localList, fileagent.replicaList);
			} catch (MalformedURLException e) {
				fileagent.failure();
			} catch (RemoteException e) {
				fileagent.failure();
			} catch (NotBoundException e) {
				fileagent.failure();
			}
		}

		if (idNext == idOwn || idNext == 0) {
			// Remove itself out of the nameserver
			try {
				INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
				lns.delete(hasher(name));
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				System.out.println("failed to connect the server");
			}
			return;
		}
		// Change the prev and next node in the neighbor nodes; Transfer
		// replicas to next node.
		if (ipNext != null && ipNext != ip()) {
			final File folder1 = new File(pathReplica);
			HashMap<String, Integer> replicaNew = listLocalFiles(folder1);
			ArrayList<String> replicaNewList = new ArrayList<String>(replicaNew.keySet());
			ArrayList<String> replicaList = new ArrayList<String>(replica.keySet());
			compare(replicaList, replicaNewList);
			System.out.println("Contents of replica: " + replicaList);
			if (replicaList.size() != 0) {
				move(replicaList, idNext);
			}
		}
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
		// Remove itself out of the nameserver
		try {
			INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
			lns.delete(hasher(name));
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			System.out.println("failed to connect the server");
		}

		// Ask to agent if their are any other local copy's of the files in own
		// local folder.
		// Remove the replicas of this node's local files in other node's
		// replica folders.
		final File folder1 = new File(pathLokaal);
		HashMap<String, Integer> lokaal = listLocalFiles(folder1);
		ArrayList<String> lokaalList = new ArrayList<String>(lokaal.keySet());
		System.out.println("Contents of lokaal: " + lokaalList);

		for (int i = 0; i < lokaalList.size(); i++) {
			String filename = lokaalList.get(i);
			if (false == fileagent.existLocal(idOwn, filename)) {

				System.out.println("Bestaat nergens anders " + filename);
				String ipfilenode;
				try {
					INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
					int idnode = lns.getNode(filename);
					ipfilenode = lns.lookUp(idnode);
					System.out.println("ID delete: " + idnode);
					try {
						INode node = (INode) Naming.lookup("//" + ipfilenode + "/node");
						try {
							node.deletefile(filename, pathReplica, false);
						} catch (IOException e) {
							e.printStackTrace();
						}

					} catch (MalformedURLException | RemoteException | NotBoundException e) {
						e.printStackTrace();
					}
				} catch (MalformedURLException e) {
					System.out.println("Node.getNode (): MalformedURLException\n\n" + e);
				} catch (RemoteException e) {
					System.out.println("Node.getNode (): RemoteException\n\n" + e);
				} catch (NotBoundException e) {
					System.out.println("Node.getNode (): NotBoundException\n\n" + e);
				}

			}
		}

		System.exit(0); // Stop all processes of this system.

	}

	private static void move(ArrayList<String> replicaList, int idNextPrev) throws IOException {
		String ip;
		if (idNextPrev == idNext) {
			ip = ipNext;
		} else {
			ip = ipPrev;
		}
		for (int i = 0; i < replicaList.size(); i++) {
			// copy to prevnext node
			final String filename = replicaList.get(i);
			System.out.println(idNextPrev + ", verplaatsen wordt gestart, " + filename);
			try {
				final int socketPort = getSocketPort();
				ServerSocket servsock = new ServerSocket(socketPort);
				File myFile = new File(pathReplica + filename);
				final INode node = (INode) Naming.lookup("//" + ip + "/node");
				Thread getFileThread = new Thread() {
					public void run() {
						try {
							node.getFile(socketPort, ip(), filename, pathReplica);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
				getFileThread.start();

				Socket sock = servsock.accept();
				byte[] mybytearray = new byte[(int) myFile.length()];
				FileInputStream fis = new FileInputStream(myFile);
				BufferedInputStream bis = new BufferedInputStream(fis);
				bis.read(mybytearray, 0, mybytearray.length);
				OutputStream os = sock.getOutputStream();
				os.write(mybytearray, 0, mybytearray.length);
				while (getFileThread.isAlive())
					;
				os.flush();
				servsock.close();
				bis.close();
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// delete replica
			try {

				File file = new File(pathReplica + filename);

				if (file.delete()) {
					System.out.println(file.getName() + " is deleted!");
				} else {
					System.out.println("Delete operation is failed.");
				}

			} catch (Exception e) {
				System.out.println("Delete operation is failed, " + filename);
				e.printStackTrace();
			}
		}
	}

	public static void doubles(HashMap<String, Integer> local, HashMap<String, Integer> replica)
			throws InterruptedException, IOException {

		final File folder = new File(pathLokaal);
		HashMap<String, Integer> localNew = listLocalFiles(folder);
		ArrayList<String> localNewList = new ArrayList<String>(localNew.keySet());
		ArrayList<String> localList = new ArrayList<String>(local.keySet());
		compare(localList, localNewList);
		// System.out.println("Contents of local files: " + localList);
		// System.out.println("Contents of local new files: " + localNewList);

		local.clear();
		for (int i = 0; i < localNewList.size(); i++) {
			String var = localNewList.get(i);
			// gui.addFile(var, true); /*** GUI functie */
			local.put(var, hasher(var));
		}
		// System.out.println("dit is de nieuwe local: " + local);

		final File folder1 = new File(pathReplica);
		HashMap<String, Integer> replicaNew = listLocalFiles(folder1);
		ArrayList<String> replicaNewList = new ArrayList<String>(replicaNew.keySet());
		@SuppressWarnings("unused")
		ArrayList<String> replicaList = new ArrayList<String>(replica.keySet());
		// compare(replicaList, replicaNewList);
		// System.out.println("Contents of replica files: " + replicaList);
		// System.out.println("Contents of replica new files: " +
		// replicaNewList);

		replica.clear();
		for (int i = 0; i < replicaNewList.size(); i++) {
			String var = replicaNewList.get(i);

			replica.put(var, hasher(var));
		}
		// System.out.println("dit is de nieuwe replica: ");
		// System.out.println(replica);
	}

	/*************************
	 * Process Attributes
	 *************************/

	private void getNameServerIp() throws IOException { // Look for the ip of
														// the name server
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

	private boolean addToSystem() throws IOException { // Add this node to the
														// system.

		try {
			INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
			if (lns.add(hasher(name), ip())) { // add to server by the hash of
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

	public boolean setNextNode() throws IOException { // when this function is
														// called, this node
		// would contact his upper neighbor.
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

	public boolean setPrevNode() throws IOException {
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

		} catch (MalformedURLException e) {
			System.out.println("Node: setPrevNode (): MalformedURLException: (to node)\n" + e);
			return false;
		} catch (RemoteException e) {
			System.out.println("Node: setPrevNode (): RemoteException: (to node)\n" + e);
			return false;
		} catch (NotBoundException e) {
			System.out.println("Node: setPrevNode (): NotBoundException: (to node)\n" + e);
			return false;
		}

		return true;
	}

	public static HashMap<String, Integer> listLocalFiles(final File folder) {
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

	public static void compare(ArrayList<String> al1, ArrayList<String> al2) throws IOException {

		// Check copy to node
		ArrayList<Integer> al3 = new ArrayList<Integer>();
		for (String temp1 : al2)
			al3.add(al1.contains(temp1) ? 1 : 0);
		// System.out.println("check to copy: " + al3);

		// Check delete file
		ArrayList<Integer> al4 = new ArrayList<Integer>();
		for (String temp2 : al1)
			al4.add(al2.contains(temp2) ? 1 : 0);
		// System.out.println("check to delete: " + al4);

		for (int i = 0; i < al3.size(); i++) {
			int bool = (al3.get(i));
			if (bool == 0) {
				String toevoegen = al2.get(i);
				copyToNode(toevoegen);
				System.out.println("Toevoegen: " + toevoegen + "\tHash: " + hasher(toevoegen));
			}
		}
		for (int ii = 0; ii < al4.size(); ii++) {
			int bool1 = (al4.get(ii));

			if (bool1 == 0) {
				String verwijder = al1.get(ii);
				System.out.println("Verwijder: " + verwijder + "\tHash: " + hasher(verwijder));
				String ipfilenode = "";
				try {
					INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
					int idnode = lns.getNode(verwijder);
					/*
					 * if (idnode == hasher(name)) { idnode = idNext; }
					 */
					ipfilenode = lns.lookUp(idnode);
					System.out.println(ipfilenode + ", verwijderen wordt gestart, " + verwijder);
				} catch (MalformedURLException e) {
					System.out.println("Node.deletefile (): MalformedURLException\n\n" + e);
				} catch (RemoteException e) {
					System.out.println("Node.deletefile (): RemoteException\n\n" + e);
				} catch (NotBoundException e) {
					System.out.println("Node.deletefile (): NotBoundException\n\n" + e);
				}
				try {
					INode node = (INode) Naming.lookup("//" + ipfilenode + "/node");
					try {
						node.deletefile(verwijder, pathReplica, false);
						// gui.deleteFile(verwijder);
					} catch (IOException e) {

						e.printStackTrace();
					}

				} catch (MalformedURLException | RemoteException | NotBoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*************************
	 * RMI methods
	 *************************/

	public void changeNextNode(int id, String ip) { // Remote function to change
													// the next node of this
													// node.
		idNext = id;
		ipNext = ip;
		System.out.println("Node message: next node is changed: id: " + id + " ip: " + ip);
		// Replica over volgende node uitdelen
		// if (idPrev > idOwn) {
		try {
			VerplaatsenNextPrevNode(idNext);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// }
	}

	public void changePrevNode(int id, String ip) { // Remote function to change
													// the previous node of this
													// node.
		idPrev = id;
		ipPrev = ip;
		System.out.println("Node message: prev node is changed: id: " + id + " ip: " + ip);
		// distribute replicas on the previous neighbors.

		try {
			VerplaatsenNextPrevNode(idPrev);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void VerplaatsenNextPrevNode(int idNextPrev) throws IOException {
		final File folder1 = new File(pathReplica);
		HashMap<String, Integer> replica = listLocalFiles(folder1);
		ArrayList<String> replicaNames = new ArrayList<String>(replica.keySet());
		ArrayList<String> replicaMoves = new ArrayList<String>();
		for (int i = 0; i < replicaNames.size(); i++) {

			if (lookupFile(replicaNames.get(i)) == idNextPrev) {
				replicaMoves.add(replicaNames.get(i));
				System.out
						.println("Te Verplaatsen: " + replicaNames.get(i) + "\t Hash: " + hasher(replicaNames.get(i)));
			} else {
				System.out.println(
						"Blijft op positie : " + replicaNames.get(i) + "\t Hash: " + hasher(replicaNames.get(i)));
			}

		}
		if (replicaMoves.size() != 0) {
			try {
				move(replicaMoves, idNextPrev);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void getFile(int portNr, String ip, String filename, String path) throws IOException {
		// Called when another node has to send a replica of a file to this
		// node.
		try {
			Socket sock = new Socket(ip, portNr);
			byte[] mybytearray = new byte[6022386];
			InputStream is = sock.getInputStream();
			FileOutputStream fos = new FileOutputStream(path + filename);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			int current = 0;
			int bytesRead = is.read(mybytearray, 0, mybytearray.length);
			current = bytesRead;
			while (current < is.available()) {
				bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
				if (bytesRead >= 0)
					current += bytesRead;
			}
			bos.write(mybytearray, 0, current);
			bos.flush();

			fos.close();
			sock.close();
			bos.close();
		} catch (IOException e) {
			System.out.println("IOException in getFile():\n" + e);
		}
	}

	public void deletefile(String filename, String pathName, boolean force) throws RemoteException {
		if (fileagent.existLocal(idOwn, filename) == false || force==true) {
			try {
				File file = new File( pathName+ filename);
				filesSystemNode.remove(filename);
				if (file.delete()) {
					// System.out.println(file.getName() + " is deleted!");
				} else {
					System.out.println("Delete operation is failed:" + file.getName());
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			System.out.println("File exist somewhere else.");
		}
	}

	/*************************
	 * private methods
	 *************************/

	private static int hasher(String hashName) { // Make a hash from the given
													// String
		return Math.abs(hashName.hashCode() % 32768);
	}

	static String ip() { // Returns the ip of this system.
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

	private static int getSocketPort() {
		Random randomGenerator = new Random();
		return randomGenerator.nextInt(3000) + 1026;
	}

	private void deleteReplica() throws IOException {
		final File folder1 = new File(pathReplica);
		HashMap<String, Integer> replicaNew = listLocalFiles(folder1);
		ArrayList<String> replicaNewList = new ArrayList<String>(replicaNew.keySet());

		replica.clear();
		for (int i = 0; i < replicaNewList.size(); i++) {
			try {

				File file = new File(pathReplica + replicaNewList.get(i));

				if (file.delete()) {
					// System.out.println(file.getName() + " is deleted!");
				} else {
					System.out.println("Delete operation is failed, " + replicaNewList);
				}

			} catch (Exception e) {
				System.out.println("Delete operation is failed, " + replicaNewList);
				e.printStackTrace();
			}
		}
	}

	public static int lookupFile(String filename) throws IOException {
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
		return 0;
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

	public static void copyToNode(final String filename) throws IOException {
		String ipfilenode = "";
		try {
			INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
			int idnode = lns.getNode(filename);
			/*
			 * if (idnode == hasher(name)) { idnode = idNext; }
			 */
			ipfilenode = lns.lookUp(idnode);
			System.out.println("ID copy: " + idnode);
		} catch (MalformedURLException e) {
			System.out.println("Node.getNode (): MalformedURLException\n\n" + e);
		} catch (RemoteException e) {
			System.out.println("Node.getNode (): RemoteException\n\n" + e);
		} catch (NotBoundException e) {
			System.out.println("Node.getNode (): NotBoundException\n\n" + e);
		}

		try {
			final int socketPort = getSocketPort();
			ServerSocket servsock = new ServerSocket(socketPort);
			File myFile = new File(pathLokaal + filename);
			final INode node = (INode) Naming.lookup("//" + ipfilenode + "/node");

			Thread getFileThread = new Thread() {
				public void run() {
					try {
						node.getFile(socketPort, ip(), filename, pathReplica);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			getFileThread.start();

			Socket sock = servsock.accept();
			byte[] mybytearray = new byte[(int) myFile.length()];
			FileInputStream fis = new FileInputStream(myFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(mybytearray, 0, mybytearray.length);
			OutputStream os = sock.getOutputStream();
			os.write(mybytearray, 0, mybytearray.length);
			while (getFileThread.isAlive())
				;
			os.flush();
			servsock.close();
			bis.close();
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/***************
	 * GUI methodes
	 * 
	 * @throws IOException
	 ************/
	public static void open(String filename) throws IOException { // The GUI
																	// will call
																	// this
																	// method
		// when the user want to open a
		// file that isn't represented
		// in the local map.
		System.out.println("Node void open()");
		// Lijst alles nodes waar bestand voorkomt in Local
		// Vraag lookup aan nameserver
		// Kopieer bestand naar lokaal
		try {
			INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
			int idnode = lns.getNode(filename);
			String ipfilenode = lns.lookUp(idnode);
			final INode node = (INode) Naming.lookup("//" + ipfilenode + "/node");
			// Roep RMI op bij waar file in replica zit
			node.copyReplicaToLocal(ip(), filename);
			gui.changeLocality(filename, true);
			openLocal(filename);
		} catch (MalformedURLException e) {
			System.out.println("Node.getNode (): MalformedURLException\n\n" + e);
		} catch (RemoteException e) {
			System.out.println("Node.getNode (): RemoteException\n\n" + e);
		} catch (NotBoundException e) {
			System.out.println("Node.getNode (): NotBoundException\n\n" + e);
		}
	}

	public static void openLocal(String fileName) throws IOException { // The
																		// GUI
																		// will
																		// call
																		// this
		// method when the user want
		// to open a local file.
		File file = new File(pathLokaal + fileName);
		if (file.exists()) {
			try {
				@SuppressWarnings("unused")
				Process p = Runtime.getRuntime().exec("rundll32 url.dll, FileProtocolHandler " + pathLokaal + fileName);
			} catch (IOException e) {
				System.out.println("IO exception in Node: void openLocel():\nFailed to run " + fileName + ".\n" + e);
			}
		} else { // When the given file doesn't exist in the local map.
			gui.changeLocality(fileName, false);
			open(fileName); // Call the none local version of this function.
		}
	}

	public static void delete(String fileName) { // The GUI will call this
													// method when the user want
													// to remove a file out of
													// the system.
		//Remove all local files
		ArrayList<Integer> localnodes = new ArrayList<Integer>(fileagent.getLocalLocations(fileName));
		for (int j = 0; j < localnodes.size(); j++) {
			try {
				INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
				String ipfilenode = lns.lookUp(localnodes.get(j));
				final INode node = (INode) Naming.lookup("//" + ipfilenode + "/node");
				// Verwijder alle lokale bestanden
				node.deletefile(fileName, pathLokaal, true);
				gui.changeLocality(fileName, false);
				} catch (MalformedURLException e) {
				System.out.println("Node.getNode (): MalformedURLException\n\n" + e);
			} catch (RemoteException e) {
				System.out.println("Node.getNode (): RemoteException\n\n" + e);
			} catch (NotBoundException e) {
				System.out.println("Node.getNode (): NotBoundException\n\n" + e);
			}
		}
		//Remove replica
		try {
			INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
			int idnode = lns.getNode(fileName);
			String ipfilenode = lns.lookUp(idnode);
			final INode node = (INode) Naming.lookup("//" + ipfilenode + "/node");
			node.deletefile( fileName, pathReplica, true);
			gui.changeLocality(fileName, false);
			} catch (MalformedURLException e) {
			System.out.println("Node.getNode (): MalformedURLException\n\n" + e);
		} catch (RemoteException e) {
			System.out.println("Node.getNode (): RemoteException\n\n" + e);
		} catch (NotBoundException e) {
			System.out.println("Node.getNode (): NotBoundException\n\n" + e);
		}
	}

	public static void deleteLocal(String fileName) { // This method will be
														// called from the gui
														// to delete a local
														// file.
		File file = new File(pathLokaal + fileName);
		if (!file.exists()) { // if the given file doesn't exist in the local
								// folder.
			gui.changeLocality(fileName, false); // Remove the button "delete
													// local" from this file.
			System.out.println(fileName + " is not a local file."); // -------Report
		} else {
			if (file.delete()) {
				gui.changeLocality(fileName, false); // Remove delete local
														// button when file is
														// successfully removed.
			} else {
				System.out.println("Failed to delete " + fileName + " local."); // -------Report
			}
		}
	}

	/***************
	 * Agent methodes
	 ************/
	public void refreshAgent(HashMap<Integer, HashMap<String, Integer>> localList,
			HashMap<Integer, HashMap<String, Integer>> replicaList) throws RemoteException {
		try {
			fileagent.refreshList(localList, replicaList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void copyLocalToReplica(String ipReplica, String filename) throws RemoteException {
		try {
			final int socketPort = getSocketPort();
			ServerSocket servsock = new ServerSocket(socketPort);
			File myFile = new File(pathLokaal + filename);
			final INode node = (INode) Naming.lookup("//" + ipReplica + "/node");

			Thread getFileThread = new Thread() {
				public void run() {
					try {
						node.getFile(socketPort, ip(), filename, pathReplica);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			getFileThread.start();

			Socket sock = servsock.accept();
			byte[] mybytearray = new byte[(int) myFile.length()];
			FileInputStream fis = new FileInputStream(myFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(mybytearray, 0, mybytearray.length);
			OutputStream os = sock.getOutputStream();
			os.write(mybytearray, 0, mybytearray.length);
			while (getFileThread.isAlive())
				;
			os.flush();
			servsock.close();
			bis.close();
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void copyReplicaToLocal(String ipLocal, String filename) throws RemoteException {
		try {
			final int socketPort = getSocketPort();
			ServerSocket servsock = new ServerSocket(socketPort);
			File myFile = new File(pathReplica + filename);
			final INode node = (INode) Naming.lookup("//" + ipLocal + "/node");

			Thread getFileThread = new Thread() {
				public void run() {
					try {
						node.getFile(socketPort, ip(), filename, pathLokaal);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			getFileThread.start();

			Socket sock = servsock.accept();
			byte[] mybytearray = new byte[(int) myFile.length()];
			FileInputStream fis = new FileInputStream(myFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(mybytearray, 0, mybytearray.length);
			OutputStream os = sock.getOutputStream();
			os.write(mybytearray, 0, mybytearray.length);
			while (getFileThread.isAlive())
				;
			os.flush();
			servsock.close();
			bis.close();
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}