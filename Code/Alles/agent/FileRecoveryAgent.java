package agent;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;

import NameServer.INameServer;

import node.INode;
import node.Node;

public class FileRecoveryAgent extends Agent implements Runnable, Serializable {

	private static final long serialVersionUID = 1L;
	private int idFail;
	private int idStart;
	private int currentid;
	private int nextFailid;
	private int nextId;
	private boolean flag;
	
	public FileRecoveryAgent(int idFail, int idStart){
		super();
		this.idFail = idFail;
		this.idStart = idStart;
		this.currentid = idStart;
		this.nextId = idStart;
		this.nextFailid = idFail;
		this.flag = true;
	}
	
	public void run() {
		while(flag){ //run until you are back at the start node
			INameServer lns;
			String ipNode;
			int setup = 0; 
			INode currentNode;
			int counter = 0;
			int prevFailNode = 0;
			
			if(currentid != idFail){ //Do the following only when the current node is not the failed node
				try { //Setup of the current node
					lns = (INameServer) Naming.lookup("//" + Node.lnsIp + "/LNS");
					ipNode = lns.lookUp(currentid);
					currentNode = (INode) Naming.lookup("//" + ipNode + "/node");
					prevFailNode = lns.getPrev(idFail);
					nextId = lns.getNext(currentid);
				} catch (MalformedURLException| RemoteException| NotBoundException e1) {
					e1.printStackTrace();
				}
				 
				Iterator<HashMap.Entry<String, Integer>> entriesNode = Node.local.entrySet().iterator();
				
				while (entriesNode.hasNext()) { //loop over the local list of the node
					HashMap.Entry<String, Integer> entryNode = entriesNode.next();
					if ((entryNode.getValue() < idFail)&&(entryNode.getValue() >= currentid) && (entryNode.getValue() >= prevFailNode)){ //check when hash is equal to the failed node
						try {
							lns = (INameServer) Naming.lookup("//" + Node.lnsIp + "/LNS");
							nextFailid = lns.getNext(idFail); //ask next node of the failed node
							String ipNextNode = lns.lookUp(nextFailid); //ask his ip address
							INode nextNode = (INode) Naming.lookup("//" + ipNextNode + "/node"); //go the next node of the failed node
							Iterator<HashMap.Entry<String, Integer>> entriesNextNode = Node.local.entrySet().iterator();
							counter = 0;
							while (entriesNextNode.hasNext()) { //loop over the next node of the failed node
								HashMap.Entry<String, Integer> entryNextNode = entriesNextNode.next();
								if( entryNextNode.getKey() == entryNode.getKey()){ //check if this node already has the file
									counter++;
								}
							}
							if(counter == 0){
								try {//NAKIJKEN OF HET NAAR DE VOLGENDE NODE WORDT GEKOPIEERD!!!!
									lns = (INameServer) Naming.lookup("//" + Node.lnsIp + "/LNS");
									String ipFail = lns.lookUp(idFail);
									INode nextidNode = (INode) Naming.lookup("//" + ipFail + "/node");
									Node.copyToNode(entryNode.getKey());
								} catch (IOException e) {
									System.out.println("Failed to copy file: " + e);
								}
								FileListAgent.files_in_system.put(entryNode.getKey(), true); //File must be copied so the value on true
							}
							else{ 
								entryNode.setValue(nextFailid); //change the place where the file is now.
							}
							currentid = nextId;
							if((currentid == idStart)&&(setup != 0)){ //function must end when it's back at the first node
								flag = false;		
							}	
						} catch (MalformedURLException | RemoteException | NotBoundException e) {	
							System.out.println("Agent failed to adapt files " + e);
						}
					}
				}
				setup++;
			}
			else
				currentid = nextFailid;
		}
	}
}
