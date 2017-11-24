package client.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import common.constants.Constants;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

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
		Button addFile = new Button("Add a file to the catalog");
		root.add(addFile, 0, 6);
		addFile.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				try {
					FileChooser fileChooser = new FileChooser();
					fileChooser.setTitle("Choose the file to add to the catalog.");
					File file = fileChooser.showOpenDialog(viewManager.getStage());
					List<String> dialogData = new ArrayList<>();
					dialogData.add(Constants.ACCESS_PUBLIC);
					dialogData.add(Constants.ACCESS_PRIVATE);
					ChoiceDialog dialog = new ChoiceDialog(dialogData.get(0), dialogData);
					dialog.setTitle("Access permission");
					dialog.setHeaderText("Select the access permission.");
					Optional<String> result = dialog.showAndWait();
					String access = null;
					if (result.isPresent()) {
						access = result.get();
					}
					if (file != null && access != null) {
						viewManager.getController().sendFile(file);
						if (viewManager.getServer().addFile(file.getName(), file.length(), access)) {
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.setTitle("Upload success");
							alert.setHeaderText(null);
							alert.setContentText("The file has been added to the catalog.");
							alert.showAndWait();
						} else {
							Alert alert = new Alert(AlertType.ERROR);
							alert.setTitle("Upload failure");
							alert.setHeaderText(null);
							alert.setContentText(
									"Impossible to add the file to the catalog.\nMake sure you are logged and try again with another name.");
							alert.showAndWait();
						}
					}
				} catch (Exception exception) {
					exception.printStackTrace();
					System.err.println("File adding to the catalog failed.");
				}
			}
		});

		Button listFiles = new Button("See the catalog");
		root.add(listFiles, 0, 7);
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
