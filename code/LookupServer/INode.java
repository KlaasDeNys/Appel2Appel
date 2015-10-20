package NameServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface INode extends Remote{
	int generateId() throws RemoteException;
	int keyHash(String key) throws RemoteException;
	String address() throws RemoteException;
	boolean changePrevious(int id) throws RemoteException;
	boolean contactPrevious() throws RemoteException;
	boolean changeNext(int id) throws RemoteException;
	boolean contactNext() throws RemoteException;
	void initialise() throws RemoteException;
}
