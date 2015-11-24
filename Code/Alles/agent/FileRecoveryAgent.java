package agent;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;

import NameServer.INameServer;

import node.INode;
import node.Node;

public class FileRecoveryAgent {

	public int idFail;
	public int idStart;
	
	public FileRecoveryAgent(int idFail, int idStart){
		this.idFail = idFail;
		this.idStart = idStart;
	}
	
	public void run() {
		Iterator<HashMap.Entry<String, Integer>> entriesNode = Node.local.entrySet().iterator();
		int counter = 0;
		int isStarted = 0;
		while (entriesNode.hasNext()) {
			HashMap.Entry<String, Integer> entryNode = entriesNode.next();
			if ((entryNode.getValue() < idFail)&&(entryNode.getValue() > idStart)){ //check when hash is equal to the failed node
				//idStart moet nog aangepast worden!!!
				try {
					INameServer lns = (INameServer) Naming.lookup("//" + Node.lnsIp + "/LNS");
					int nextNodeId = lns.getNext(idFail); //ask next node of the failed node
					INode nextFailNode = (INode) Naming.lookup("//" + nextNodeId + "/node");
					Iterator<HashMap.Entry<String, Integer>> entriesNextFailNode = Node.local.entrySet().iterator();
					counter = 0;
					while (entriesNextFailNode.hasNext()) {
						HashMap.Entry<String, Integer> entryNextFailNode = entriesNextFailNode.next();
						if( entryNextFailNode.getValue() == entryNode.getValue()){
							counter++;
						}
					}
					if(counter == 0){
						entryNode.setValue(nextNodeId);
					}
					
					if((entryNode.getValue() == idStart)&&(isStarted == 0)){
						isStarted = 1;
						break;
					}
				} catch (MalformedURLException | RemoteException | NotBoundException e) {	
					System.out.println("Agent failed to adapt files " + e);
				}

			}
			
		}
	}
}
