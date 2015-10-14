package nameserver;
import hashing_function.Hash;

import java.io.*;
import java.math.*;
import java.net.InetAddress;
import java.rmi.Naming;
import java.util.*;


public class Node {

	
	
	static int keyHash(String key)
	{
	    int hashCode = key.hashCode();
	    hashCode = Math.abs(hashCode);
	    int value = (int) Math.pow(2, 15);
	    while (hashCode > 32767 ) {
	    	hashCode = hashCode -value;
	    }
	    System.out.println("input hash code =");
	    System.out.println(hashCode);
	    return hashCode;
	}
	
	public static void main (String [] args ){
		try 
        { 
           INameServer obj = (INameServer) Naming.lookup( "//"+"66.66.66.69"+"/NameServer");         //objectname in registry 
           //System.out.println(obj.AddNode());
           int id = add();
           String ip = "192.1.1.1";
           obj.addNode(id, ip);
           obj.removeNode(id);
           obj.lookUpFile("test");
        } 
        catch (Exception e) 
        { 
           System.out.println("Client exception: " + e.getMessage()); 
           e.printStackTrace(); 
        } 
		//Node n = new Node();
		//boolean k = n.addNode();
		//System.out.println(k);
	}


	private static int add() {
		// TODO Auto-generated method stub
		Date date = new Date(15,10,15);
		long diff =date.getTime();
		String stringDate = Objects.toString(diff,null);
		int id = keyHash(stringDate);
		//String ip = "192.1.1.1";
		//obj.addNode(id, ip);
		//String ip = InetAddress.getLocalHost().getHostAddress();
		//NetworkInterface.getNetworkInterfaces()
		return id;
	}
}
