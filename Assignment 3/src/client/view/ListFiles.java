package client.view;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import common.constants.Constants;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;

public class ListFiles {

	private Scene scene;

	public ListFiles(ViewManager viewManager) {
		GridPane root = new GridPane();
		root.setAlignment(Pos.CENTER);
		root.setHgap(10);
		root.setVgap(10);
		root.setPadding(new Insets(25, 25, 25, 25));

		Label intro = new Label("List of the files in the catalog");
		root.add(intro, 0, 0);

		final ObservableList<client.view.File> data = FXCollections.observableArrayList();
		try {
			List<Object[]> files = viewManager.getServer().getFiles();
			for (Object[] file : files) {
				client.view.File filePrepared = new client.view.File(file[0].toString(), file[1].toString(),
						file[2].toString(), file[3].toString(), file[4].toString());
				data.add(filePrepared);
			}
		} catch (RemoteException e) {
			System.err.println("Error while getting files list.");
		}
		TableView<client.view.File> table = new TableView<>();
		table.setEditable(false);
		TableColumn name = new TableColumn("Name");
		name.setCellValueFactory(new PropertyValueFactory<client.view.File, String>("name"));
		TableColumn size = new TableColumn("Size");
		size.setCellValueFactory(new PropertyValueFactory<client.view.File, String>("size"));
		TableColumn owner = new TableColumn("Owner");
		owner.setCellValueFactory(new PropertyValueFactory<client.view.File, String>("owner"));
		TableColumn access = new TableColumn("Access");
		access.setCellValueFactory(new PropertyValueFactory<client.view.File, String>("access"));
		TableColumn action = new TableColumn("Action");
		action.setCellValueFactory(new PropertyValueFactory<client.view.File, String>("action"));
		TableColumn download = new TableColumn("Download");
		download.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<client.view.File, Boolean>, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<client.view.File, Boolean> p) {
						return new SimpleBooleanProperty(p.getValue() != null);
					}
				});
		download.setCellFactory(
				new Callback<TableColumn<client.view.File, Boolean>, TableCell<client.view.File, Boolean>>() {
					@Override
					public TableCell<client.view.File, Boolean> call(TableColumn<client.view.File, Boolean> p) {
						return new Download();
					}

				});
		table.setItems(data);
		table.getColumns().addAll(name, size, owner, access, action, download);
		table.setMinWidth(700);
		root.add(table, 0, 2);

		Image imageHome = new Image(getClass().getResourceAsStream("./home.png"), 25, 25, true, false);
		Button home = new Button("", new ImageView(imageHome));
		root.add(home, 0, 5);
		home.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Home home = new Home(viewManager);
				Scene homeScene = home.getScene();
				viewManager.getStage().setScene(homeScene);
			}
		});

		Button addFile = new Button("Add a file to the catalog");
		root.add(addFile, 0, 3);
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
							ListFiles listUpdated = new ListFiles(viewManager);
							Scene sceneUpdated = listUpdated.getScene();
							viewManager.getStage().setScene(sceneUpdated);
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

		this.scene = new Scene(root, 800, 600);
		this.scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

	}

	public Scene getScene() {
		return this.scene;
	}

}