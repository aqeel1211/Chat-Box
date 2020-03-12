package com.windows.vimo.screens;

import com.windows.vimo.database.*;
import java.sql.Connection;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class SignUpController {

	@FXML
	private TextField password;

	@FXML
	private Button signUpBtn1;

	@FXML
	private Label confirmationID;

	@FXML
	private TextField username;

	private SignUpMain mainApp;

	public void signUp() throws SQLException {
		DBWorker obj = new DBWorker();
		// obj.run();
		Connection conn = null;
		try {
			conn = obj.getConnection();
			System.out.println("connection name is :: " + conn.getClass().getName());
			System.out.println("Connected to database\n");
		} catch (SQLException e) {
			System.out.println("ERROR: Could not connect to the database");
			e.printStackTrace();
			return;
		}
		// .addUserInTable(conn, username, password);
		String user = username.getText();
		String pass = password.getText();

		if (!(user.equals("")) && !(pass.equals(""))) {
			System.out.println("New User:");
			System.out.println("Username: " + username.getText());
			System.out.println("Password: " + password.getText());
			if (obj.addUserInTable(conn, user, pass)) {
				this.confirmationID.setText("Registered Successfully");
			} else {
				this.confirmationID.setText("Account already exists");
				this.confirmationID.setTextFill(Color.web("#ff5757"));
				username.clear();
				password.clear();
			}

		} else {
			// Nothing selected.
			Alert alert = new Alert(AlertType.WARNING);
			alert.initOwner(mainApp.getPrimaryStage());
			alert.setTitle("Error");
			alert.setHeaderText("Empty Fields");
			alert.setContentText("Please fill the specified fields.");

			alert.showAndWait();
		}

	}

	public void setMainApp(SignUpMain mainApp) {
		// TODO Auto-generated method stub
		this.mainApp = mainApp;
	}

}
