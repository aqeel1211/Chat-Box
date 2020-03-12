package com.windows.vimo.client;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

public class Client {

	
	private final String serverName;
	private final int serverPort;
	private Socket socket;
	private OutputStream serverOut;
	private InputStream serverIn;
	private BufferedReader bufferedIn;
	private ObservableList<Friends> userNames = FXCollections.observableArrayList();
	
	private ObservableList<Friends> friendList = FXCollections.observableArrayList();
	public String userName, password;
	
	private ArrayList<UserStatusListener> userStatusListeners;
	private ArrayList<MessageListener> messageListeners;
	private ArrayList<PrivateChatWindow> privateChatWindows;
//	private TableView<Friends> currentOnlineUsers;
	private TextArea MessageWindow;
	private PrivateChatWindow groupChatWindow;
	private ChatWindowController myChatWindowController;
	private Label TabName;
	private TableView<Friends> FriendsTable;
	public final static int FILE_SIZE = 6022386; // for file transfer
	public final static String FILE_TO_RECEIVED = "e:/";

	public Client(String serverName, int serverPort) {
		// TODO Auto-generated constructor stub
		// read = new Scanner (System.in);
		this.serverName = serverName;
		this.serverPort = serverPort;
		
		userStatusListeners = new ArrayList<>();
		messageListeners = new ArrayList<>();
		privateChatWindows = new ArrayList<>();
		TextArea textArea = new TextArea();
		textArea.clear();
		PrivateChatWindow groupWindow = new PrivateChatWindow("groupChat", textArea);
		privateChatWindows.add(groupWindow);
	}

	public static void main(String[] args) throws IOException {
		Client client = new Client("localhost", 8813);
		client.addUserSattustListener(new UserStatusListener() {
			@Override
			public void online(String login) {
				System.out.println("ONLINE: " + login);
			}

			@Override
			public void offline(String login) {
				System.out.println("OFFLINE: " + login);
			}
		});

		client.addMessageListener(new MessageListener() {
			@Override
			public void onMessage(String fromLogin, String msgBody) {
				System.out.println(fromLogin + "===>" + msgBody);
			}
		});

		if (!client.connect()) {
			System.err.println("Connect failed.");
		} else {
			System.out.println("Connect successful");

			System.out.println("Enter your username: ");
			Scanner read = new Scanner(System.in);
			String userName = read.nextLine();
			System.out.println("Enter your password: ");
			String password = read.nextLine();
			if (client.logIn(userName, password)) {
				client.userName = userName;
				client.password = password;
				System.out.println("Login Successful");
				// client.sendDetails(client.userName,client.password);
				// client.msg("guest","Hello world");
			} else {
				System.out.println("Login Failed");
			}
			// client.logoff();

		}
	}

	public void sendDetails(String userName2, String password2) {
		// TODO Auto-generated method stub

	}

	public void logoff() throws IOException {
		// TODO Auto-generated method stub
		String cmd = "logoff\n";
		serverOut.write(cmd.getBytes());
	}

	public void msg(String sendTo, String msgBody, int opt, String fileName) throws IOException {
		// TODO Auto-generated method stub
		if (opt == 1) {
			String cmd = "msg " + sendTo + " " + msgBody + "\n";
			serverOut.write(cmd.getBytes());
		} else if (opt == 2) {
			String cmd = "#msg " + msgBody + "\n";
			serverOut.write(cmd.getBytes());
			System.out.println("Group message");
		} else if (opt == 3) {
			System.out.println("Option 3");
			String cmd = "file " + sendTo + " " + msgBody + " " + fileName + "\n";
			System.out.println("Command: " + cmd);
			serverOut.write(cmd.getBytes());
		}
	}

	public void groupMsg(String msgBody) throws IOException {
		String msg = "#msg " + msgBody;
		serverOut.write(msg.getBytes());
		System.out.println("Message sent: " + msg);
	}
	/*
	 * private boolean sendMsg(String msg) throws IOException { -> needs update //
	 * TODO Auto-generated method stub if(msg.equalsIgnoreCase("quit") ||
	 * msg.equalsIgnoreCase("logoff")) { serverOut.write(msg.getBytes()); return
	 * true; } else { serverOut.write(msg.getBytes()); return false; } }
	 */

	public boolean logIn(String login, String password) throws IOException {
		// TODO Auto-generated method stub
		String cmd = "login " + login + " " + password + "\n";
		serverOut.write(cmd.getBytes());

		String response = bufferedIn.readLine();
		System.out.println("Response from server: " + response);

		if ("ok login".equalsIgnoreCase(response)) {
			startMessageReader();
			return true;
		} else
			return false;
	}

	private void startMessageSender() {
		Thread t = new Thread() {
			@Override
			public void run() {
				sendMessageLoop();
			}

		};
		t.start();
	}

	private void sendMessageLoop() {
		// TODO Auto-generated method stub

	}

	private void startMessageReader() {
		// TODO Auto-generated method stub
		Thread t = new Thread() {
			@Override
			public void run() {
				readMessageLoop();
			}
		};
		t.start();
	}

	private void readMessageLoop() {
		// TODO Auto-generated method stub
		try {
			String line;
			line = null;
			while ((line = bufferedIn.readLine()) != null) {
				// System.out.println("Line: "+line);
				String[] tokens1 = null;
				Pattern pattern = Pattern.compile(" ");
				tokens1 = pattern.split(line);
				// System.out.println("Response: " + tokens1[1]);
				if (tokens1 != null && tokens1.length > 0) {
					String cmd = tokens1[0]; // first word is at index 1
					// System.out.println("CMD: "+ cmd);

					if ("online".equalsIgnoreCase(cmd)) { // if a user has logged in
						// System.out.println("Online present");
						handleOnline(tokens1);
					} else if ("dump".equals(cmd)) {
						System.out.println("Dump received");
					}
					else if("add".equals(cmd)) {
						handleNewFriend(tokens1);
					}
					else if ("file".equalsIgnoreCase(cmd)) {
						receiveFile(tokens1);
					} else if ("offline".equalsIgnoreCase(cmd)) { // if a user has signed out
						// System.out.println("Offline");
						handleOffline(tokens1);
					} else if ("msg".equalsIgnoreCase(cmd)) { // if it's a personal message

						String[] tokenMsg = line.split(" ", 3);
						handleMessage(tokenMsg);
					} else if ("#msg".equalsIgnoreCase(cmd)) { // if it's a group message
						String[] tokenMsg = line.split(" ", 3);
						handleGroupMessage(tokenMsg);
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public void handleNewFriend(String[] tokens1) {
		// TODO Auto-generated method stub
		String friendName = tokens1[1];        // sender
		Friends newFriend = new Friends(friendName);
		this.friendList.add(newFriend);
		this.FriendsTable.setItems(this.friendList);
	}

	private void receiveFile(String[] tokens1) throws IOException {
		// TODO Auto-generated method stub
		String sender = tokens1[1];
		String fileName = tokens1[2];
		String recAddr = FILE_TO_RECEIVED + fileName;

		int bytesRead;
		int current = 0;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;

		System.out.println("File " + FILE_TO_RECEIVED + " downloaded (" + current + " bytes read)");

		try {

			// receive file
			byte[] mybytearray = new byte[FILE_SIZE];
			this.serverIn = this.socket.getInputStream();
			fos = new FileOutputStream(recAddr);
			bos = new BufferedOutputStream(fos);
			bytesRead = this.serverIn.read(mybytearray, 0, mybytearray.length);
			current = bytesRead;

			for (PrivateChatWindow window : privateChatWindows) {
				if (window.getUserName().equals(sender)) {
					// System.out.println("UserName found");
					this.MessageWindow.clear();
					window.getFriendsWindow().appendText("\n" + fileName + " received (" + current
							+ " bytes read) -> downloaded path -> " + recAddr);
					this.MessageWindow.appendText(window.getFriendsWindow().getText());
					break;
				}
			}

			System.out.println("initiating download");
			serverOut.write("received\n".getBytes());
			while (bytesRead > -1) {
				serverOut.write("received\n".getBytes());
				bytesRead = this.serverIn.read(mybytearray, current, (mybytearray.length - current));
				if (bytesRead >= 0)
					current += bytesRead;
			}

			bos.write(mybytearray, 0, current);
			bos.flush();
			System.out.println("File " + FILE_TO_RECEIVED + " downloaded (" + current + " bytes read)");

		} finally {
			if (bos != null)
				bos.close();
			if (fos != null)
				fos.close();

		}

	}

	public void handleGroupMessage(String[] tokens1) {
		// TODO Auto-generated method stub
		String user = tokens1[1];
		String msg = tokens1[2]; // use the translation function here as well
		for (MessageListener listener : messageListeners) {
			listener.onMessage(user, msg);
			for (PrivateChatWindow window : privateChatWindows) {
				if (window.getUserName().equals("groupChat")) {
					System.out.println("UserName found");
					this.MessageWindow.clear();
					window.getFriendsWindow().appendText((msg.isEmpty() ? "" : "\n") + user + "->" + msg);
					this.MessageWindow.appendText(window.getFriendsWindow().getText());
					break;
				}
			}
		}

	}

	public void handleMessage(String[] tokenMsg) {
		// TODO Auto-generated method stub
		String login = tokenMsg[1];
		String msgBody = tokenMsg[2];

		// this.TabName.setText(login);

		for (MessageListener listener : messageListeners) {
			listener.onMessage(login, msgBody);

			for (PrivateChatWindow window : privateChatWindows) {
				if (window.getUserName().equals(login)) {
					System.out.println("UserName found");
					this.MessageWindow.clear();
					window.getFriendsWindow().appendText((msgBody.isEmpty() ? "" : "\n") + login + "->" + msgBody);
					this.MessageWindow.appendText(window.getFriendsWindow().getText());
					break;
				}
			}

		}
	}

	public void sendFile(String user, String path, String fileName) throws IOException {
		// TODO Auto-generated method stub

	}

	public void handleOffline(String[] tokens) {
		// TODO Auto-generated method stub

		String login = tokens[1];
		/*
		 * for(Friends friend: userNames) { if(friend.getUserName().equals(login)) {
		 * Friends tempFriend = friend; this.userNames.remove(tempFriend); break; } }
		 */
		

		for (UserStatusListener listener : userStatusListeners) {
			listener.offline(login);

			// userNames.remove(new Friends(login));
		}
	}

	public void handleOnline(String[] tokens) {
		// TODO Auto-generated method stub
		String login = tokens[1];
		boolean alreadyOnline = false;
		for (UserStatusListener listener : userStatusListeners) {
			listener.online(login);
			for (Friends friend : userNames) {
				if (friend.getUserName().equals(login)) {
					alreadyOnline = true;
					break;
				}
			}
			if (!alreadyOnline)
				this.userNames.add(new Friends(login));
			boolean exist = false;
			for (PrivateChatWindow privateWindows : privateChatWindows) {
				if (privateWindows.getUserName().equals(login)) {
					exist = true;
				}
			}

			if (!exist) {
				TextArea textArea = new TextArea();
				textArea.clear();
				PrivateChatWindow window = new PrivateChatWindow(login, textArea);
				privateChatWindows.add(window);
				System.out.println("Private window created for: " + login);
			}
			break;
			// userNames.add(new Friends(login));
			// this.currentOnlineUsers.setItems(userNames);
		}
	}

	public boolean connect() {
		// TODO Auto-generated method stub
		try {
			this.socket = new Socket(serverName, serverPort);
			System.out.println("Client Port: " + socket.getLocalPort());
			this.serverOut = socket.getOutputStream();
			this.serverIn = socket.getInputStream();

			this.bufferedIn = new BufferedReader(new InputStreamReader(this.serverIn));
			this.serverIn = socket.getInputStream();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public void addUserSattustListener(UserStatusListener listener) {
		userStatusListeners.add(listener);

	}

	public void removeUserSattustListener(UserStatusListener listener) {
		userStatusListeners.remove(listener);

	}

	public void addMessageListener(MessageListener listener) {
		messageListeners.add(listener);
	}

	public void removeMessageListener(MessageListener listener) {
		messageListeners.remove(listener);
	}

	public void setReceiverTextField(ArrayList<PrivateChatWindow> privateChatWindows) {
		// TODO Auto-generated method stub
		this.privateChatWindows = privateChatWindows;
	}

	public ObservableList<Friends> getuserNames() {
		return userNames;
	}

	public ArrayList<PrivateChatWindow> getPrivateWindowS() {
		return privateChatWindows;
	}

	public void setReceivingWindow(TextArea displayMessage) {
		// TODO Auto-generated method stub
		this.MessageWindow = displayMessage;

	}

	public PrivateChatWindow getGroupChatWindow() {
		// TODO Auto-generated method stub
		return this.groupChatWindow;
	}

	public void passChatWindowController(ChatWindowController chatWindowController) {
		// TODO Auto-generated method stub
		this.myChatWindowController = chatWindowController;

	}

	public void setTabName(Label friendName) {
		// TODO Auto-generated method stub
		this.TabName = friendName;
	}

	public void AddAsFriend(Friends newFriend) throws IOException {
		String add = "add " + newFriend.getUserName() + "\n";
		serverOut.write(add.getBytes());
		friendList.add(newFriend);
	}
	public ObservableList<Friends> getfriendList() {
		// TODO Auto-generated method stub
		return friendList;
	}

	public void setFriendsTable(TableView<Friends> friendsTable) {
		// TODO Auto-generated method stub
		this.FriendsTable = friendsTable;
	}

	public boolean hasFriend(Friends newValue) {
		// TODO Auto-generated method stub
		for(Friends friend: friendList) {
			if(friend.getUserName().equals(newValue.getUserName())) {
				return true;
			}
		}
		return false;
	}

}
