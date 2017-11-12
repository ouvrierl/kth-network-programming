package server.net;

import java.net.ServerSocket;
import java.net.Socket;

import common.exception.ConnectionException;

public class HangmanServer {

	private static final int PORT = 8080;

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT);) {
			while (true) {
				Socket clientSocket = serverSocket.accept();
				ClientHandler clientHandler = new ClientHandler(clientSocket);
				new Thread(clientHandler).start();
			}
		} catch (Exception e) {
			throw new ConnectionException("Error during server launch on port " + PORT);
		}
	}

}