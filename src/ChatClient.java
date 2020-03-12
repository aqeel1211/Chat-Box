import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ChatClient {
	private final String serverName;
	private final int serverPort;
	private Socket socket;
	private OutputStream serverOut;
	private InputStream serverIn;
	private BufferedReader bufferedIn;
	
	private String userName,password;
	
	private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
	private ArrayList<MessageListener> messageListeners = new ArrayList<>();

	
	
	public ChatClient(String serverName, int serverPort) {
		// TODO Auto-generated constructor stub
	//	read = new Scanner (System.in);
		this.serverName=serverName;
		this.serverPort=serverPort;
	}

	public static void main(String[] args) throws IOException {
		ChatClient client = new ChatClient("localhost", 8813);
		client.addUserSattustListener(new UserStatusListener(){
			@Override
			public void online(String login) {
				System.out.println("ONLINE: "+  login);
			}
			@Override
			public void offline(String login) {
				System.out.println("OFFLINE: " + login);
			}
		});
		
		
		client.addMessageListener(new MessageListener(){
			@Override 
			public void onMessage(String fromLogin, String msgBody) {
				System.out.println("You got a message from "+ fromLogin + "===>" + msgBody);
			}
		});
		
		if(!client.connect()) {
			System.err.println("Connect failed.");
		}
		else {
			System.out.println("Connect successful");
			
			System.out.println("Enter your username: ");
			Scanner read = new Scanner(System.in);
			String userName = read.nextLine();
			System.out.println("Enter your password: ");
			String password = read.nextLine();
			if(client.logIn(userName,password)) {
				client.userName = userName;
				client.password = password;
				System.out.println("Login Successful");
				client.msg("guest","Hello world");
			}
			else {
				System.out.println("Login Failed");
			}
		//	client.logoff();
		
			
		}
	}

	private void logoff() throws IOException {
		// TODO Auto-generated method stub
		String cmd = "logoff\n";
		serverOut.write(cmd.getBytes());
	}

	private void msg(String sendTo, String msgBody) throws IOException {
		// TODO Auto-generated method stub
		String cmd = "msg "+sendTo + " " + msgBody +"\n";
		serverOut.write(cmd.getBytes());
	}

	/*private boolean sendMsg(String msg) throws IOException {             -> needs update
		// TODO Auto-generated method stub
		if(msg.equalsIgnoreCase("quit") || msg.equalsIgnoreCase("logoff")) {
			serverOut.write(msg.getBytes());
			return true;
		}
		else {
			serverOut.write(msg.getBytes());
			return false;
		}
	}*/

	private boolean logIn(String login, String password) throws IOException {
		// TODO Auto-generated method stub
		String cmd = "login "+ login + " " + password + "\n";
		serverOut.write(cmd.getBytes());
		
		
		String response = bufferedIn.readLine();
		System.out.println("Response from server: " + response);
		
		if("ok login".equalsIgnoreCase(response)) {
			startMessageReader();
			return true;
		}
		else
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
		Thread t = new  Thread() {
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
			while((line = bufferedIn.readLine())!=null) {
		//	System.out.println("Line: "+line);
				String []tokens1 =null;	
				Pattern pattern = Pattern.compile(" ");
				tokens1= pattern.split(line);
			//	System.out.println("Response: " + tokens1[1]);
				if(tokens1!=null && tokens1.length>0) {
					String  cmd = tokens1[0];           // first word is at index 1
				//	System.out.println("CMD: "+ cmd);
					
					if("online".equalsIgnoreCase(cmd)) {
						//System.out.println("Online present");
						handleOnline(tokens1);
					}
					else if("offline".equalsIgnoreCase(cmd)) {
					//	System.out.println("Offline");
						handleOffline(tokens1);
					}
					else if("msg".equalsIgnoreCase(cmd)) {
						
						String[]tokenMsg = line.split(" ", 3);
						handleMessage(tokenMsg);
					}
					
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			try {
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private void handleMessage(String[] tokenMsg) {
		// TODO Auto-generated method stub
		String login = tokenMsg[1];
		String msgBody = tokenMsg[2];
		
		for(MessageListener listener: messageListeners) {
			listener.onMessage(login, msgBody);
		}
	}

	private void handleOffline(String[] tokens) {
		// TODO Auto-generated method stub
		String login = tokens[1];
		for(UserStatusListener listener: userStatusListeners) {
			listener.offline(login);
		}
	}

	private void handleOnline(String[] tokens) {
		// TODO Auto-generated method stub
		String login = tokens[1];
		for(UserStatusListener listener: userStatusListeners) {
			listener.online(login);
		}
	}

	private boolean connect() {
		// TODO Auto-generated method stub
		try {
			this.socket = new Socket(serverName,serverPort);
			System.out.println("Client Port: " +  socket.getLocalPort());
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
}
