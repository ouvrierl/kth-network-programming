package server.controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import common.catalog.CatalogServer;
import server.integration.Catalog;
import server.model.User;

public class Controller extends UnicastRemoteObject implements CatalogServer {

	private final Catalog catalog;
	private User loggedUser;

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
	public boolean addFile(String name, long size, String access) throws RemoteException {
		if (this.loggedUser == null) {
			return false;
		}
		return this.catalog.addFile(name, size, this.loggedUser, access);
	}

	@Override
	public List<Object[]> getFiles() throws RemoteException {
		return this.catalog.getFiles(this.loggedUser);
	}

}
