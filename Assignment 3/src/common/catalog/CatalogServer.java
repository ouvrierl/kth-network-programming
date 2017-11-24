package common.catalog;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface CatalogServer extends Remote {

	public static final String SERVER_NAME_IN_REGISTRY = "CATALOG_SERVER";

	boolean login(String username, String password) throws RemoteException;

	boolean register(String username, String password) throws RemoteException;

	boolean unregister() throws RemoteException;

	boolean logout() throws RemoteException;

	boolean addFile(String name, long size, String access) throws RemoteException;

	List<Object[]> getFiles() throws RemoteException;

	boolean downloadFile(String name) throws RemoteException;

}
