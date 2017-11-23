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

public class Register {

	private Scene scene;

	public Register(Stage primaryStage) {
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
				String username = usernameValue.getText();
				String password = passwordValue.getText();
				if (!username.equals("username")) {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Register success");
					alert.setHeaderText(null);
					alert.setContentText("You are now registered.\nYou can login.");
					alert.showAndWait();
					Home home = new Home(primaryStage);
					Scene homeScene = home.getScene();
					primaryStage.setScene(homeScene);
				} else {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Register failure");
					alert.setHeaderText(null);
					alert.setContentText("The username already exists, please choose another one.");
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
