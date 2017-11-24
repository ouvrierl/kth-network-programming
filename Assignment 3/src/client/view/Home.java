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

public class Home {

	private Scene scene;

	public Home(ViewManager viewManager) {

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
				Login login = new Login(viewManager);
				Scene loginScene = login.getScene();
				viewManager.getStage().setScene(loginScene);
			}
		});

		Button logout = new Button("Logout");
		root.add(logout, 0, 3);
		logout.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				try {
					if (viewManager.getServer().logout()) {
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Logout success");
						alert.setHeaderText(null);
						alert.setContentText("You are now logged out.");
						alert.showAndWait();
					} else {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Logout failure");
						alert.setHeaderText(null);
						alert.setContentText("Impossible to logout.\nMake sure you are logged.");
						alert.showAndWait();
					}
				} catch (Exception exception) {
					System.err.println("Error while trying to log out.");
				}
			}
		});

		Button register = new Button("Register");
		root.add(register, 0, 4);
		register.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Register register = new Register(viewManager);
				Scene registerScene = register.getScene();
				viewManager.getStage().setScene(registerScene);
			}
		});

		Button unregister = new Button("Unregister");
		root.add(unregister, 0, 5);
		unregister.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				try {
					if (viewManager.getServer().unregister()) {
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Unregister success");
						alert.setHeaderText(null);
						alert.setContentText("You are now unregistered.");
						alert.showAndWait();
					} else {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Unregister failure");
						alert.setHeaderText(null);
						alert.setContentText("Impossible to unregister.\nMake sure you are logged.");
						alert.showAndWait();
					}
				} catch (Exception exception) {
					System.err.println("Unregister request failed.");
				}
			}
		});

		Button listFiles = new Button("See the catalog");
		root.add(listFiles, 0, 6);
		listFiles.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				ListFiles files = new ListFiles(viewManager);
				Scene sceneFiles = files.getScene();
				viewManager.getStage().setScene(sceneFiles);
			}
		});

		this.scene = new Scene(root, 400, 400);
		this.scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	}

	public Scene getScene() {
		return this.scene;
	}

}
