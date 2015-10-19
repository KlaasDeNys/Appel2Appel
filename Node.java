
package nameserver;
<<<<<<< HEAD

=======
>>>>>>> origin/master

import java.io.*;
import java.math.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.util.*;


public class Node {

	private static int generateId() {		//Generate an ID.
		Date date = new Date(15,10,15);		//Based on the date, we generate an ID.		
		long diff =date.getTime();
		String stringDate = Objects.toString(diff,null);
		int id = keyHash(stringDate);		//Generate the ID.
		//String ip = "192.1.1.1";
		//obj.addNode(id, ip);
		//String ip = InetAddress.getLocalHost().getHostAddress();
		//NetworkInterface.getNetworkInterfaces()
		return id;
	}
	
	static int keyHash(String key)			//Hashfunction for strings.
	{
	    int hashCode = key.hashCode();
	    hashCode = Math.abs(hashCode);
	    int value = (int) Math.pow(2, 15); 	//2^15 = 32678
	    while (hashCode > 32767 ) { 		//Value must be between 0 and 32768
	    	hashCode = hashCode -value;
	    }
	    return hashCode;
	}
	
	public static void main (String [] args ){
		try 
        { 
           INameServer obj = (INameServer) Naming.lookup( "//"+"66.66.66.70"+"/NameServer");         //objectname in registry 
           //System.out.println(obj.AddNode());
<<<<<<< HEAD
           int id = add();
           String ip = address();
           obj.addNode(id, ip);
           obj.removeNode(id);
           obj.lookUpFile("test");
=======
           boolean test_value = false;
           int id = 0;
           while (test_value == false){ //Only when addNode is successful. ID is unique en in the range.  
        	   id = generateId();		//Generate an ID.
        	   String ip = "66.66.66.70";
        	   test_value = obj.addNode(id, ip); //Add node to the hashmap
           }
           System.out.println("Your id is: " + id);
           
           test_value = false;
           while (test_value == false){			
        	   test_value = obj.deleteNode(id);	//remove node from hashmap
           }
           System.out.println("You are deleted from the system");
           
           String ip_addr = obj.lookUpFile("test");
           System.out.println("The ip-address where the file is, is "+ip_addr);
>>>>>>> origin/master
        } 
        catch (Exception e) 
        { 
           System.out.println("Client exception: " + e.getMessage()); 
           e.printStackTrace(); 
        } 
<<<<<<< HEAD
		//Node n = new Node();
		//boolean k = n.addNode();
		//System.out.println(k);
	}


	private static int add() {
		// TODO Auto-generated method stub
		Date date = new Date(15,10,15);
		long diff =date.getTime();
		String stringDate = date.toString();
		int id = keyHash(stringDate);
		//String ip = "192.1.1.1";
		//obj.addNode(id, ip);
		//String ip = InetAddress.getLocalHost().getHostAddress();
		//NetworkInterface.getNetworkInterfaces()
		return id;
=======
>>>>>>> origin/master
	}
	

	private static String address() throws UnknownHostException {
			
	
			 InetAddress address = InetAddress.getLocalHost(); 
			    String hostIP = address.getHostAddress() ;
			    return hostIP;
		 
		     
		    }
		
	}

