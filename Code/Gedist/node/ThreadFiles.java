package node;

import java.io.IOException;


public class ThreadFiles extends Thread{
	   public void run() {
		   boolean flag = true;
		    try{
			   
			   Thread.sleep(5000);	//Checks every 5s
		   } catch(Exception e){
			   System.out.println("Thread is ended! The error is " + e.getMessage());	// ----report
		   }
		   if(!flag)		//Only when the flag is false, the thread ends
			   return;
		   else
			   
		   try {
			   //if (Node.ipNext != null && Node.ipNext != Node.ip()){
			Node.doubles(Node.local, Node.replica);
			fileagent.sendList();	// Update the Hashmap of the fileAgent
		
			run();	// Create a loop.
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   
	   }
	   
	   /*public static void main(String args[]) {
		   //agent.FileListAgent fileAgent = new FileListAgent();
		   (new ThreadFiles()).start();
	   }*/
}