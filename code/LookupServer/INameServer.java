package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 * @author Jens
 * @since 14/10/2015
 *
 */

public interface INameServer extends Remote{
	String lookUp (int id) throws RemoteException;	// Give the ip of the specified node.
	boolean add (int id, String adr) throws RemoteException;	// Add a node by the given addres.
}
