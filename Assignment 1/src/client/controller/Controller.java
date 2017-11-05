package client.controller;

import client.net.ServerConnection;
import client.view.Screen;

public class Controller {

	private ServerConnection serverConnection;
	private Screen screen;

	public Controller(Screen screen) {
		this.serverConnection = new ServerConnection(this);
		this.screen = screen;
	}

	public void connect(String host, int port) {
		serverConnection.connect(host, port);
	}

	public void sendMessage(String message) {
		serverConnection.sendMessage(message);
	}

	public void setScore(int newScore) {
		screen.setScore(newScore);
	}

	public void setWord(String newWord) {
		screen.setWord(newWord);
	}

	public void setLetter(char letter, int position) {
		screen.setLetter(letter, position);
	}

	public void setNumberOfRemainingFailedAttempts(int newNumber) {
		screen.setNumberOfRemainingFailedAttempts(newNumber);
	}
}
