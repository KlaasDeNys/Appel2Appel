package NameServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface INameServer extends Remote{

	// Add a node to the system
	boolean add (int id, String adr) throws RemoteException;
	// Delete a node out of the system
	boolean delete (int id) throws RemoteException;
	// Give the ip of the given node (id of the node)
	String lookUp (int id) throws RemoteException;
	// Give the id of the upper neighbor from the given node.
	int getPrev(int id)throws RemoteException;
	// Give the id of the lower neighbor form the given node.
	int getNext(int id)throws RemoteException;
	// Give the rightful owner of the given file.
	int getNode(String filename)throws RemoteException;
}