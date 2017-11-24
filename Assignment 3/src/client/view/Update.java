package client.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
				CatalogFile file = getTableView().getItems().get(getIndex());
				String fileName = file.getName();
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
