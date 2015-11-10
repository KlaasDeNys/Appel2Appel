package node;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface INode extends Remote {
	void changeNextNode (int id, String ip) throws RemoteException;
	void changePrevNode (int id, String ip) throws RemoteException;
	
	void getFile (int portNr, String ip, String file) throws RemoteException, IOException;
	void deletefile (String filename)throws RemoteException;
}
