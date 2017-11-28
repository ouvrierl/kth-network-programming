package client.view;

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

public class Delete extends TableCell<CatalogFile, Boolean> {

	private Button delete;
	private ViewManager viewManager;

	public Delete(ViewManager viewManager) {

		this.viewManager = viewManager;

		Image imageHome = new Image(getClass().getResourceAsStream("./delete.jpg"), 25, 25, true, false);
		this.delete = new Button("", new ImageView(imageHome));

		this.delete.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				try {
					CatalogFile file = getTableView().getItems().get(getIndex());
					String fileName = file.getName();
					if (viewManager.getServer().removeFile(viewManager.getServerReader(), fileName)) {
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Delete success");
						alert.setHeaderText(null);
						alert.setContentText("The file has been deleted on the server.");
						alert.showAndWait();
						List<Object[]> filesList = viewManager.getServer().getFiles(viewManager.getServerReader());
						ListFiles listUpdated = new ListFiles(viewManager, filesList);
						Scene sceneUpdated = listUpdated.getScene();
						viewManager.getStage().setScene(sceneUpdated);
					} else {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Delete failure");
						alert.setHeaderText(null);
						alert.setContentText(
								"Impossible to delete the file on the server, please make sure you have the rights and try again.");
						alert.showAndWait();
					}
				} catch (Exception e) {
					System.out.println("Error while deleting the file on the server.");
				}

			}
		});
	}

	@Override
	protected void updateItem(Boolean t, boolean empty) {
		super.updateItem(t, empty);
		if (!empty) {
			this.setGraphic(this.delete);
		}
	}
}
