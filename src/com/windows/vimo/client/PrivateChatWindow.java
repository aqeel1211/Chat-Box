package com.windows.vimo.client;

import javafx.scene.control.TextArea;

public class PrivateChatWindow {

	private String friendName;
	private TextArea friendWindow;

	public PrivateChatWindow(String userName, TextArea friend) {
		super();
		this.friendName = userName;
		this.friendWindow = friend;
	}

	public PrivateChatWindow() {
		// TODO Auto-generated constructor stub
	}

	public PrivateChatWindow(TextArea textArea) {
		// TODO Auto-generated constructor stub
		this.friendWindow = textArea;
	}

	public String getUserName() {
		return friendName;
	}

	public void setUserName(String userName) {
		this.friendName = userName;
	}

	public TextArea getFriendsWindow() {
		return friendWindow;
	}

}
