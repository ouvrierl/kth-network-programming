package client.startup;

import java.io.IOException;

import javax.swing.JLabel;

import client.net.ServerHandler;
import client.view.ScreenSwing;

public class Main {

	public static void main(String[] args) throws IOException {

		JLabel currentWord = new JLabel("");
		JLabel attemptsNumber = new JLabel("No value");
		JLabel scoreValue = new JLabel("0");

		ServerHandler serverConnection = new ServerHandler(currentWord, attemptsNumber, scoreValue);
		serverConnection.connect("localhost", 8080);

		ScreenSwing screen = new ScreenSwing(currentWord, attemptsNumber, scoreValue, serverConnection.output);

		// ScreenFX screen = new ScreenFX();
		// screen.displayScreen(args);
	}

}
