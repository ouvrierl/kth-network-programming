package client.view;

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
import javafx.stage.Stage;

public class Login {

	private Scene scene;

	public Login(Stage primaryStage) {
		GridPane root = new GridPane();
		root.setAlignment(Pos.CENTER);
		root.setHgap(10);
		root.setVgap(10);
		root.setPadding(new Insets(25, 25, 25, 25));

		Label intro = new Label("Login");
		root.add(intro, 1, 0);

		Label username = new Label("Usename");
		root.add(username, 0, 1);

		TextField usernameValue = new TextField();
		root.add(usernameValue, 1, 1);

		Label password = new Label("Password");
		root.add(password, 0, 2);

		PasswordField passwordValue = new PasswordField();
		root.add(passwordValue, 1, 2);

		Button login = new Button("Login");
		root.add(login, 1, 3);
		login.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				String username = usernameValue.getText();
				String password = passwordValue.getText();
				if (username.equals("username") && password.equals("password")) {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Login success");
					alert.setHeaderText(null);
					alert.setContentText("You are now logged.");
					alert.showAndWait();
					Home home = new Home(primaryStage);
					Scene homeScene = home.getScene();
					primaryStage.setScene(homeScene);
				} else {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Login failure");
					alert.setHeaderText(null);
					alert.setContentText(
							"Wrong username or password, please try again.\nYou must register before login.");
					alert.showAndWait();
					usernameValue.setText("");
					passwordValue.setText("");
				}
			}
		});

		Image imageHome = new Image(getClass().getResourceAsStream("./home.png"), 50, 50, true, false);
		Button home = new Button("Home", new ImageView(imageHome));
		root.add(home, 1, 6);
		home.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Home home = new Home(primaryStage);
				Scene homeScene = home.getScene();
				primaryStage.setScene(homeScene);
			}
		});

		this.scene = new Scene(root, 400, 400);
		this.scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

	}

	public Scene getScene() {
		return this.scene;
	}

}