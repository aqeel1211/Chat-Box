package com.windows.vimo.client;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Friends {

	private final StringProperty userName;

	public Friends() {
		this(null);
	}

	public Friends(String userName) {
		this.userName = new SimpleStringProperty(userName);

	}

	public String getUserName() {
		return userName.get();
	}

	public void setUserName(String userName) {
		this.userName.set(userName);
	}

	public StringProperty userNameProperty() {
		return userName;
	}

}
