package node;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface INode extends Remote {
	// Set the next node of this node
	void changeNextNode (int id, String ip) throws RemoteException;
	// Set the previous node of this node
	void changePrevNode (int id, String ip) throws RemoteException;
	
	// When some node want to send a file to this node, he need to call this method.
	void getFile (int portNr, String ip, String file) throws RemoteException, IOException;
	// When a file needs to be deleted at a node, this method needs to be called.
	void deletefile (String filename)throws RemoteException;
}
