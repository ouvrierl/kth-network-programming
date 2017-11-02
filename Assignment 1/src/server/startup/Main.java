package server.startup;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import server.net.ClientHandler;

public class Main {

	public static void main(String[] args) {
		try {
            ServerSocket listeningSocket = new ServerSocket(8080);
            while (true) {
                Socket clientSocket = listeningSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                Thread handlerThread = new Thread(handler);
                handlerThread.setPriority(Thread.MAX_PRIORITY);
                handlerThread.start();
            }
        } catch (IOException e) {
            System.err.println("Server failure.");
        }
	}

}
