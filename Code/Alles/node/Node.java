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
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.Random;

import agent.Agent;
import agent.FileListAgent;
import agent.FileRecoveryAgent;

public class Node extends UnicastRemoteObject implements INode {
	private static final long serialVersionUID = 1L;
	private static String name;
	public static String lnsIp;

	public static int idNext;
	public static String ipNext;
	public int idPrev;
	public String ipPrev;
	public int idOwn;

	private static String pathLokaal = "c://lokaal/";
	private static String pathReplica = "c://replica/";
	public static HashMap<String, Integer> local = new HashMap<String, Integer>();
	public static HashMap<String, Integer> replica = new HashMap<String, Integer>();
	public static HashMap<String, Boolean> filesSystemNode = new HashMap<String,Boolean>();
	private static Agent ag = new Agent(){;
	public void run(){}};
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
		
		gui = new NodeGui();
		gui.setVisible(true);
	}

	public void shutdown() throws IOException { // Call to shut down this node.
		/*
		 * Verwijdern van nameserver. Check Verander de naaste nodes check
		 * replica, deel uit aan bovenste. Check lokaal, vraag op aan agent of
		 * bestand ergens ander ook bestaat in lokaal --> wis alle replicas op
		 * andere nodes.
		 * 
		 */
		if (idNext == idOwn ||idNext == 0) {
			// verwijder van nameserver
			try {
				INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
				lns.delete(hasher(name));
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				System.out.println("failed to connect the server");
				failure(ag);
			}
			return;
		}
		// Verander de naaste nodes check replica, deel uit aan bovenste.
		if (ipNext != null && ipNext != ip()) {
			final File folder1 = new File(pathReplica);
			HashMap<String, Integer> replicaNew = listLocalFiles(folder1);
			ArrayList<String> replicaNewList = new ArrayList<String>(replicaNew.keySet());
			ArrayList<String> replicaList = new ArrayList<String>(replica.keySet());
			compare(replicaList, replicaNewList);
			System.out.println("Contents of replica: " + replicaList);
			move(replicaList, idPrev);

		}
		try {
			INode nextNode = (INode) Naming.lookup("//" + ipNext + "/node");
			nextNode.changePrevNode(idPrev, ipPrev);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			System.out.println("failed to connect to nextNode");
			failure(ag);
		}

		try {
			INode prevNode = (INode) Naming.lookup("//" + ipPrev + "/node");
			prevNode.changeNextNode(idNext, ipNext);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			System.out.println("failed to connect to prevNode");
			failure(ag);
		}
		// verwijder van nameserver
		try {
			INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
			lns.delete(hasher(name));
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			System.out.println("failed to connect the server");
			failure(ag);
		}

		// Lokaal nakijken,
		// vraag aan agent op of het nog ergens anders in lokaal bestaat.
		// Wis replica's indien niet lokaal op andere nodes
		final File folder1 = new File(pathLokaal);
		HashMap<String, Integer> lokaal = listLocalFiles(folder1);
		ArrayList<String> lokaalList = new ArrayList<String>(lokaal.keySet());
		System.out.println("Contents of lokaal: " + lokaalList);

		for (int i = 0; i < lokaalList.size(); i++) {
			String filename = lokaalList.get(i);
			if (false == false) {// Bestaatlokaalergensanders(idOwn, filename)

				System.out.println("Bestaat nergens anders " + filename);
				String ipfilenode;
				try {
					INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
					int idnode = lns.getNode(filename);
					/*
					 * if (idnode == hasher(name)) { idnode = idPrev; }
					 */
					ipfilenode = lns.lookUp(idnode);
					System.out.println("ID delete: " + idnode);
					try {
						INode node = (INode) Naming.lookup("//" + ipfilenode + "/node");
						try {
							node.deletefile(filename);
						} catch (IOException e) {
							failure(ag);
							e.printStackTrace();
						}

					} catch (MalformedURLException | RemoteException | NotBoundException e) {
						e.printStackTrace();
					}
				} catch (MalformedURLException e) {
					failure(ag);
					System.out.println("Node.getNode (): MalformedURLException\n\n" + e);
				} catch (RemoteException e) {
					failure(ag);
					System.out.println("Node.getNode (): RemoteException\n\n" + e);
				} catch (NotBoundException e) {
					failure(ag);
					System.out.println("Node.getNode (): NotBoundException\n\n" + e);
				}

			}
		}

	}

	private void move(ArrayList<String> replicaList, int idNextPrev)throws IOException {
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
							node.getFile(socketPort, ip(), filename);
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
				failure(ag);
				e.printStackTrace();
			} catch (IOException e) {
				failure(ag);
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
				failure(ag);
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
			gui.addFile (var, true);	/*** GUI functie */
			local.put(var, hasher(var));
		}
		// System.out.println("dit is de nieuwe local: " + local);

		final File folder1 = new File(pathReplica);
		HashMap<String, Integer> replicaNew = listLocalFiles(folder1);
		ArrayList<String> replicaNewList = new ArrayList<String>(replicaNew.keySet());
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

	private void getNameServerIp()throws IOException { // Look for the ip of the name server
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
			failure(ag);
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
			failure(ag);
			System.out.println("UDP: SocketException: " + e.getMessage());
		} catch (IOException e) {
			failure(ag);
			System.out.println("UDP: IOException: " + e.getMessage());
		}
		aSocket.close();
	}

	private boolean addToSystem() throws IOException{ // Add this node to the system.

		try {
			INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
			if (lns.add(hasher(name), ip())) { // add to server by de hash of
												// the node's name and his ip.
				return true;
			}
		} catch (MalformedURLException e) {
			failure(ag);
			System.out.println("Node.addToSystem (): MalformedURLException\n\n" + e);
		} catch (RemoteException e) {
			failure(ag);
			System.out.println("Node.addToSystem (): RemoteException\n\n" + e);
		} catch (NotBoundException e) {
			failure(ag);
			System.out.println("Node.addToSystem (): NotBoundException\n\n" + e);
		}
		System.out.println("Node.addToSystem ():\tfailled to add id");
		return false;
	}

	public boolean setNextNode() throws IOException{ // when this function is called, this node
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
			failure(ag);
			System.out.println("Node: setNextNode (): MalformedURLException:\n" + e);
			return false;
		} catch (RemoteException e) {
			failure(ag);
			System.out.println("Node: setNextNode (): RemoteException:\n" + e);
			return false;
		} catch (NotBoundException e) {
			failure(ag);
			System.out.println("Node: setNextNode (): NotBoundException:\n" + e);
			return false;
		}

		try {
			INode nextNode = (INode) Naming.lookup("//" + ipNext + "/node");
			nextNode.changePrevNode(hasher(name), ip());
			return true;
		} catch (MalformedURLException e) {
			failure(ag);
			System.out.println("Node: setNextNode (): MalformedURLException: (to node)\n" + e);
		} catch (RemoteException e) {
			failure(ag);
			System.out.println("Node: setNextNode (): RemoteException: (to node)\n" + e);
		} catch (NotBoundException e) {
			failure(ag);
			System.out.println("Node: setNextNode (): NotBoundException: (to node)\n" + e);
		}
		return false;
	}

	public boolean setPrevNode() throws IOException{
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
			failure(ag);
			System.out.println("Node: setPrevNode (): MalformedURLException:\n" + e);
			return false;
		} catch (RemoteException e) {
			failure(ag);
			System.out.println("Node: setPrevNode (): RemoteException:\n" + e);
			return false;
		} catch (NotBoundException e) {
			failure(ag);
			System.out.println("Node: setPrevNode (): NotBoundException:\n" + e);
			return false;
		}

		try {
			INode prevNode = (INode) Naming.lookup("//" + ipPrev + "/node");
			prevNode.changeNextNode(hasher(name), ip());

		} catch (MalformedURLException e) {
			failure(ag);
			System.out.println("Node: setPrevNode (): MalformedURLException: (to node)\n" + e);
			return false;
		} catch (RemoteException e) {
			failure(ag);
			System.out.println("Node: setPrevNode (): RemoteException: (to node)\n" + e);
			return false;
		} catch (NotBoundException e) {
			failure(ag);
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
						node.deletefile(verwijder);
						gui.deleteFile(verwijder);
					} catch (IOException e) {

						e.printStackTrace();
					}

				} catch (MalformedURLException | RemoteException | NotBoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void failure(Agent ag) throws RemoteException
	{
		ag = new FileRecoveryAgent(idOwn,idNext);
		ag.run();
		System.out.println("Starting failure procedure...");
		
	//	agent.FileRecoveryAgent fileAgent = new FileRecoveryAgent(idOwn, idNext);
	//	RMIObject r = new RMIObject(ag);
		//fileAgent.run();
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
		// Replica over vorige node uitdelen

		try {
			VerplaatsenNextPrevNode(idPrev);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void VerplaatsenNextPrevNode(int idNextPrev) throws IOException{
		final File folder1 = new File(pathReplica);
		HashMap<String, Integer> replica = listLocalFiles(folder1);
		ArrayList<String> replicaNames = new ArrayList<String>(replica.keySet());
		ArrayList<String> replicaMoves = new ArrayList<String>();
		for (int i = 0; i < replicaNames.size(); i++) {
			/*
			 * if (hasher(replicaNames.get(i)) >= idNextPrev&&
			 * hasher(replicaNames.get(i))<idOwn) {
			 * replicaMoves.add(replicaNames.get(i)); System.out.println(
			 * "Te Verplaatsen: " + replicaNames.get(i) +"\t Hash: "+
			 * hasher(replicaNames.get(i))); }else{ System.out.println(
			 * "Blijft op positie : " + replicaNames.get(i) +"\t Hash: " +
			 * hasher(replicaNames.get(i))); }
			 */
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
				failure(ag);
				e.printStackTrace();
			}
		}
	}

	public void getFile(int portNr, String ip, String filename) throws IOException{	// Called when an other node has to send a replica of a file to this node.
		try {
			Socket sock = new Socket(ip, portNr);
			byte[] mybytearray = new byte[6022386];
			InputStream is = sock.getInputStream();
			FileOutputStream fos = new FileOutputStream(pathReplica + filename);
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
			failure(ag);
			System.out.println("IOException in getFile():\n" + e);
		}
	}

	public void deletefile(String filename) throws RemoteException {
		try {

			File file = new File(pathReplica + filename);
			filesSystemNode.remove(filename);
			agent.FileListAgent.files_in_system.remove(filename);
			if (file.delete()) {
				// System.out.println(file.getName() + " is deleted!");
			} else {
				System.out.println("Delete operation is failed:" + file.getName());
			}

		} catch (Exception e) {
			failure(ag);
			e.printStackTrace();
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
				failure(ag);
				System.out.println("Delete operation is failed, " + replicaNewList);
				e.printStackTrace();
			}
		}
	}
	
	/*private static void deleteLocal () {
		final File folder1 = new File(pathLokaal);
		HashMap<String, Integer> LokaalNew = listLocalFiles(folder1);
		ArrayList<String> LokaalNewList = new ArrayList<String>(LokaalNew.keySet());

		replica.clear();
		for (int i = 0; i < LokaalNewList.size(); i++) {
			try {

				File file = new File(pathLokaal + LokaalNewList.get(i));

				if (file.delete()) {
					// System.out.println(file.getName() + " is deleted!");
				} else {
					System.out.println("Delete operation is failed, " + LokaalNewList);
				}

			} catch (Exception e) {
				System.out.println("Delete operation is failed, " + LokaalNewList);
				e.printStackTrace();
			}
		}
	}*/
	
	public int lookupFile(String filename) throws IOException {
		try {
			INameServer lns = (INameServer) Naming.lookup("//" + lnsIp + "/LNS");
			return lns.getNode(filename);

		} catch (MalformedURLException e) {
			failure(ag);
			System.out.println("Node.lookupFile (): MalformedURLException\n\n" + e);
		} catch (RemoteException e) {
			failure(ag);
			System.out.println("Node.lookupFile (): RemoteException\n\n" + e);
		} catch (NotBoundException e) {
			failure(ag);
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
						node.getFile(socketPort, ip(), filename);
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

/*	public void run() throws InterruptedException, IOException {
		boolean flag = true;
		System.out.println("Thread control files started...");
		try {
			Thread.sleep(5000); // Checks every 5s
		} catch (Exception e) {
			System.out.println("Thread is ended! The error is " + e.getMessage());
		}
		if (!flag) // Only when the flag is false, the thread ends
			return;
		else
			run();
		final File folder = new File(pathLokaal);
		*
		 * HashMap<String, Integer> local = listLocalFiles(folder); final File
		 * folder1 = new File(pathReplica); HashMap<String, Integer> replica =
		 * listReplicaFiles(folder1);
		 *
		System.out.println("Contents of local files: " + local);
		System.out.println("Contents of replica files: " + replica);
		doubles(local, replica);
	}
*/
	
	/***************
	 * GUI methodes
	 * ************/
	public static void open (String fileName) {
		System.out.println("Node void open()");
	}
	
	public static void delete (String fileName) {
		System.out.println("Node void delete()");
	}
	
	public static void deleteLocal (String fileName) {
		File file = new File(pathLokaal + fileName);
		if (file.exists()) {
			gui.changeLocality(fileName, false);
		} else {
			if (file.delete()) {
				gui.changeLocality(fileName, false);
			} else {
				
			}
		}
	}
}