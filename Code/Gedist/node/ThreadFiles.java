package node;

import java.io.IOException;


public class ThreadFiles extends Thread{
	   public void run() {
		   boolean flag = true;
		    try{			   
			   Thread.sleep(1000);	//Checks every 5s
		   } catch(Exception e){
		   }
		   if(!flag)		//Only when the flag is false, the thread ends
			   return;
			   
		   try {
			   //if (Node.ipNext != null && Node.ipNext != Node.ip()){
			Node.doubles(Node.local, Node.replica);
			fileagent.sendList();	// Update the Hashmap of the fileAgent
		
			run();	// Create a loop.
		   } catch (InterruptedException e) {
			   new errorReport ("Failed to repeat file update process. (InterruptedException)");
		   } catch (IOException e) {
			   new errorReport ("File Agent Error", "Failed to send file agent information to other nodes. (IOException)");
		   }
		   
	   }
	   
	   /*public static void main(String args[]) {
		   //agent.FileListAgent fileAgent = new FileListAgent();
		   (new ThreadFiles()).start();
	   }*/
}
