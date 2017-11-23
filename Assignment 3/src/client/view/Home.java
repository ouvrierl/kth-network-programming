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
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Home {

	private Scene scene;

	public Home(Stage primaryStage) {
		GridPane root = new GridPane();
		root.setAlignment(Pos.CENTER);
		root.setHgap(10);
		root.setVgap(10);
		root.setPadding(new Insets(25, 25, 25, 25));

		Label intro = new Label("Welcome to the catalog");
		root.add(intro, 0, 0);

		Button login = new Button("Login");
		root.add(login, 0, 2);
		login.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Login login = new Login(primaryStage);
				Scene loginScene = login.getScene();
				primaryStage.setScene(loginScene);
			}
		});

		Button logout = new Button("Logout");
		root.add(logout, 0, 3);
		logout.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Logout");
				alert.setHeaderText(null);
				alert.setContentText("You are now logged out.");
				alert.showAndWait();
			}
		});

		Button register = new Button("Register");
		root.add(register, 0, 4);
		register.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Register register = new Register(primaryStage);
				Scene registerScene = register.getScene();
				primaryStage.setScene(registerScene);
			}
		});

		Button unregister = new Button("Unregister");
		root.add(unregister, 0, 5);
		unregister.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Unregister");
				alert.setHeaderText(null);
				alert.setContentText("You are now unregistered.");
				alert.showAndWait();
			}
		});

		this.scene = new Scene(root, 400, 400);
		this.scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	}

	public Scene getScene() {
		return this.scene;
	}

}
