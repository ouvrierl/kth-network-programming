package common.catalog;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface CatalogServer extends Remote {

	public static final String SERVER_NAME_IN_REGISTRY = "CATALOG_SERVER";

	void incomingClient(CatalogServer catalogServer) throws RemoteException;

	boolean login(CatalogServer catalogServer, String username, String password) throws RemoteException;

	boolean register(CatalogServer catalogServer, String username, String password) throws RemoteException;

	boolean unregister(CatalogServer catalogServer) throws RemoteException;

	boolean logout(CatalogServer catalogServer) throws RemoteException;

	boolean addFile(CatalogServer catalogServer, String name, long size, String access, String action)
			throws RemoteException;

	List<Object[]> getFiles(CatalogServer catalogServer) throws RemoteException;

	boolean downloadFile(CatalogServer catalogServer, String fileName) throws RemoteException;

	boolean removeFile(CatalogServer catalogServer, String fileName) throws RemoteException;

	boolean updateFile(CatalogServer catalogServer, String fileName, long length, String access, String action)
			throws RemoteException;

}
