package client.view;

import client.controller.ClientManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Notify extends TableCell<CatalogFile, Boolean> {

	private Button notify;
	private ClientManager viewManager;

	public Notify(ClientManager viewManager) {

		this.viewManager = viewManager;

		Image imageNotify = new Image(getClass().getResourceAsStream("./notify.jpg"), 25, 25, true, false);
		this.notify = new Button("", new ImageView(imageNotify));

		this.notify.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				try {
					CatalogFile file = getTableView().getItems().get(getIndex());
					String fileName = file.getName();
					if (viewManager.getServer().notifyFile(viewManager.getServerReader(), fileName)) {
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Notify success");
						alert.setHeaderText(null);
						alert.setContentText("You are now notified for the file.");
						alert.showAndWait();
					} else {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Notify error");
						alert.setHeaderText(null);
						alert.setContentText(
								"Impossible to launch the notification.\nYou need to be the owner of the file.");
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
			this.setGraphic(this.notify);
		}
	}
}
