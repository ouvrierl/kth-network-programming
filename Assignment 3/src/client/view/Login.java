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

public class Login {

	private Scene scene;

	public Login(ViewManager viewManager) {
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
				try {
					String username = usernameValue.getText();
					String password = passwordValue.getText();
					if (viewManager.getServer().login(viewManager.getServerReader(), username, password)) {
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Login success");
						alert.setHeaderText(null);
						alert.setContentText("You are now logged.");
						alert.showAndWait();
						Home home = new Home(viewManager);
						Scene homeScene = home.getScene();
						viewManager.getStage().setScene(homeScene);
					} else {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Login failure");
						alert.setHeaderText(null);
						alert.setContentText(
								"Impossible to login.\nMake sure the username and password are correct, and try again.\nBeware that you must register before login.\nMake sure you are not already logged and that this account isn't accessed by another client.");
						alert.showAndWait();
						usernameValue.setText("");
						passwordValue.setText("");
					}
				} catch (Exception exception) {
					System.err.println("Login request failed.");
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
