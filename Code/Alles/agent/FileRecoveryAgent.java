package agent;

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
	private int nextid;
	private int failNextNode;
	private boolean flag = true;
	
	public FileRecoveryAgent(int idFail, int idStart){
		super();
		this.idFail = idFail;
		this.idStart = idStart;
		this.currentid = idStart; 
	}
	
	public void run() {
		while(flag){
			INameServer lns;
			String ipNode;
			int setup = 0; 
			INode currentNode;
			int counter = 0;
			
			if(currentid != idFail){
				try { //Setup current node
					lns = (INameServer) Naming.lookup("//" + Node.lnsIp + "/LNS");
					ipNode = lns.lookUp(currentid);
					currentNode = (INode) Naming.lookup("//" + ipNode + "/node");
				} catch (MalformedURLException| RemoteException| NotBoundException e1) {
					e1.printStackTrace();
				}
				 
				Iterator<HashMap.Entry<String, Integer>> entriesNode = Node.local.entrySet().iterator();
				
				while (entriesNode.hasNext()) {
					HashMap.Entry<String, Integer> entryNode = entriesNode.next();
				//	if ((entryNode.getKey() >= idFail)&&(entryNode.getKey() < failNextNode)){
					if ((entryNode.getValue() < idFail)&&(entryNode.getValue() > currentid)){ //check when hash is equal to the failed node
						try {
							lns = (INameServer) Naming.lookup("//" + Node.lnsIp + "/LNS");
							nextid = lns.getNext(currentid); //ask next node
							String ipNextNode = lns.lookUp(nextid);
							INode nextNode = (INode) Naming.lookup("//" + ipNextNode + "/node");
							Iterator<HashMap.Entry<String, Integer>> entriesNextNode = Node.local.entrySet().iterator();
							counter = 0;
							while (entriesNextNode.hasNext()) {
								HashMap.Entry<String, Integer> entryNextNode = entriesNextNode.next();
								if( entryNextNode.getValue() == entryNode.getValue()){
									counter++;
								}
							}
							if(counter == 0){
								entryNode.setValue(nextid);
							}
							currentid = nextid;
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
		}
	}
}
