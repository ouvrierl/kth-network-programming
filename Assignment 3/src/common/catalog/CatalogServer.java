package common.catalog;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CatalogServer extends Remote {

	public static final String SERVER_NAME_IN_REGISTRY = "CATALOG_SERVER";

	boolean login(String username, String password) throws RemoteException;

	boolean register(String username, String password) throws RemoteException;

}
