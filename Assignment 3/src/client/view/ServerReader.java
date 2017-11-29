package client.view;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import common.catalog.CatalogServer;

public class ServerReader extends UnicastRemoteObject implements CatalogServer {

	protected ServerReader() throws RemoteException {
	}

	@Override
	public boolean login(CatalogServer catalogServer, String username, String password) throws RemoteException {
		return false;
	}

	@Override
	public boolean register(CatalogServer catalogServer, String username, String password) throws RemoteException {
		return false;
	}

	@Override
	public boolean unregister(CatalogServer catalogServer) throws RemoteException {
		return false;
	}

	@Override
	public boolean logout(CatalogServer catalogServer) throws RemoteException {
		return false;
	}

	@Override
	public boolean addFile(CatalogServer catalogServer, String name, long size, String access, String action)
			throws RemoteException {
		return false;
	}

	@Override
	public List<Object[]> getFiles(CatalogServer catalogServer) throws RemoteException {
		return null;
	}

	@Override
	public boolean removeFile(CatalogServer catalogServer, String fileName) throws RemoteException {
		return false;
	}

	@Override
	public boolean updateFile(CatalogServer catalogServer, String fileName, long length, String access, String action)
			throws RemoteException {
		return false;
	}

	@Override
	public void incomingClient(CatalogServer catalogServer) throws RemoteException {
	}

	@Override
	public void leavingClient(CatalogServer catalogServer) throws RemoteException {
	}

	@Override
	public boolean downloadFile(CatalogServer catalogServer, String fileName) throws RemoteException {
		return false;
	}

	@Override
	public boolean notifyFile(CatalogServer catalogServer, String fileName) throws RemoteException {
		return false;
	}

	@Override
	public void receiveNotification(String fileName, String username, String action) throws RemoteException {
		try {
			SystemTray tray = SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().getImage("");
			TrayIcon trayIcon = new TrayIcon(image, "Notification");
			trayIcon.setImageAutoSize(true);
			trayIcon.setToolTip("Notification");
			tray.add(trayIcon);
			trayIcon.displayMessage("Notification", username + " has " + action + " the file " + fileName,
					MessageType.INFO);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error while displaying notification.");
		}
	}

}
