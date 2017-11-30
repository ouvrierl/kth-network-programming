package client.view;

import java.security.MessageDigest;

import client.controller.ClientManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

public class Register {

	private Scene scene;

	public Register(ClientManager viewManager) {
		GridPane root = new GridPane();
		root.setAlignment(Pos.CENTER);
		root.setHgap(10);
		root.setVgap(10);
		root.setPadding(new Insets(25, 25, 25, 25));

		Label intro = new Label("Register");
		root.add(intro, 1, 0);

		Label username = new Label("Usename");
		root.add(username, 0, 1);

		TextField usernameValue = new TextField();
		root.add(usernameValue, 1, 1);

		Label password = new Label("Password");
		root.add(password, 0, 2);

		PasswordField passwordValue = new PasswordField();
		root.add(passwordValue, 1, 2);

		Button register = new Button("Register");
		root.add(register, 1, 3);
		register.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				try {
					String username = usernameValue.getText();
					String password = passwordValue.getText();
					String passwordHashed = null;
					if (!username.equals("") && !password.equals("")) {
						try {
							MessageDigest md = MessageDigest.getInstance("MD5");
							md.update(password.getBytes());
							byte[] bytes = md.digest();
							StringBuilder sb = new StringBuilder();
							for (int i = 0; i < bytes.length; i++) {
								sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
							}
							passwordHashed = sb.toString();
						} catch (Exception exception) {
							System.err.println("Error while hashing password.");
						}
						if (viewManager.getServer().register(viewManager.getServerReader(), username, passwordHashed)) {
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.setTitle("Register success");
							alert.setHeaderText(null);
							alert.setContentText("You are now registered.\nYou can login.");
							alert.showAndWait();
							Home home = new Home(viewManager);
							Scene homeScene = home.getScene();
							viewManager.getStage().setScene(homeScene);
						} else {
							Alert alert = new Alert(AlertType.ERROR);
							alert.setTitle("Register failure");
							alert.setHeaderText(null);
							alert.setContentText(
									"Impossible to register.\nThe username/password is empty or the username already exists, please choose another one.\n Make sure that you are not already logged.");
							alert.showAndWait();
							usernameValue.setText("");
							passwordValue.setText("");
						}
					}
				} catch (Exception exception) {
					System.err.println("Register request failed.");
				}
			}
		});

		Image imageHome = new Image(getClass().getResourceAsStream("./home.png"), 25, 25, true, false);
		Button home = new Button("", new ImageView(imageHome));
		root.add(home, 1, 6);
		home.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Home home = new Home(viewManager);
				Scene homeScene = home.getScene();
				viewManager.getStage().setScene(homeScene);
			}
		});

		this.scene = new Scene(root, 400, 400);
		this.scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

	}

	public Scene getScene() {
		return this.scene;
	}

}
