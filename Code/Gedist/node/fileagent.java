package node;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import NameServer.INameServer;

public class fileagent {

	public static HashMap<Integer, HashMap<String, Integer>> localList = new HashMap<Integer, HashMap<String, Integer>>(); // nodeId,
	// filelist			//id node		//Filename, lock
	// file(name,
	// writeaccess)
	public static HashMap<Integer, HashMap<String, Integer>> replicaList = new HashMap<Integer, HashMap<String, Integer>>(); // nodeId,
	// filelist			//id node		//Filename, lock
	// file(name,
	// writeaccess)

	public static void sendList() {
		// Make List and Put List into the big Hashmap
		HashMap<String, Integer> templistlocal = new HashMap<String, Integer>();
		ArrayList<String> localNames = new ArrayList<String>(Node.local.keySet());
		for (int i = 0; i < localNames.size(); i++) {
			templistlocal.put(localNames.get(i), 0);
		}
		localList.put(Node.idOwn, templistlocal);

		HashMap<String, Integer> templistreplica = new HashMap<String, Integer>();
		ArrayList<String> replicaNames = new ArrayList<String>(Node.replica.keySet());
		for (int i = 0; i < replicaNames.size(); i++) {
			templistreplica.put(replicaNames.get(i), 0);
		}
		replicaList.put(Node.idOwn, templistreplica);

		// Send Hashmap to NextNode and catch
		if (Node.ipNext != null) {
			try {
				INode nextnode = (INode) Naming.lookup("//" + Node.ipNext + "/node");
				nextnode.refreshAgent(localList, replicaList);
			} catch (MalformedURLException e) {
				failure();
				// System.out.println("Agent.refreshAgent ():
				// MalformedURLException\n\n" + e);
			} catch (RemoteException e) {
				failure();
				// System.out.println("Agent.refreshAgent ():
				// RemoteException\n\n" + e);
			} catch (NotBoundException e) {
				failure();
				// System.out.println("Agent.refreshAgent ():
				// NotBoundException\n\n" + e);
			}
		}
	}

	public static boolean existLocal(int id, String filename) {
		// Exist also in the local foler of an other node

		ArrayList<Integer> templistnode = new ArrayList<Integer>(localList.keySet());
		for (int i = 0; i < templistnode.size(); i++) {
			if (templistnode.get(i) != id) {
				ArrayList<String> templistfiles = new ArrayList<String>(localList.get(templistnode.get(i)).keySet());
				for (int j = 0; j < templistfiles.size(); j++) {
					if (templistfiles.get(j).equals(filename)) {
						return true;
					}
				}
			}
		}
		return false;

	}

	/******
	 * RMI Methods
	 *******/
	public static void refreshList(HashMap<Integer, HashMap<String, Integer>> localListi,
			HashMap<Integer, HashMap<String, Integer>> replicaListi) throws IOException {
		// change Hashmap except own part

		HashMap<String, Integer> templocal = new HashMap<String, Integer>();
		templocal = localList.get(Node.idOwn);
		HashMap<String, Integer> tempreplica = new HashMap<String, Integer>();
		tempreplica = replicaList.get(Node.idOwn);
		localList.clear();
		replicaList.clear();
		localList.putAll(localListi);
		replicaList.putAll(replicaListi);

		// Replace own part
		localList.put(Node.idOwn, templocal);
		replicaList.put(Node.idOwn, tempreplica);
		System.out.println("Local:  " + localList);
		System.out.println("Replica:  " + replicaList);

	}

	/******
	 * Failure
	 */

	public static void failure() {
		int id = Node.idNext;
		System.out.println("Failure:  " + Node.idNext);
		// Meld failure to nameServer
		// Ask next neighbor from the nameServer
		try {
			INameServer lns = (INameServer) Naming.lookup("//" + Node.lnsIp + "/LNS");
			lns.delete(Node.idNext);
			Node.idNext = lns.getNext(Node.idOwn);
			Node.ipNext = lns.lookUp(Node.idNext);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			System.out.println("failed to connect the server");
		}
		// Move files from replica to next when necessarily.
		try {
			Node.VerplaatsenNextPrevNode(Node.idNext);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (Node.ipNext != null) {
			// Change prevId of next neighbor.
			try {
				INode nextnode = (INode) Naming.lookup("//" + Node.ipNext + "/node");
				nextnode.changePrevNode(Node.idOwn, Node.ip());
			} catch (MalformedURLException e) {
				System.out.println("Agent.failure (): MalformedURLException\n\n" + e);
			} catch (RemoteException e) {
				System.out.println("Agent.failure (): RemoteException\n\n" + e);
			} catch (NotBoundException e) {
				System.out.println("Agent.failure (): NotBoundException\n\n" + e);
			}

			// Bijkijk bestanden dode buur
			HashMap<String, Integer> templist = new HashMap<String, Integer>();
			templist.putAll(replicaList.get(id));
			String filename;
			ArrayList<String> templistfile = new ArrayList<String>(templist.keySet());
			for (int i = 0; i < templistfile.size(); i++) {
				// When Replica's also exist in the local folder somwhere else => 
				// Ask new address and the location where we can find it in a local folder
				// Copie files from that local folder to this node's replica folder

				filename = templistfile.get(i);
				if (existLocal(id, filename) == true) {
					
					// Ask location where we can find it in a local folder
					ArrayList<Integer> templistnode = new ArrayList<Integer>(localList.keySet());
					int idTempLocalFile = 0;
					for (int j = 0; j < templistnode.size(); j++) {
						if (templistnode.get(j) != id) {
							ArrayList<String> templistfiles = new ArrayList<String>(
									localList.get(templistnode.get(j)).keySet());
							for (int m = 0; m < templistfiles.size(); m++) {
								if (templistfiles.get(m).equals(filename)) {
									idTempLocalFile = templistnode.get(j);
									break;
								}
							}
						}
					}
					// Ask ip from the of the local - and replica copy.
					try {
						INameServer lns = (INameServer) Naming.lookup("//" + Node.lnsIp + "/LNS");
						int idTempReplicaFile = lns.getNode(filename);
						String ipTempReplicaFile = lns.lookUp(idTempReplicaFile);
						String ipTempLocalFile = lns.lookUp(idTempLocalFile);
						// Copy from local to replica
						try {
							INode nodeLocal = (INode) Naming.lookup("//" + ipTempLocalFile + "/node");
							System.out.println("Recover " + filename);
							nodeLocal.copyLocalToReplica(ipTempReplicaFile, filename);
						} catch (MalformedURLException | RemoteException | NotBoundException e) {
							System.out.println("failed to connect the server");
						}
					} catch (MalformedURLException | RemoteException | NotBoundException e) {
						System.out.println("failed to connect the server");
					}

				}

			}
			
			// If their is no other local copy, the replica will be removed.
			templist.clear();
			templist.putAll(localList.get(id));
			templistfile.clear();
			templistfile =  new ArrayList<String>(templist.keySet());
			// All local files from the local folder of the neighbour
			for (int i = 0; i < templistfile.size(); i++) {
				filename = templistfile.get(i);
				if (existLocal(id, filename) == false) {
					//delete replica
					try {
						INameServer lns = (INameServer) Naming.lookup("//" + Node.lnsIp + "/LNS");
						int idTempReplicaFile = lns.getNode(filename);
						String ipTempReplicaFile = lns.lookUp(idTempReplicaFile);
						// delete replica
						try {
							INode nodeReplica = (INode) Naming.lookup("//" + ipTempReplicaFile + "/node");
							System.out.println("Delete " + filename);
							nodeReplica.deletefile(filename);
						} catch (MalformedURLException | RemoteException | NotBoundException e) {
							System.out.println("failed to connect the server");
						}
					} catch (MalformedURLException | RemoteException | NotBoundException e) {
						System.out.println("failed to connect the server");
					}
				}
			}
		}
		// Remove the Hashmap of the death neighbor
		localList.remove(id);
		replicaList.remove(id);
		// Send Hashmap to NextNode
		sendList();
	}

}
