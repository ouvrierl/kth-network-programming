package server;

import java.net.ServerSocket;
import java.net.Socket;

import common.ConnectionException;

public class HangmanServer {

	private static final int PORT = 8080;

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT);) {
			while (true) {
				Socket clientSocket = serverSocket.accept();
				ClientHandler clientHandler = new ClientHandler(clientSocket);
				Thread thread = new Thread(clientHandler);
				thread.start();
			}
		} catch (Exception e) {
			throw new ConnectionException("Error during server launch on port " + PORT);
		}
	}

}