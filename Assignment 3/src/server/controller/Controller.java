package server.controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import common.catalog.CatalogServer;

public class Controller extends UnicastRemoteObject implements CatalogServer {

	public Controller() throws RemoteException {
	}

	@Override
	public boolean login(String username, String password) throws RemoteException {
		return true;
	}

	@Override
	public boolean register(String username, String password) throws RemoteException {
		return false;
	}

}
