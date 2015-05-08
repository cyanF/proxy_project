package cse461_project1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
		int port = 2011;
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
			try {
				InputStream in;
				in = s.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				int lineCount = 0;
				int port = 80; // default
				String requestType = "";
				String host = "";
				String line = "";
				String request = "";
				while((line = br.readLine()) != null && !line.equals("")){
					lineCount++;
					System.out.println(line);
					if(lineCount == 1) {
						request += line.replace("HTTP/1.1", "HTTP/1.0") + "\r\n";
						System.out.println(">>> " + line);
						requestType = line.split(" ")[0];
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
					}else if(lineWithoutSpace.startsWith("connection:")){
						request += line.replace("keep-alive", "close") + "\r\n";
					}else{
						request += line + "\r\n";
					}
				}
				
				if(requestType.equals("CONNECT")){ // connect
					
				}else{ // else
					Socket sender = new Socket(host, port);
					// send request to web site
					PrintWriter s_out = new PrintWriter(sender.getOutputStream(), true);
					s_out.println(request);
					
					// read the reply from the web site and forward it to the browser
					copyStream(sender.getInputStream(), s.getOutputStream());
					
					// close the socket
					sender.close();
					s.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		private static void copyStream(InputStream input, OutputStream output){
			byte[] buffer = new byte[1024]; // Adjust if you want
			int bytesRead;
			try {
				while ((bytesRead = input.read(buffer)) != -1){
					output.write(buffer, 0, bytesRead);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
