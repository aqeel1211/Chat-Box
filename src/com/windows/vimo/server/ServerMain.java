package com.windows.vimo.server;



public class ServerMain {
	public static void main(String[] args){
		
		int port =8813;
		Server server = new Server(port);
		server.start(); // A thread has been created with the current server port
		// multiple server threads can be created with different ports (that means different servers)

	}


}
