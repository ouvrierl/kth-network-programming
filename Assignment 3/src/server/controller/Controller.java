package server.controller;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import common.catalog.CatalogServer;
import common.exception.IOException;
import server.integration.Catalog;
import server.net.ClientHandler;

public class Controller extends UnicastRemoteObject implements CatalogServer {

	private final Catalog catalog;
	private Map<CatalogServer, String> loggedUsers;
	private Map<CatalogServer, ClientHandler> clientHandlers;
	private Map<String, String> notifications;
	private CatalogServer serverWaitingForSocket;

	public synchronized void newClientHandler(ClientHandler clientHandler) {
		if (this.serverWaitingForSocket != null) {
			this.clientHandlers.put(this.serverWaitingForSocket, clientHandler);
			this.serverWaitingForSocket = null;
		} else {
			throw new IOException("Error while associating CatalogServer and ClientHandler.");
		}
	}

	public Controller() throws RemoteException {
		super();
		this.catalog = new Catalog();
		this.loggedUsers = new HashMap<>();
		this.clientHandlers = new HashMap<>();
		this.notifications = new HashMap<>();
		this.serverWaitingForSocket = null;
	}

	@Override
	public synchronized void incomingClient(CatalogServer catalogServer) throws RemoteException {
		this.serverWaitingForSocket = catalogServer;
	}

	@Override
	public void leavingClient(CatalogServer catalogServer) throws RemoteException {
		this.loggedUsers.remove(catalogServer);
		this.clientHandlers.remove(catalogServer);
	}

	@Override
	public synchronized boolean login(CatalogServer catalogServer, String username, String password)
			throws RemoteException {
		if (this.loggedUsers.containsKey(catalogServer)) {
			// Client already logged, need to logout
			return false;
		}
		if (this.loggedUsers.containsValue(username)) {
			// User already logged somewhere
			return false;
		}
		if (this.catalog.isAccount(username, password)) {
			this.loggedUsers.put(catalogServer, username);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public synchronized boolean register(CatalogServer catalogServer, String username, String password)
			throws RemoteException {
		if (this.loggedUsers.containsKey(catalogServer)) {
			return false;
		}
		return this.catalog.createAccount(username, password);
	}

	@Override
	public synchronized boolean unregister(CatalogServer catalogServer) throws RemoteException {
		if (!this.loggedUsers.containsKey(catalogServer)) {
			return false;
		}
		if (this.catalog.deleteAccount(this.loggedUsers.get(catalogServer))) {
			this.removeFilesLinkedToUser(this.loggedUsers.get(catalogServer));
			this.loggedUsers.remove(catalogServer);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public synchronized boolean logout(CatalogServer catalogServer) throws RemoteException {
		if (this.loggedUsers.containsKey(catalogServer)) {
			this.removeFilesLinkedToUser(this.loggedUsers.get(catalogServer));
			this.loggedUsers.remove(catalogServer);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public synchronized boolean addFile(CatalogServer catalogServer, String name, long size, String access,
			String action) throws RemoteException {
		if (!this.loggedUsers.containsKey(catalogServer)) {
			return false;
		}
		return this.catalog.addFile(name, size, this.loggedUsers.get(catalogServer), access, action);
	}

	@Override
	public synchronized List<Object[]> getFiles(CatalogServer catalogServer) throws RemoteException {
		if (!this.loggedUsers.containsKey(catalogServer)) {
			return null;
		} else {
			return this.catalog.getFiles(this.loggedUsers.get(catalogServer));
		}
	}

	@Override
	public synchronized boolean downloadFile(CatalogServer catalogServer, String fileName) throws RemoteException {
		// Check if file exists for security
		this.clientHandlers.get(catalogServer).sendFile(fileName);
		this.checkNotification(fileName, this.loggedUsers.get(catalogServer), CatalogServer.ACTION_DOWNLOAD);
		return true;
	}

	@Override
	public synchronized boolean removeFile(CatalogServer catalogServer, String fileName) throws RemoteException {
		boolean dataDeleted = this.catalog.deleteFile(fileName, this.loggedUsers.get(catalogServer));
		boolean fileDeleted = false;
		if (dataDeleted) {
			File fileToDelete = new File(ClientHandler.FILES_DIRECTORY + fileName);
			fileDeleted = fileToDelete.delete();
		}
		if (fileDeleted && dataDeleted) {
			this.checkNotification(fileName, this.loggedUsers.get(catalogServer), CatalogServer.ACTION_REMOVE);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public synchronized boolean updateFile(CatalogServer catalogServer, String fileName, long length, String access,
			String action) throws RemoteException {
		if (this.catalog.updateFile(fileName, length, this.loggedUsers.get(catalogServer), access, action)) {
			this.checkNotification(fileName, this.loggedUsers.get(catalogServer), CatalogServer.ACTION_UPDATE);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public synchronized boolean notifyFile(CatalogServer catalogServer, String fileName) throws RemoteException {
		if (this.catalog.controlNotify(this.loggedUsers.get(catalogServer), fileName)) {
			this.notifications.put(fileName, this.loggedUsers.get(catalogServer));
			return true;
		} else {
			return false;
		}
	}

	private synchronized CatalogServer getCatalogServer(String username) {
		for (Entry<CatalogServer, String> association : this.loggedUsers.entrySet()) {
			if (association.getValue().equals(username)) {
				return association.getKey();
			}
		}
		return null;
	}

	private synchronized void removeFilesLinkedToUser(String username) {
		for (Entry<String, String> association : new HashSet<Entry<String, String>>(this.notifications.entrySet())) {
			if (association.getValue().equals(username)) {
				this.notifications.remove(association.getKey());
			}
		}
	}

	private synchronized void checkNotification(String fileName, String username, String action) {
		for (Entry<String, String> association : this.notifications.entrySet()) {
			if (association.getKey().equals(fileName) && !association.getValue().equals(username)) {
				try {
					this.getCatalogServer(association.getValue()).receiveNotification(fileName, username, action);
				} catch (RemoteException e) {
					e.printStackTrace();
					System.err.println("Error while sending notification.");
				}
			}
		}
	}

	@Override
	public void receiveNotification(String fileName, String username, String action) throws RemoteException {
	}

}
