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
		int port = 2012;
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
				String firstLine = "";
				while((line = br.readLine()) != null && !line.equals("")){
					lineCount++;
					String lineWithoutSpace = line.replace(" ", "").toLowerCase();
					//System.out.println(line);
					if(lineCount == 1) {
						firstLine = line;
						request += line.replace("HTTP/1.1", "HTTP/1.0") + "\r\n";
						System.out.println(">>> " + line);
						requestType = line.split(" ")[0];
						String url = line.split(" ")[1];
						if(url.toLowerCase().startsWith("https//")){
							port = 443;
						}
					}else if(lineWithoutSpace.startsWith("host:")){ // host line
						request += line + "\r\n";
						String[] parts = lineWithoutSpace.substring(5).split(":");
						if(parts.length > 1) {
							port = Integer.valueOf(parts[1]).intValue();
						}else{
							int port_temp = getPortFromURL(firstLine);
							if(port_temp >= 0){
								port = port_temp;
							}
						}
						host = parts[0];
					}else if(lineWithoutSpace.startsWith("connection:")){
						request += line.replace("keep-alive", "close") + "\r\n";
					}else{
						request += line + "\r\n";
					}
				}
				
				if(requestType.equals("CONNECT")){ // connect
					System.out.println("host: " + host + " port: "+port);
					try{
						Socket sender = new Socket(host, port);
						sendHTTPResponse("HTTP/1.1 200 OK\r\n", s);
						
						// this thread listen to the browser and forward to web site server
						TCPTunnel tunnel = new TCPTunnel(sender,s);
						tunnel.start();
						// the main thread listen to the server and forward to browser
						copyStream(sender.getInputStream(), s.getOutputStream(), "inseide connect");
						s.shutdownInput();
						System.out.println("finished");
						try {
							tunnel.join();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						sender.close();
						s.close();
					}catch(IOException e){
						System.out.println("Connetion failed");
						sendHTTPResponse("HTTP/1.1 502 Bad Gateway\r\n", s);
					}
					
					
				}else{ // if the request type is not "CONNECT"
					Socket sender = new Socket(host, port);
					// send request to web site
					PrintWriter s_out = new PrintWriter(sender.getOutputStream(), true);
					s_out.println(request);
					
					// read the reply from the web site and forward it to the browser
					copyStream(sender.getInputStream(), s.getOutputStream(), "inside not connect");
					
					// close the socket
					sender.close();
					s.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.out.println("Error in socket");
			}
		}
		
		public static void copyStream(InputStream input, OutputStream output, String where){
			byte[] buffer = new byte[1024];
			int bytesRead;
			try {
				while ((bytesRead = input.read(buffer)) != -1){
					output.write(buffer, 0, bytesRead);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Error when copying stream...");
				System.out.println("where: " + where);
				e.printStackTrace();
			}
		}
		
		private static void sendHTTPResponse(String response, Socket s){
			PrintWriter s_out;
			try {
				s_out = new PrintWriter(s.getOutputStream(), true);
				s_out.println(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Error when sending response");
			}
		}
		
		private static int getPortFromURL(String line){
			String[] parts = line.split(" ");
			if(parts.length >= 2){
				String[] URLParts = parts[1].split(":");
				if(URLParts.length >= 2){
					try{
						int port = Integer.valueOf(URLParts[URLParts.length-1]).intValue();
						return port;
					}catch(NumberFormatException e){
						return -1;
					}
				}
			}
			return -1;			
		}
	}
	static class TCPTunnel extends Thread{
		Socket sender = null;
		Socket recevier = null;
	
		public TCPTunnel(Socket sender, Socket recevier){
			this.sender = sender;
			this.recevier = recevier;
		}
		
		public void run(){
			try {
				RequestProcessor.copyStream(recevier.getInputStream(), sender.getOutputStream(),"thread");
				sender.shutdownInput();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("TCPTunnel Error");
			}
			System.out.println("thread finished");
			
		}
	}
	
}
