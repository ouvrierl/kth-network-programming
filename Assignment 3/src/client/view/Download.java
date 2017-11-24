package client.view;

import java.io.File;
import java.rmi.RemoteException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class Download extends TableCell<CatalogFile, Boolean> {

	private Button download;
	private ViewManager viewManager;

	public Download(ViewManager viewManager) {

		this.viewManager = viewManager;

		Image imageHome = new Image(getClass().getResourceAsStream("./download.jpg"), 25, 25, true, false);
		this.download = new Button("", new ImageView(imageHome));

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
					viewManager.getController().getServerConnection().setDownloadFile(fileToDownload);
					viewManager.getServer().downloadFile(fileName);
				} catch (RemoteException e) {
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
