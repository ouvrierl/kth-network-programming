package client.controller;

import client.net.OutputHandler;
import client.net.ServerConnection;

public class Controller {

	private ServerConnection serverConnection;

	public Controller() {
		this.serverConnection = new ServerConnection();
	}

	public void connect(String host, int port, OutputHandler outputHandler) {
		this.serverConnection.connect(host, port, outputHandler);
	}

	public void disconnect() {
		this.serverConnection.disconnect();
	}

	public void start() {
		this.serverConnection.start();
	}

	public void sendMessage(String message) {
		this.serverConnection.sendMessage(message);
	}

}
