package client.view;

import java.io.File;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class Update extends TableCell<CatalogFile, Boolean> {

	private Button update;
	private ViewManager viewManager;

	public Update(ViewManager viewManager) {

		this.viewManager = viewManager;

		Image imageHome = new Image(getClass().getResourceAsStream("./update.jpeg"), 25, 25, true, false);
		this.update = new Button("", new ImageView(imageHome));

		this.update.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				try {
					CatalogFile file = getTableView().getItems().get(getIndex());
					String fileName = file.getName();
					FileChooser fileChooser = new FileChooser();
					fileChooser.setTitle("Choose the file to update to the catalog.");
					File fileToUpdate = fileChooser.showOpenDialog(viewManager.getStage());
					if (fileToUpdate != null) {
						if (viewManager.getServer().updateFile(fileName)) {
							viewManager.getController().sendFile(fileToUpdate, fileName);
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.setTitle("Update success");
							alert.setHeaderText(null);
							alert.setContentText("The file has been updated on the server.");
							alert.showAndWait();
							List<Object[]> filesList = viewManager.getServer().getFiles();
							ListFiles listUpdated = new ListFiles(viewManager, filesList);
							Scene sceneUpdated = listUpdated.getScene();
							viewManager.getStage().setScene(sceneUpdated);
						} else {
							Alert alert = new Alert(AlertType.ERROR);
							alert.setTitle("Update failure");
							alert.setHeaderText(null);
							alert.setContentText(
									"Impossible to update the file on the server, please make sure you have the rights and try again.");
							alert.showAndWait();
						}
					}
				} catch (Exception e) {
					System.err.println("Error while updating file on server.");
				}
			}
		});
	}

	@Override
	protected void updateItem(Boolean t, boolean empty) {
		super.updateItem(t, empty);
		if (!empty) {
			this.setGraphic(this.update);
		}
	}
}
