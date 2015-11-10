package node;

public class ThreadFiles extends Thread{
	   public void run() {
		   boolean flag = true;
		   System.out.println("Thread control files started...");
		   try{
			   
			   Thread.sleep(5000);	//Checks every 5s
		   } catch(Exception e){
			   System.out.println("Thread is ended! The error is " + e.getMessage());
		   }
		   if(!flag)		//Only when the flag is false, the thread ends
			   return;
		   else
			   run();
		   //Hier de naam van de functie die aangeroepen moet worden. Voorlopig controlFiles() genoemd.
		   //doubles()
		   //controlFiles();
	   }
	   
	   public static void main(String args[]) {
		   (new ThreadFiles()).start();
	   }
}
