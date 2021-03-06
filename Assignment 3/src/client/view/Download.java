package client.view;

import java.io.File;
import java.rmi.RemoteException;

import client.controller.ClientManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class Download extends TableCell<CatalogFile, Boolean> {

	private Button download;
	private ClientManager viewManager;

	public Download(ClientManager viewManager) {

		this.viewManager = viewManager;

		Image imageDownload = new Image(getClass().getResourceAsStream("./download.jpg"), 25, 25, true, false);
		this.download = new Button("", new ImageView(imageDownload));

		this.download.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				try {
					CatalogFile file = getTableView().getItems().get(getIndex());
					String fileName = file.getName();
					FileChooser fileChooser = new FileChooser();
					fileChooser.setTitle("Choose where to download the file.");
					fileChooser.setInitialFileName(fileName);
					File fileToDownload = fileChooser.showSaveDialog(viewManager.getStage());
					if (fileToDownload != null) {
						viewManager.getController().getServerConnection().setDownloadFile(fileToDownload);
						if (viewManager.getServer().downloadFile(viewManager.getServerReader(), fileName)) {
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.setTitle("Download success");
							alert.setHeaderText(null);
							alert.setContentText("The file has been downloaded from the server.");
							alert.showAndWait();
						} else {
							Alert alert = new Alert(AlertType.ERROR);
							alert.setTitle("Download failure");
							alert.setHeaderText(null);
							alert.setContentText("Impossible to download the file from the server, please try again.");
							alert.showAndWait();
						}
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					System.err.println("Error while trying to download file.");
				}
			}
		});
	}

	@Override
	protected void updateItem(Boolean t, boolean empty) {
		super.updateItem(t, empty);
		if (!empty) {
			this.setGraphic(this.download);
		}
	}
}
