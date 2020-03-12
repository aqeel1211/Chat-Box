package com.windows.vimo.screens;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;

public class SignUpMain extends Application {
	private Stage primaryStage;

	@Override
	public void start(Stage primaryStage) {
		try {

			this.primaryStage = primaryStage;
			this.primaryStage.setTitle("Sign up page");

			initStage();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initStage() {
		// Parent root = null;
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(SignUpMain.class.getResource("Sign.fxml"));
			AnchorPane signUp = (AnchorPane) loader.load();
			Scene scene = new Scene(signUp);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			SignUpController controller = loader.getController();
			controller.setMainApp(this);

			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	/*
	 * public static void main(String[] args) { launch(args); }
	 */

	public void setPrimaryStage(Stage stage) {
		this.primaryStage = stage;
	}

	public Stage getPrimaryStage() {
		// TODO Auto-generated method stub
		return primaryStage;
	}

}
