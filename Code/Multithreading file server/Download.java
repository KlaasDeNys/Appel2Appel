package fileServer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class Download implements Runnable {

	private Socket sock;
	private FileInputStream fis;
	private BufferedInputStream bis;
	private OutputStream os;

	public Download(Socket sock) {
		this.sock = sock;
		fis = null;
		bis = null;
		os = null;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(5000);
			System.out.println("Accepted connection : " + sock);
			new Server();
			// send file
			File myFile = new File(Server.FILE_TO_SEND);
			byte[] mybytearray = new byte[(int) myFile.length()];
			fis = new FileInputStream(myFile);
			bis = new BufferedInputStream(fis);
			bis.read(mybytearray, 0, mybytearray.length);
			os = sock.getOutputStream();
			System.out.println("Sending " + Server.FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
			os.write(mybytearray, 0, mybytearray.length);
			os.flush();
			System.out.println("Done.");
			
			if (sock != null)
				sock.close();
			if (bis != null)
				bis.close();
			if (os != null)
				os.close();
			if (sock != null)
				sock.close();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
