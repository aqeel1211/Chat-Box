package com.windows.vimo.screens;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.windows.vimo.screens.LoginMain;
import com.windows.vimo.client.ChatWindowMain;
import com.windows.vimo.client.Client;
import com.windows.vimo.client.MessageListener;
import com.windows.vimo.client.UserStatusListener;
import com.windows.vimo.database.*;

public class LogInController {

	@FXML
	private Button signUpBtn;

	@FXML
	private TextField username;

	@FXML
	private TextField password;

	@FXML
	private Button logInBtn1;

	Connection conn; // for connecting to the database
	private LoginMain mainApp;

	private SignUpMain signUpMain;
	private ChatWindowMain chatWindowMain;
	Client newClient;
	@FXML
	public void signUpPressed() {
		/*
		 * try { FXMLLoader loader = new FXMLLoader();
		 * loader.setLocation(SignUpMain.class.getResource("Sign.fxml")); AnchorPane
		 * signUp = (AnchorPane) loader.load(); Scene scene = new Scene(signUp);
		 * scene.getStylesheets().add(getClass().getResource("application.css").
		 * toExternalForm()); Stage secondStage=new Stage();
		 * signUpMain.setPrimaryStage(secondStage); SignUpController controller =
		 * loader.getController(); controller.setMainApp(signUpMain);
		 * 
		 * //secondStage.setScene(scene); //secondStage.show();
		 * 
		 * //Stage stage = new Stage();
		 * 
		 * 
		 * // signUpMain.start(stage); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); }
		 */
		mainApp.getPrimaryStage().close();
		Stage secondStage = new Stage();
		signUpMain = new SignUpMain();
		// signUpMain.setPrimaryStage(secondStage);
		signUpMain.start(secondStage);
	}

	@FXML
	public void logInPressed() throws SQLException, IOException {
		String user = username.getText();
		String pass = password.getText();

		if (!(user.equals("")) && !(pass.equals(""))) {
		newClient = new Client("localhost", 8813);
		
		newClient.addUserSattustListener(new UserStatusListener() {
			@Override
			public void online(String login) {
				System.out.println("ONLINE: " + login);
			}

			@Override
			public void offline(String login) {
				System.out.println("OFFLINE: " + login);
			}
		});

		newClient.addMessageListener(new MessageListener() {
			@Override
			public void onMessage(String fromLogin, String msgBody) {
				System.out.println(fromLogin + "==>" + msgBody);
			}
		});
		
		
		if (!newClient.connect()) {
			System.err.println("Connect failed.");
		} else {
			System.out.println("Connected successfully");
			if (newClient.logIn(user, pass)) {
				newClient.userName = user;
				newClient.password = pass;
				System.out.println("Login Successful");
				// client.sendDetails(client.userName,client.password);
				// client.msg("guest","Hello world");
				
				mainApp.getPrimaryStage().close();
				Stage fourthStage = new Stage();
				chatWindowMain = new ChatWindowMain();
				// signUpMain.setPrimaryStage(secondStage);
				try {
					mainApp.getPrimaryStage().close();
					chatWindowMain.start(fourthStage, this.newClient);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.initOwner(mainApp.getPrimaryStage());
				alert.setTitle("Error");
				alert.setHeaderText("Wrong username or password");
				alert.showAndWait();
			}

		}

	}
		else {
			// Nothing selected.
			Alert alert = new Alert(AlertType.WARNING);
			alert.initOwner(mainApp.getPrimaryStage());
			alert.setTitle("Error");
			alert.setHeaderText("Empty Fields");
			alert.setContentText("Please fill the specified fields.");

			alert.showAndWait();
		}

	}

	public void setMainApp(LoginMain main) {
		// TODO Auto-generated method stub
		this.mainApp = main;
	}

}
