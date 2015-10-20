package NameServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 * @author Jens
 * @since 14/10/2015
 *
 */

public interface INameServer extends Remote{


	boolean delete (int id) throws RemoteException;

	String lookUp (int id) throws RemoteException;	// Give the ip of the specified node.
	boolean add (int id, String adr) throws RemoteException;	// Add a node by the given addres.
	int getPrev(int id) throws RemoteException;
	int getNext(int id) throws RemoteException;
}
