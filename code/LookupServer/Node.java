package NameServer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
//import java.util.InputMismatchException;

//import NameServer.ILookupServer;

/**
 * 
 * @author Jens
 * @since 14/10/2015
 *
 */

public class Node {
	
	public Node () {
		
	}
	
	public static void main (String [] args) {
		
		Scanner scanner = new Scanner (System.in);
		
		try {
			INameServer obj = (INameServer) Naming.lookup ("//"+"localhost"+"/LNS");
			int id;
			boolean flag = true;
			do {
				System.out.print ("\n[1]: look up ip\n[2]: add ip\n[3]: delete id\n[0]: quit\n > ");
				int choise = scanner.nextInt();
				scanner.nextLine();
				switch (choise) {
				case 1:
					System.out.print ("\nid: ");
					id = scanner.nextInt();
					System.out.println ("IP adr: " + obj.lookUp(id));
					break;
				case 2:
					System.out.print ("\nID: ");
					id = scanner.nextInt();
					System.out.print ("IP adr: ");
					String ip = scanner.next();
					obj.add(id, ip);
					break;
				case 3:
					System.out.print ("\nid: ");
					id = scanner.nextInt();
					System.out.println ("Delete: " + obj.delete(id));
					break;
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
