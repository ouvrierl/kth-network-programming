package server.controller;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import common.catalog.CatalogServer;
import server.integration.Catalog;
import server.model.User;
import server.net.ClientHandler;

public class Controller extends UnicastRemoteObject implements CatalogServer {

	private final Catalog catalog;
	private User loggedUser;
	private ClientHandler clientHandler;

	public void setClientHandler(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}

	public Controller() throws RemoteException {
		super();
		this.catalog = new Catalog();
		this.loggedUser = null;
	}

	@Override
	public boolean login(String username, String password) throws RemoteException {
		if (this.catalog.isAccount(username, password)) {
			this.loggedUser = new User(username);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean register(String username, String password) throws RemoteException {
		return this.catalog.createAccount(username, password);
	}

	@Override
	public boolean unregister() throws RemoteException {
		if (this.loggedUser == null) {
			return false;
		}
		if (this.catalog.deleteAccount(this.loggedUser)) {
			this.loggedUser = null;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean logout() throws RemoteException {
		if (this.loggedUser != null) {
			this.loggedUser = null;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean addFile(String name, long size, String access, String action) throws RemoteException {
		if (this.loggedUser == null) {
			return false;
		}
		return this.catalog.addFile(name, size, this.loggedUser, access, action);
	}

	@Override
	public List<Object[]> getFiles() throws RemoteException {
		if (this.loggedUser == null) {
			return null;
		} else {
			return this.catalog.getFiles(this.loggedUser);
		}
	}

	@Override
	public boolean downloadFile(String name) throws RemoteException {
		// Check if file exists for security
		this.clientHandler.sendFile(name);
		return true;
	}

	@Override
	public boolean removeFile(String name) throws RemoteException {
		boolean dataDeleted = this.catalog.deleteFile(name, this.loggedUser);
		boolean fileDeleted = false;
		if (dataDeleted) {
			File fileToDelete = new File(ClientHandler.FILES_DIRECTORY + name);
			fileDeleted = fileToDelete.delete();
		}
		return fileDeleted && dataDeleted;
	}

	@Override
	public boolean updateFile(String fileName) throws RemoteException {
		return this.catalog.updateFile(fileName, this.loggedUser);
	}

}
