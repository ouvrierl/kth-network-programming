package client.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Download extends TableCell<File, Boolean> {

	Button download;

	public Download() {

		Image imageHome = new Image(getClass().getResourceAsStream("./download.jpg"), 25, 25, true, false);
		this.download = new Button("", new ImageView(imageHome));

		this.download.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				File file = getTableView().getItems().get(getIndex());
				System.out.println(file.getName());
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
