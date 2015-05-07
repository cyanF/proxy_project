package cse461_project1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Proxy {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		if (args.length != 1) {
//	    	System.err.println("Usage: java Proxy <port_num>");
//	        System.exit(1);
//	    }
//	
//		int port = Integer.valueOf(args[0]).intValue(); 
//		
		int port = 2000;
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(port);
			//serverSocket.listen();
			while (true) {
				Socket s = serverSocket.accept();
				InputStream in = s.getInputStream();

				String readLine;
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				 
				while (((readLine = br.readLine()) != null)) {
				System.out.println(readLine);
				}
				
				
				s.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			int a = 5;
		} 
		
		
	}

}
