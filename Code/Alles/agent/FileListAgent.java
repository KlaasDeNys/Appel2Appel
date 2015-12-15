package agent;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import NameServer.INameServer;

import node.INode;
import node.Node;

public class FileListAgent extends Agent implements Runnable, Serializable {

	private static final long serialVersionUID = 1L;
	public static HashMap<String, Boolean> files_in_system = new HashMap<String,Boolean>(); //List of all files in system
	public static HashMap<String, Boolean> localList = new HashMap<String,Boolean>(); 
	public static HashMap<String, Boolean> replicaList = new HashMap<String,Boolean>(); 
	
	public FileListAgent(){
		super();
	}
	
	public void run() {
			makeListFiles();
			checkDownload();
			checkListAgent();
			checkListNode();
			Iterator<HashMap.Entry<String, Boolean>> entriesAgent = files_in_system.entrySet().iterator();
			System.out.println(files_in_system.size());
			while(entriesAgent.hasNext()){ //Check if the file is already in the agent his list
				HashMap.Entry<String, Boolean> entryAgent = entriesAgent.next();
				System.out.println(entryAgent.getKey());
			}	
	}
	
	public boolean existLocal(int idOwn, String filename)
	{
		INameServer lns;
		int presentId;
		presentId = idOwn;
		int counter = 0;
		while ((presentId != idOwn)&&(counter  != 0)){
			try { //Setup of the current node
				lns = (INameServer) Naming.lookup("//" + Node.lnsIp + "/LNS");
				int idNextNode = lns.getNext(presentId);
				String ipNextNode = lns.lookUp(idNextNode);
				INode currentNode = (INode) Naming.lookup("//" + ipNextNode + "/node");
				counter++;
				presentId = idNextNode;
			} catch (MalformedURLException| RemoteException| NotBoundException e1) {
				e1.printStackTrace();
			}
			Iterator<HashMap.Entry<String, Integer>> entriesNode = Node.local.entrySet().iterator();
			while (entriesNode.hasNext()) { 
				HashMap.Entry<String, Integer> entryNode = entriesNode.next();
				if(entryNode.getKey() == filename){
					return true;
				}
			}
		}	
		return false;
	}
	
	public int existSomeWhereLocal(String filename) throws RemoteException
	{
		INameServer lns;
		int presentId;
		  for (Entry<String, Boolean> entry : replicaList.entrySet()) {
	            if (entry.getValue().equals(filename)) {
	            	 for (Entry<String, Boolean> entry2 : localList.entrySet()) {
	            		 if(entry.getValue().equals(filename))
	            		 {
	            			 
	            		 }
	            		 else
	            		 {	 
	            				 replicaList.remove(filename); 
	            			 
	            		 }
	            	 }
	            }
	    }
		
		return 0;
	}

	
	private void makeListFiles(){//Put local files in your global list
		int occurences = 0;
		//First copy the local files to the list
		Iterator<HashMap.Entry<String, Integer>> entriesNode = Node.local.entrySet().iterator();
		while (entriesNode.hasNext()) {
			HashMap.Entry<String, Integer> entryNode = entriesNode.next();
			Iterator<HashMap.Entry<String, Boolean>> entriesTotal = Node.filesSystemNode.entrySet().iterator();
			occurences = 0;
			while(entriesTotal.hasNext()){ //Check if the file is already in the node his list
				HashMap.Entry<String, Boolean> entryTotal = entriesTotal.next();
				if(entryTotal.getKey() == entryNode.getKey()){
					occurences++;
				}
			}
			if(occurences == 0){ // The file isn't in the list of the agent
				Node.filesSystemNode.put(entryNode.getKey(), false); //Lock moet nog automatisch bepaald worden
				localList.put(entryNode.getKey(), false);
			}
		}
		//Copy the replica files to the list
		Iterator<HashMap.Entry<String, Integer>> entriesReplica = Node.replica.entrySet().iterator();
		while (entriesReplica.hasNext()) {
			HashMap.Entry<String, Integer> entryReplica = entriesReplica.next();
			Iterator<HashMap.Entry<String, Boolean>> entriesTotal = Node.filesSystemNode.entrySet().iterator();
			occurences = 0;
			while(entriesTotal.hasNext()){ //Check if the file is already in the node his list
				HashMap.Entry<String, Boolean> entryTotal = entriesTotal.next();
				if(entryTotal.getKey() == entryReplica.getKey()){
					occurences++;
				}
			}
			if(occurences == 0){ // The file isn't in the list of the agent
				Node.filesSystemNode.put(entryReplica.getKey(), false); //Lock moet nog automatisch bepaald worden
				replicaList.put(entryReplica.getKey(), false);
			}
		}
	}
	
	private void checkDownload(){ //Check if file is downloaded. This is when it is in the replicalist
		Iterator<HashMap.Entry<String, Boolean>> entriesNode = Node.filesSystemNode.entrySet().iterator();
		while (entriesNode.hasNext()) {
			HashMap.Entry<String, Boolean> entryNode = entriesNode.next();
			Iterator<HashMap.Entry<String, Integer>> entriesReplica = Node.replica.entrySet().iterator();
			while(entriesReplica.hasNext()){ //Check if the file is downloaded
				HashMap.Entry<String, Integer> entryReplica = entriesReplica.next();
				if(entryReplica.getKey() == entryNode.getKey()){
					Node.filesSystemNode.put(entryNode.getKey(), false);
				}
			}
		}
	}
	
	private void checkListAgent(){ //Check if agent his list is complete
		int occurences = 0;
		Iterator<HashMap.Entry<String, Boolean>> entriesNode = Node.filesSystemNode.entrySet().iterator();
		while (entriesNode.hasNext()) {
			HashMap.Entry<String, Boolean> entryNode = entriesNode.next();
			Iterator<HashMap.Entry<String, Boolean>> entriesAgent = files_in_system.entrySet().iterator();
			occurences = 0;
			while(entriesAgent.hasNext()){ //Check if the file is already in the agent his list
				HashMap.Entry<String, Boolean> entryAgent = entriesAgent.next();
				if(entryAgent.getKey() == entryNode.getKey()){
					occurences++;
					if (entryAgent.getValue() != entryNode.getValue()){ //check if lock is the same
						files_in_system.put(entryAgent.getKey(), entryNode.getValue());
					}
				}
			}
			if(occurences == 0){ // The file isn't in the list of the agent
				files_in_system.put(entryNode.getKey(), entryNode.getValue());
			}
		}
	}
	
	private void checkListNode(){ //Check if node his list is complete
		int occurences = 0;
		Iterator<HashMap.Entry<String, Boolean>> entriesAgent = files_in_system.entrySet().iterator();
		while (entriesAgent.hasNext()) {
			HashMap.Entry<String, Boolean> entryAgent = entriesAgent.next();
			Iterator<HashMap.Entry<String, Boolean>> entriesNode = Node.filesSystemNode.entrySet().iterator();
			occurences = 0;
			while(entriesNode.hasNext()){ //Check if the file is already in the node his list
				HashMap.Entry<String, Boolean> entryNode = entriesNode.next();
				if(entryAgent.getKey() == entryNode.getKey()){ //file is already in list
					occurences++;
				}
			}
			if(occurences == 0){ // The file isn't in the list of the node
				Node.filesSystemNode.put(entryAgent.getKey(), entryAgent.getValue()); 
			}
		}
	}
	
/*	public static void main(String args[]) {
		Node.local.put("test1", 1);
		Node.local.put("test2", 2);
		Node.replica.put("test3", 3);
		Node.replica.put("test4", 4);
		files_in_system.put("testlijst", true);
		Node.filesSystemNode.put("test4", true);

		(new FileListAgent()).run();
		
		System.out.println("locallist");
		for (HashMap.Entry<String, Integer> entry : Node.local.entrySet()) {
		    String key = entry.getKey();
		    int value = entry.getValue();
		    System.out.println(key + " " + value);
		}
		
		System.out.println("--------------------------------------------------------------------");
		System.out.println("replicalist");
		for (HashMap.Entry<String, Integer> entry : Node.replica.entrySet()) {
		    String key = entry.getKey();
		    int value = entry.getValue();
		    System.out.println(key + " " + value);
		}
		
		System.out.println("--------------------------------------------------------------------");
		System.out.println("filesSystemNode");
		for (HashMap.Entry<String, Boolean> entry : Node.filesSystemNode.entrySet()) {
		    String key = entry.getKey();
		    boolean value = entry.getValue();
		    System.out.println(key + " " + value);
		}
		
		System.out.println("--------------------------------------------------------------------");
		System.out.println("files_in_system");
		for (HashMap.Entry<String, Boolean> entry : files_in_system.entrySet()) {
		    String key = entry.getKey();
		    boolean value = entry.getValue();
		    System.out.println(key + " " + value);
		}
	}*/
}
