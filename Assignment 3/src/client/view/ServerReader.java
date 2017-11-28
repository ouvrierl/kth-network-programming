package client.view;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import common.catalog.CatalogServer;

public class ServerReader extends UnicastRemoteObject implements CatalogServer {

	private CatalogServer catalogServer;

	protected ServerReader(CatalogServer catalogServers) throws RemoteException {
		this.catalogServer = catalogServer;
	}

	@Override
	public boolean login(CatalogServer catalogServer, String username, String password) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean register(CatalogServer catalogServer, String username, String password) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unregister(CatalogServer catalogServer) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean logout(CatalogServer catalogServer) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addFile(CatalogServer catalogServer, String name, long size, String access, String action)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Object[]> getFiles(CatalogServer catalogServer) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeFile(CatalogServer catalogServer, String fileName) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateFile(CatalogServer catalogServer, String fileName, long length, String access, String action)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void incomingClient(CatalogServer catalogServer) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean downloadFile(CatalogServer catalogServer, String fileName) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

}
