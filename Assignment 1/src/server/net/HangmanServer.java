package server.net;

import java.net.ServerSocket;
import java.net.Socket;

public class HangmanServer {
	
	private final static int port = 8080;

	public static void main(String[] args) {
		try {
			ServerSocket listeningSocket = new ServerSocket(port);
			while (true) {
				Socket clientSocket = listeningSocket.accept();
				ClientHandler handler = new ClientHandler(clientSocket);
				Thread handlerThread = new Thread(handler);
				handlerThread.start();
			}
		} catch (Exception e) {
			System.err.println("Server failure.");
		}
	}

}
