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
		int port = 2008;
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(port);
			//serverSocket.listen();
			while (true) {
				Socket s = serverSocket.accept();
				RequestProcessor processor = new RequestProcessor(s);
				processor.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	static class RequestProcessor extends Thread{
		Socket s;
		
		public RequestProcessor(Socket s){
			this.s = s;
		}
		
		public void run(){	
			InputStream in;
			try {
				in = s.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				int lineCount = 0;
				int port = 80; // default
				String request = "";
				String host = "";
				String line = "";
				
				while(!(line = br.readLine()).equals("")){
					lineCount++;
					System.out.println(line);
					if(lineCount == 1) {
						System.out.println(">>> " + line);
						request = line.split(" ")[0];
						String url = line.split(" ")[1];
						if(url.toLowerCase().startsWith("https//")){
							port = 443;
						}
					}
					
					String lineWithoutSpace = line.replace(" ", "").toLowerCase();
					if(lineWithoutSpace.startsWith("host:")){ // host line
						String[] parts = lineWithoutSpace.substring(5).split(":");
						if(parts.length > 1) {
							port = Integer.valueOf(parts[1]).intValue();
						}
						host = parts[0];
					}
				}
				System.out.println();
				System.out.println("host: " + host + " port: " + port + " request type: " + request);
				
				if(request.equals("CONNECT")){ // connect
					
				}else{ // else
					
				}
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			
			
			
			
			
			
			// close the socket!
			try {
				s.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
