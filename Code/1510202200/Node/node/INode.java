package node;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface INode extends Remote {
	void changeNextNode (int id, String ip) throws RemoteException;
	void changePrevNode (int id, String ip) throws RemoteException;
	
	
	void deletefile (String filename)throws RemoteException;
	void getFile(String ip, String filename)throws RemoteException;
}
