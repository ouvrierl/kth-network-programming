package client.controller;

import java.io.File;

import client.net.ServerConnection;

public class Controller {
	private ServerConnection serverConnection;

	public Controller() {
		this.serverConnection = new ServerConnection();
	}

	public void connect(String host, int port) {
		this.serverConnection.connect(host, port);
	}

	public void disconnect() {
		this.serverConnection.disconnect();
	}

	public void sendMessage(String message) {
		this.serverConnection.sendMessage(message);
	}

	public void sendFile(File file) {
		this.serverConnection.sendFile(file);
	}
}
