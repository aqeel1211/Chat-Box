package com.windows.vimo.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import com.sun.glass.ui.Window;
import com.windows.vimo.translator.GoogleTranslate;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ChatWindowController {
	@FXML
	public TableView<Friends> friendsTable;

	@FXML
	private TableColumn<Friends, String> chatsColumn;

	@FXML
	private TextArea displayMessage;

	boolean shifted = true; // true if it is personal chat, false if it's group chat
	@FXML
	private Button ShiftButton;

	@FXML
	private TableView<Friends> onlineUsers;

	@FXML
	private TableColumn<Friends, String> OnlineUsers;

	@FXML
	private Label friendName;

	@FXML
	private Button sendButton;

	@FXML
	private TextField messageToBeSent;

	private TextField groupText = new TextField();

	@FXML
	private ImageView audioCall;

	@FXML
	private ImageView videoCall;

	@FXML
	private Button signoutBtn;

	@FXML
	private Label userID;

	private String currentFriendTab = null;
	private Client client;
	private String msg;

	@FXML
	private ImageView attachment;
	private ChatWindowMain mainApp;

	private PrivateChatWindow groupChatWindow;
	private ArrayList<PrivateChatWindow> privateChatWindows; // this will hold messages of each user in different window

	public void showFriendChatWindow(Friends userName) {

		if (userName != null) {

			for (PrivateChatWindow window : privateChatWindows) {
				if (window.getUserName().equals(userName.getUserName())) {
					displayMessage.clear(); // clears the current field
					displayMessage.appendText(window.getFriendsWindow().getText()); // appends the previous text of the
																					// current user tab window
				}
			}
			friendName.setText(userName.getUserName());
			setCurrentUserTab(userName.getUserName());
		}
		// temp=displayMessage;
		else {

			friendName.setText("");
			setCurrentUserTab(userName.getUserName());

		}

		// friendName.setText(userName.getUserName());
		//
	}

	public void setCurrentUserTab(String userName) {
		// TODO Auto-generated method stub
		this.currentFriendTab = userName;
	}

	public String getCurrentFriendTab() {
		return this.currentFriendTab;
	}

	@FXML
	public void shiftWindow() {
		if (shifted) {
			ShiftButton.setText("Go To Personal Chat");
			friendName.setText("");
			shifted = false;
			displayMessage.clear();
			for (PrivateChatWindow window : privateChatWindows) {
				if (window.getUserName().equals("groupChat")) {
					displayMessage.appendText(window.getFriendsWindow().getText());
					break;
				}
			}
			this.messageToBeSent.setText(null);
			this.messageToBeSent.clear();

		} else if (!shifted) {
			shifted = true;
			ShiftButton.setText("Go To Group Chat");
			friendName.setText("friend Name");
			displayMessage.clear();
		}

	}

	@FXML
	private void initialize() {

		chatsColumn.setCellValueFactory(cellData -> cellData.getValue().userNameProperty());

		
		OnlineUsers.setCellValueFactory(cellData -> cellData.getValue().userNameProperty());
		
		// showFriendChatWindow(null);
		// if(temp!=null) {
		// displayMessage=temp;
		// }
		// displayMessage.setText("");
		// Listen for selection changes and show the person details when changed.
		if (shifted) {
			onlineUsers.getSelectionModel().selectedItemProperty()
					.addListener((observable, oldValue, newValue) -> showFriendChatWindow(newValue));
		}

			friendsTable.getSelectionModel().selectedItemProperty()
			.addListener((observable, oldValue, newValue) -> AddFriend(newValue));
	}

	public  void AddFriend(Friends newValue) {
		// TODO Auto-generated method stub
		if(!this.mainApp.getClient().hasFriend(newValue)) {
			try {
				this.mainApp.getClient().AddAsFriend(newValue);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.onlineUsers.setItems(this.mainApp.getClient().getfriendList());
		
		}
	}

	@FXML
	public void refreshTable() {
		friendsTable.setItems(mainApp.getClient().getuserNames());
		this.privateChatWindows = mainApp.getClient().getPrivateWindowS();
		System.out.println("Refresh clicked");
	}

	@FXML
	public void fileTransferClicked() throws IOException {

		if (this.getCurrentFriendTab() != null) {
			Stage stage = new Stage();
			FileChooser filechoose = new FileChooser();
			File file = filechoose.showOpenDialog(stage);

			if (file != null) {
				String path = file.getAbsolutePath();
				String filename = file.getName();

				System.out.println("File path: " + path);
				System.out.println("File name: " + filename);

				for (PrivateChatWindow privateWindow : privateChatWindows) {
					if (privateWindow.getUserName().equals(getCurrentFriendTab())) {

						displayMessage.clear(); // clearing the field

						privateWindow.getFriendsWindow().appendText("\n" + filename + " sent successfully");
						displayMessage.appendText(privateWindow.getFriendsWindow().getText()); // appending the previous
																								// messages with the new
																								// one

						this.client = mainApp.getClient();
						// boolean msgType = true; // if it's a personal messsage
						int opt = 3;
						this.client.msg(getCurrentFriendTab(), path, opt, filename);

						break;

					}
				}

			}
		}
	}

	@FXML
	public void sendButtonPressed() {

		if (shifted == true) { // if personal chat is open

			if (getCurrentFriendTab() != null && messageToBeSent.getText() != null) {
				// displayMessage.setText("\n");
				// int lengthBefore = displayMessage.getText().length();
				System.out.println("Personal chat opened");
				for (PrivateChatWindow privateWindow : privateChatWindows) {
					if (privateWindow.getUserName().equals(getCurrentFriendTab())) {
						msg = messageToBeSent.getText();

						displayMessage.clear(); // clearing the field
						if (messageToBeSent.getText() != null && messageToBeSent.getText() != "") {
							privateWindow.getFriendsWindow()
									.appendText((messageToBeSent.getText().isEmpty() ? "" : "\n") + "You" + "->"
											+ messageToBeSent.getText());
							displayMessage.appendText(privateWindow.getFriendsWindow().getText()); // appending the
																									// previous messages
																									// with the new one

							this.client = mainApp.getClient();
							try {
								int msgType = 1; // if it's a personal messsage
								this.client.msg(getCurrentFriendTab(), msg, msgType, "dump");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							messageToBeSent.setText(null);
							// displayMessage.setStyle(lengthBefore, (lengthBefore +
							// 6),"-fx-font-weight:bold; -fx-font-size:14px; -fx-fill:maroon;");
							messageToBeSent.clear();
							System.out.println(getCurrentFriendTab() + " is on");
							break;

						}

					}

				}

			} else {
				System.out.println("something was not written");
			}

		} else if (!shifted) { // if it's group chat window
			if (!messageToBeSent.getText().isEmpty()) {
				for (PrivateChatWindow privateWindow : privateChatWindows) {
					if (privateWindow.getUserName().equals("groupChat")) {
						msg = messageToBeSent.getText(); // call the function of translation api and translate the msg
															// variable
						try {
							msg = GoogleTranslate.translate(msg);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						displayMessage.clear(); // clearing the field
						if (messageToBeSent.getText() != null && messageToBeSent.getText() != "") {
							privateWindow.getFriendsWindow()
									.appendText((msg.isEmpty() ? "" : "\n") + "You" + "->" + msg);
							displayMessage.appendText(privateWindow.getFriendsWindow().getText()); // appending the
																									// previous messages
																									// with the new one

							this.client = mainApp.getClient();
							try {
								int msgType = 2; // if it's a group
								this.client.msg("DumpString", msg, msgType, "dump");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							messageToBeSent.setText(null);
							// displayMessage.setStyle(lengthBefore, (lengthBefore +
							// 6),"-fx-font-weight:bold; -fx-font-size:14px; -fx-fill:maroon;");
							messageToBeSent.clear();
							break;

						}

					}
				}

			} else {
				System.out.println("It's empty");
				// displayMessage.appendText("hallo");
			}
		}

	}

	@FXML
	public void LogOff() {
		try {
			this.mainApp.getClient().logoff();
			this.mainApp.getPrimaryStage().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setTabName(String newText) {
		friendName.setText(newText);
	}

	public Label getTabName() {
		return friendName;
	}

	public void setMainApp(ChatWindowMain main) {
		// TODO Auto-generated method stub
		this.mainApp = main;
		friendsTable.setItems(mainApp.getClient().getuserNames());
		this.privateChatWindows = mainApp.getClient().getPrivateWindowS();
		// this.mainApp.getClient().setCurrentOnlineUsers(this.friendsTable);
		// this.mainApp.getClient().setCurrentUsersTable(this.friendsTable);
		this.mainApp.getClient().setReceivingWindow(this.displayMessage);
		this.userID.setText(this.mainApp.getClient().userName);
		this.mainApp.getClient().passChatWindowController(this);
		this.mainApp.getClient().setTabName(this.friendName);
		this.mainApp.getClient().setFriendsTable(this.onlineUsers);

	}

}
