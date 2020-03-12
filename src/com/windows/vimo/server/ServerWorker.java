package com.windows.vimo.server;

import java.io.BufferedInputStream;

//1. When the user logs off, SocketException occurs because it's sending msg to already disconnected users (Not resolved)
// 2. multiple clients can communicate with each other (done) - > Personal chat room
// 3. Logoff command can have more than one strings and it still works (not resolved)
// 4. clients can communicate in a group chat (not done) - > Common Chat Room

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import com.windows.vimo.client.Friends;
import com.windows.vimo.database.DBWorker;

public class ServerWorker extends Thread {
	private final Socket clientSocket;
	// private String userName = null;
	private ArrayList<Friends> friendsList = new ArrayList<>();
	private final Server server;
	private OutputStream outputStream;
	private HashSet<String> groupChatSet = new HashSet<>();
	private String userName;
	private String password;
	public boolean signedIn;
	public Connection conn;
	public DBWorker obj;

	public ServerWorker(Server server, Socket clientSocket, Connection conn, DBWorker obj) {
		// TODO Auto-generated constructor stub
		this.obj = obj;
		this.conn = conn;
		this.server = server;
		this.clientSocket = clientSocket;
	}

	@Override
	public void run() {
		try {
			handleClientSocket();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void handleClientSocket() throws IOException, InterruptedException {
		InputStream inputStream = clientSocket.getInputStream();
		this.outputStream = clientSocket.getOutputStream();

		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;

		while ((line = reader.readLine()) != null) {
			System.out.println("Line: " + line);
			String[] tokens = line.split(" ");
			if (tokens != null && tokens.length > 0) {
				String cmd = tokens[0];
				System.out.println("Client command: " + cmd);

				if ("file".equalsIgnoreCase(cmd)) {
					System.out.println("File command received");
					handleFileTransfer(outputStream, tokens);
				} else if ("add".equalsIgnoreCase(cmd)) {
					handleAddingFriends(tokens);
				} else if ("received".equals(cmd)) {
					System.out.println("File received successfully");
				} else if ("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)) { // if the first command is
																								// logoff (still need changes)																	// changes)
					handleLogoff();
					break;
				} else if ("login".equalsIgnoreCase(cmd)) {
					try {
						handleLogin(outputStream, tokens);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else if ("#msg".equals(cmd)) {
					String[] tokens1 = line.split(" ", 2);
					handleGroupMessage(tokens1);
				} else if ("msg".equalsIgnoreCase(cmd)) {
					System.out.println("Message command received");
					String[] tokens1 = line.split(" ", 3);
					handleMessage(tokens1);
				} else {
					String msg = "unknown " + cmd + "\n";
					outputStream.write(msg.getBytes());
				}
			}

		}
		this.clientSocket.close();

//	outputStream.write("Hello World\n".getBytes());

	}

	public void handleFileTransfer(OutputStream outputStream2, String[] tokens) throws IOException {
		// TODO Auto-generated method stub
		String receiver = tokens[1];
		String filepath = tokens[2];
		String fileName = tokens[3];
		// InputStream fileInput = this.clientSocket.getInputStream();
		System.out.println("Receiver name: " + receiver);
		System.out.println("File path: " + filepath);
		System.out.println("File name: " + fileName);
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		OutputStream os = null;
		String format = "file " + this.userName + " " + fileName + "\n";

		List<ServerWorker> workerList = server.getWorkerList();

		try {
			File receiverFile = new File(filepath);
			byte[] mybytearray = new byte[(int) receiverFile.length()];
			fis = new FileInputStream(receiverFile);
			bis = new BufferedInputStream(fis);
			bis.read(mybytearray, 0, mybytearray.length);
			System.out.println("Sending " + fileName + "(" + mybytearray.length + " bytes)");

			for (ServerWorker worker : workerList) {
				if (worker.getUserName().equals(receiver)) {
					worker.send(format); // sending a message to the receive indicating there is going to be a file
											// transfer
					System.out.println("Sending: " + format);
					worker.sendFile(mybytearray, 0, mybytearray.length);
					worker.send("dump\n");
					break;
				}
			}

		} finally {
			if (bis != null)
				bis.close();
			if (fis != null)
				fis.close();

		}

	}

	public void sendFile(byte[] mybytearray, int i, int length) throws IOException {
		// TODO Auto-generated method stub
		outputStream.write(mybytearray, i, length);
		System.out.println("File sent to client");
	}

	private void handleAddingFriends(String[] tokens) throws IOException {
		// TODO Auto-generated method stub
		String friend = tokens[1];
		String newFriend = "add " + this.userName +"\n";
		List<ServerWorker> workerList = server.getWorkerList();
		for(ServerWorker worker: workerList) {
			if(worker.getUserName().equals(friend)) {
				worker.send(newFriend);
			}
		}
	}

	public void handleGroupMessage(String[] tokens) throws IOException {
		// TODO Auto-generated method stub
		String msg = tokens[1];
		List<ServerWorker> workerList = server.getWorkerList();
		String onlineMsg = "#msg " + this.userName + " " + msg + "\n";
		for (ServerWorker worker : workerList) {
			if (!userName.equals(worker.getUserName())) { // sending msg to all other users except the sender
				worker.send(onlineMsg);
			}
		}

	}

	// Message format (Personal chat room): "msg" "user" msgBody
	// Message format (Common chat room): "msg" "#groupName" msgBody
	public void handleMessage(String[] tokens) throws IOException {
		// TODO Auto-generated method stub

		String sendTo = tokens[1];
		String msgBody = tokens[2];
		String sendMsg;
		boolean valid = true;
		ServerWorker tempWorker = new ServerWorker(this.server, this.clientSocket, null, null);
		sendMsg = "Invalid recepient";

		boolean isGroupName = sendTo.charAt(0) == '#'; // true if clients wants to send msg in chatroom

		if (sendTo == null || msgBody == null) {
			valid = false;
			sendMsg = "Invalid format";
		}
		if (valid) {
			List<ServerWorker> workerList = server.getWorkerList();

			for (ServerWorker worker : workerList) {
				if (sendTo.equals(userName) && sendTo.equalsIgnoreCase(worker.getUserName())) { // if the user types in
																								// it's own username;
					sendMsg = "You can't send msg to yourself";
					worker.send(sendMsg);
					// return;
				}

				/*
				 * if(userName.equals(worker.getUserName())){ // for later sending the msg to
				 * the sender for invalid recipient tempWorker = worker; }
				 */
				else if (sendTo.equals(worker.getUserName())) {
					sendMsg = "msg " + userName + " " + msgBody + "\n";
					worker.send(sendMsg);
					// return;
				}
			}
		}

		tempWorker.send(sendMsg);

	}

	public void handleLogoff() throws IOException {
		// TODO Auto-generated method stub
		server.removeWorker(this);
		List<ServerWorker> workerList = server.getWorkerList();
		// send other online users current user's status
		String onlineMsg = "offline " + userName + "\n"; // the user is going to be offline
		for (ServerWorker worker : workerList) { // sending other users the message that the current user is offline
			if (!userName.equals(worker.getUserName())) {
				worker.send(onlineMsg);

			}
		}
		System.out.println("Connection disconnected from " + clientSocket);
		clientSocket.close();
	}

	public String getUserName() {
		return userName;
	}

	public void handleLogin(OutputStream outputStream, String[] tokens) throws IOException, SQLException {
		// TODO Auto-generated method stub
		if (tokens.length == 3) {
			String userName = tokens[1];
			String password = tokens[2];

			if (this.obj.checkInTable(conn, userName, password)) {
				String msg = "ok login\n";
				this.userName = userName;
				this.password = password;
				outputStream.write(msg.getBytes());
				System.out.println("User logged in successfully " + userName);

				List<ServerWorker> workerList = server.getWorkerList();
				// send current user all other online userNames
				for (ServerWorker worker : workerList) {

					if (worker.getUserName() != null) {
						if (!userName.equals(worker.getUserName())) {
							String msg2 = "online " + worker.getUserName() + "\n";
							send(msg2);
						}
					}
				}

				// send other online users current user's status
				String onlineMsg = "online " + userName + "\n";
				for (ServerWorker worker : workerList) {
					if (!userName.equals(worker.getUserName())) {
						worker.send(onlineMsg);
					}
				}
			} else {
				String msg = "error loging in\n";
				outputStream.write(msg.getBytes());
				System.out.println("Login failed for " + userName);
			}
		}
	}

	public void send(String msg) throws IOException {
		// TODO Auto-generated method stub
		if (userName != null) {
			outputStream.write(msg.getBytes());
		}

	}

}
