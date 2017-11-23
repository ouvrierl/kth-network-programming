package server.controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import common.catalog.CatalogServer;
import server.integration.Catalog;

public class Controller extends UnicastRemoteObject implements CatalogServer {

	private final Catalog catalog;

	public Controller() throws RemoteException {
		super();
		this.catalog = new Catalog();
	}

	@Override
	public boolean login(String username, String password) throws RemoteException {
		return this.catalog.isAccount(username, password);
	}

	@Override
	public boolean register(String username, String password) throws RemoteException {
		return this.catalog.createAccount(username, password);
	}

}
