package client.view;

import javafx.beans.property.SimpleStringProperty;

public class File {

	private final SimpleStringProperty name;
	private final SimpleStringProperty size;
	private final SimpleStringProperty owner;
	private final SimpleStringProperty access;
	private final SimpleStringProperty action;

	public File(String name, String size, String owner, String access, String action) {
		this.name = new SimpleStringProperty(name);
		this.size = new SimpleStringProperty(size);
		this.owner = new SimpleStringProperty(owner);
		this.access = new SimpleStringProperty(access);
		this.action = new SimpleStringProperty(action);
	}

	public String getName() {
		return this.name.get();
	}

	public void setFirstName(String name) {
		this.name.set(name);
	}

	public String getSize() {
		return this.size.get();
	}

	public void setSize(String size) {
		this.size.set(size);
	}

	public String getOwner() {
		return this.owner.get();
	}

	public void setOwner(String owner) {
		this.owner.set(owner);
	}

	public String getAccess() {
		return this.access.get();
	}

	public void setAccess(String access) {
		this.access.set(access);
	}

	public String getAction() {
		return this.action.get();
	}

	public void setAction(String action) {
		this.action.set(action);
	}

}
