package NameServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Scanner;

import NameServer.INameServer;
import NameServer.INode;


/**
 * 
 * @author Jens
 * @since 14/10/2015
 *
 */

public class Node extends UnicastRemoteObject implements INode {
	
	public int prevId;
	public int nextId;
	public int prevNode;
	public int nextNode;
	public int id;
	private static final long serialVersionUID = 1L;
	
	public Node () throws RemoteException{
		super();
	}
	
	public int generateId() {		//Generate an ID.
		Date date = new Date(16,10,15);		//Based on the date, we generate an ID.		
		String stringDate = date.toString();
		id = keyHash(stringDate);		//Generate the ID.
		//String ip = "192.1.1.1";
		//obj.addNode(id, ip);
		//String ip = InetAddress.getLocalHost().getHostAddress();
		//NetworkInterface.getNetworkInterfaces()
		return id;
	}
	
	public int keyHash(String key)			//Hashfunction for strings.
	{
	    int hashCode = key.hashCode();
	    hashCode = Math.abs(hashCode);
	    int value = (int) Math.pow(2, 15); 	//2^15 = 32678
	    while (hashCode > 32767 ) { 		//Value must be between 0 and 32768
	    	hashCode = hashCode -value;
	    }
	    return hashCode;
	}
	
	public String address(){
		
		InetAddress address;
		String hostIP = "";
		try {
			address = InetAddress.getLocalHost();
			hostIP = address.getHostAddress() ;
		} catch (UnknownHostException e) {
			System.out.println("UnknownHostException: " + e);
			e.printStackTrace();
		}
		return hostIP;
	}
	
	public boolean changePrevious(int id){
		prevNode = id;
		return true;
	}
	
	public boolean contactPrevious(){
		try {
			INameServer obj1 = (INameServer) Naming.lookup ("//"+"10.1.1.1"+"/LNS");
			prevId = obj1.getPrev(id);
			System.out.println("PrevId: " +prevId);
			String prevIp = obj1.lookUp(prevId);
			System.out.println("PrevIP: " +prevIp);
			INode objNode = (INode) Naming.lookup("//"+prevIp+"/Node"); //fout
			System.out.println("Gelukt!");
			objNode.changeNext(id);
		} catch (MalformedURLException e) {
			System.out.println("Client MalformedURLException: " + e);
			e.printStackTrace();
			return false;
		} catch (RemoteException e) {
			System.out.println("Client RemoteException: " + e);
			e.printStackTrace();
			return false;
		} catch (NotBoundException e) {
			System.out.println("Client NotBoundException: " + e);
			e.printStackTrace();
			return false;
		}			
		return true;
	}
	
	public boolean changeNext(int id){
		nextNode = id;
		return true;
	}
	
	public boolean contactNext(){
		try {
			INameServer obj1 = (INameServer) Naming.lookup ("//"+"10.1.1.1"+"/LNS");
			nextId = obj1.getNext(id);
			String nextIp = obj1.lookUp(nextId);
			INode objNode = (INode) Naming.lookup("//"+nextIp+"/Node"); 
			objNode.changePrevious(id);
		} catch (MalformedURLException e) {
			System.out.println("Client MalformedURLException: " + e);
			e.printStackTrace();
		} catch (RemoteException e) {
			System.out.println("Client RemoteException: " + e);
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println("Client NotBoundException: " + e);
			e.printStackTrace();
		}
		return true;
	}
	
	/*public void initialise(){
		Socket sock;
		try {
			sock = new Socket("test",13267);
			Download initObj = new Download(sock);
			initObj.run();
		} catch (UnknownHostException e) {
			System.out.println("UnknownHostException: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException: " + e);
			e.printStackTrace();
		}		
	}*/
	
	public void main (String [] args) {
		
		Scanner scanner = new Scanner (System.in);
		
		try {
			INameServer obj = (INameServer) Naming.lookup ("//"+"10.1.1.1"+"/LNS");
			boolean flag = true;
			do {
				System.out.print ("\n[1]: look up ip\n[2]: add ip\n[3]: Delete id\n[4]: Contact nodes\n[0]: quit\n > ");
				int choise = scanner.nextInt();
				scanner.nextLine();
				switch (choise) {
				case 1:
					System.out.print ("\nid: ");
					id = scanner.nextInt();
					System.out.println ("IP adr: " + obj.lookUp(id));
					break;
				case 2:
					id = generateId();		//Generate an ID
					String ip = address();
					System.out.println(id);
					System.out.println(ip);
					obj.add(id, ip);
					break;
				case 3:
					System.out.println("\n id: ");
					id = scanner.nextInt();
					System.out.println("Delete: "+ obj.delete(id));
				case 4:
					System.out.println("Contact: "+ contactPrevious());	
				case 0:
					flag = false;
					break;
				default:
					System.out.print ("\nNot a possibility\n");
					break;
				}
			}while (flag);
		} catch (MalformedURLException e) {
			System.out.println("Client MalformedURLException: " + e);
		} catch (RemoteException e) {
			System.out.println("Client RemoteException: " + e);
		} catch (NotBoundException e) {
			System.out.println("Client NotBoundException: " + e);
		} catch (ClassCastException e) {
			System.out.println("Client ClassCastException: " + e);
		}
		
		scanner.close();
	}
}
