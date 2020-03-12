package com.windows.vimo.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.windows.vimo.database.DBWorker;

public class Server extends Thread {

	private final int serverPort;
	// private final Server server;
//	private ArrayList<String,String> userDB = new ArrayList<>(); 
	public ArrayList<ServerWorker> workerList = new ArrayList<>();
	private String userName;
	private String password;

	public DBWorker obj = new DBWorker();
	public Connection conn = null;

	public Server(int serverPort) {
		// TODO Auto-generated constructor stub

		this.serverPort = serverPort;

		// obj.run();

		try {
			conn = obj.getConnection();
			System.out.println("connection name is :: " + conn.getClass().getName());
			System.out.println("Connected to database\n");
		} catch (SQLException e) {
			System.out.println("ERROR: Could not connect to the database");
			e.printStackTrace();
			return;
		}

	}

	public List<ServerWorker> getWorkerList() {
		return workerList;
	}

	@Override
	public void run() {
		try {
			InetAddress addr = InetAddress.getByName("127.0.0.1");
			ServerSocket serverSocket = new ServerSocket(serverPort, 100, addr);
			// System.out.println(serverSocket.getChannel());
			System.out.println("Server is up and running . . .");
			System.out.println("Assigned IP: " + serverSocket.getInetAddress());
			System.out.println("Assigned Port: " + serverSocket.getLocalPort());
			while (true) {
				System.out.println("Waiting for connections ...");
				Socket clientSocket = serverSocket.accept();
				System.out.println("Accepted connection from " + clientSocket);
				ServerWorker worker = new ServerWorker(this, clientSocket, conn, obj); // current server with any client
				workerList.add(worker); // List of clients connected with current (this) server (currently online)
				worker.start(); // starts a thread of current server with each new server
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void removeWorker(ServerWorker serverWorker) {
		// TODO Auto-generated method stub
		workerList.remove(serverWorker);
	}

}
