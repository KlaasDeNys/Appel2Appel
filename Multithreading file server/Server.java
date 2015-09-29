package fileServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * @Source: http://www.rgagnon.com/javadetails/java-0542.html
 *
 */

public class Server {

  public final static int SOCKET_PORT = 13267;  // you may change this
  public final static String FILE_TO_SEND = "c:/Users/jens/Desktop/Server/Testdocument.txt";  // you may change this

  public static void main (String [] args ) throws IOException {
    
    
    ServerSocket servsock = null;
    Socket sock = null;
    
    ExecutorService executor = Executors.newFixedThreadPool(5);	// Aantal cliënten dat tegelijk mag downloaden.
    
    try {
      servsock = new ServerSocket(SOCKET_PORT);
      while (true) {
        System.out.println("Waiting...");
        sock = servsock.accept();
        executor.submit(new Download(sock));
      }
    }
    finally {
      if (servsock != null) servsock.close();
      executor.shutdown();
      if (sock!=null) sock.close();
    }
  }
}
   