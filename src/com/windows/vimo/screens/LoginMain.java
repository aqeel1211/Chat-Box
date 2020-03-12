package com.windows.vimo.screens;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;

public class LoginMain extends Application {
	private Stage primaryStage;

	@Override
	public void start(Stage primaryStage) {
		try {

			this.primaryStage = primaryStage;
			this.primaryStage.setTitle("Login Page");

			initStage();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initStage() {
		// Parent root = null;
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(LoginMain.class.getResource("Login.fxml"));
			AnchorPane logIn = (AnchorPane) loader.load();
			Scene scene = new Scene(logIn);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			LogInController controller = loader.getController();
			controller.setMainApp(this);

			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		launch(args);
	}

	public Stage getPrimaryStage() {
		// TODO Auto-generated method stub
		return primaryStage;
	}
}
