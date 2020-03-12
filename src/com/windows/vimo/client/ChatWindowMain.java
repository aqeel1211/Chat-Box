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

import org.hamcrest.core.Is;

import com.windows.vimo.screens.LoginMain;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ChatWindowMain extends Application {
	private Stage primaryStage;
	private Client newClient;
	private String user, pass;

	public void start(Stage primaryStage, Client newClient2) {
		try {

			this.newClient = newClient2;
			

			
			this.primaryStage = primaryStage;
			this.primaryStage.setTitle(newClient.userName);

			initStage();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initStage() {
		// Parent root = null;
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(LoginMain.class.getResource("PersonalChatWindow.fxml"));
			AnchorPane chatWindow = (AnchorPane) loader.load();
			Scene scene = new Scene(chatWindow);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			ChatWindowController controller = loader.getController();
			controller.setMainApp(this);

			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void setPrimaryStage(Stage stage) {
		this.primaryStage = stage;
	}

	public Stage getPrimaryStage() {
		// TODO Auto-generated method stub
		return primaryStage;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub

	}

	public Client getClient() {
		return this.newClient;
	}

}

