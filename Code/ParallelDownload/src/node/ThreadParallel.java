package node;

import java.io.IOException;
import java.rmi.RemoteException;

public class ThreadParallel extends Thread {
	private Thread t;
	private INode node;
	private String ipadress;
	private int socketport;
	private String filename;
	private int length;
	private int index;
	  
	ThreadParallel(){}
	
	ThreadParallel( final INode node, final String ip,final int socketPort,final String filename,int length,int index){
		this.node = node;
	    this.ipadress= ip;
	    this.socketport = socketPort;
	    this.filename = filename;
	    this.length = length;
	    this.index = index;
	    System.out.println("Starting download file! ");
	}
	
	public void run(){
		System.out.println("Running download file");
	    try {
			node.getFile(socketport, ipadress, filename, Node.pathReplica,length,index);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     System.out.println("Thread  exiting.");
	   }
	}

