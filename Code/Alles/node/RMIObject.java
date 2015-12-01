package node;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import agent.Agent;
import agent.IAgent;

public class RMIObject {
	private static final int RMI_PORT = 1099;	// Port for RMI service
	private static final int MULTICAST_PORT = 8889;	// Port for multicast service
	private static final String MULTICAST_IP = "224.2.2.3";	// Multicast messages wil be sended to this address 
	private static final int TCP_PORT = 6790;	// Port use for TCP connections.
	private static Agent ag;
	
	public RMIObject(Agent ag) {
		this.ag = ag;
	}
	
	public static void main (String [] args) {
		Thread agent = new Thread(new Agent(){
			public void run(){
				boolean flag = true;
			   if(!flag)		//Only when the flag is false, the thread ends
				   return;
			   else
				  run();
			}
		});
		//IAgent obj = new Agent();
		
    	Registry registry;
		try {
				registry = LocateRegistry.createRegistry(1098);
				registry.bind("Agent", ag);
		} catch (AlreadyBoundException | RemoteException e) {
				// TODO Auto-generated catch block
			e.printStackTrace(); 
		}
    	
	}
}
