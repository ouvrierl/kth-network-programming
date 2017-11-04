package server;

import java.net.ServerSocket;
import java.net.Socket;

public class HangmanServer {
	
	private final static int port = 8080;

	public static void main(String[] args) {
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				ClientHandler handler = new ClientHandler(clientSocket);
				Thread handlerThread = new Thread(handler);
				handlerThread.start();
			}
		} catch (Exception e) {
			System.err.println("Error during the creation of the server");
		}
	}

}
