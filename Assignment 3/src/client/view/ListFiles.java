package client.view;

import java.rmi.RemoteException;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;

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

		TableView table = new TableView();
		table.setEditable(false);
		TableColumn name = new TableColumn("Name");
		TableColumn size = new TableColumn("Size");
		TableColumn owner = new TableColumn("Owner");
		TableColumn access = new TableColumn("Access");
		TableColumn action = new TableColumn("Action");
		table.getColumns().addAll(name, size, owner, access, action);
		try {
			List<Object[]> files = viewManager.getServer().getFiles();
			for (Object[] file : files) {
				System.out.print("row : ");
				for (Object column : file) {
					System.out.print(column.toString() + " ");
				}
				System.out.println("");
			}
		} catch (RemoteException e) {
			System.err.println("Error while getting files list.");
		}
		root.add(table, 0, 2);

		this.scene = new Scene(root, 800, 800);
		this.scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

	}

	public Scene getScene() {
		return this.scene;
	}

}
