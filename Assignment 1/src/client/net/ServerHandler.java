package client.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.swing.JLabel;

public class ServerHandler {
	private static final int TIMEOUT_HALF_HOUR = 1800000;
	private static final int TIMEOUT_HALF_MINUTE = 30000;
	private Socket socket;
	private BufferedReader input;
	public PrintWriter output;
	private volatile boolean connected;
	private JLabel currentWord;
	private JLabel attemptsNumber;
	private JLabel scoreValue;

	public ServerHandler(JLabel currentWord, JLabel attemptsNumber, JLabel scoreValue) {
		this.currentWord = currentWord;
		this.attemptsNumber = attemptsNumber;
		this.scoreValue = scoreValue;
	}

	public void connect(String host, int port) throws IOException {
		socket = new Socket();
		socket.connect(new InetSocketAddress(host, port), TIMEOUT_HALF_MINUTE);
		socket.setSoTimeout(TIMEOUT_HALF_HOUR);
		connected = true;
		output = new PrintWriter(socket.getOutputStream());
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		new Thread(new Listener()).start();
	}

	public void disconnect() throws IOException {
		socket.close();
		socket = null;
		connected = false;
	}

	private class Listener implements Runnable {

		@Override
		public void run() {
			try {
				for (;;) {
					String message = input.readLine();
					if (message.startsWith("WELCOME")) {
						int wordSize = Integer.parseInt(message.substring(8));
						attemptsNumber.setText(Integer.toString(wordSize));
						String wordPrepared = "";
						for (int i = 0; i < wordSize; i++) {
							wordPrepared = wordPrepared.concat(" _ ");
						}
						currentWord.setText(wordPrepared);
					} else if (message.startsWith("ATTEMPT")) {
						int currentScore = Integer.parseInt(attemptsNumber.getText());
						attemptsNumber.setText(Integer.toString(currentScore - 1));
					} else if (message.startsWith("FIND")) {
						char letter = message.charAt(5);
						int position = Integer.parseInt(message.substring(7));
						StringBuilder newWord = new StringBuilder(currentWord.getText());
						newWord.setCharAt(1 + 3 * position, letter);
						currentWord.setText(newWord.toString());
					} else if (message.startsWith("VICTORY")) {
						String finalWord = message.substring(8);
						String wordPrepared = "";
						for (int i = 0; i < finalWord.length(); i++) {
							wordPrepared = wordPrepared.concat(" " + finalWord.charAt(i) + " ");
						}
						currentWord.setText(wordPrepared);
						attemptsNumber.setText("No value");
						scoreValue.setText(Integer.toString(Integer.parseInt(scoreValue.getText()) + 1));
					} else if (message.startsWith("DEFEAT")) {
						String finalWord = message.substring(7);
						String wordPrepared = "";
						for (int i = 0; i < finalWord.length(); i++) {
							wordPrepared = wordPrepared.concat(" " + finalWord.charAt(i) + " ");
						}
						currentWord.setText(wordPrepared);
						attemptsNumber.setText("No value");
						scoreValue.setText(Integer.toString(Integer.parseInt(scoreValue.getText()) - 1));
					}
				}
			} catch (Throwable connectionFailure) {
				if (connected) {
					// Manage lost connexion
				}
			}
		}

	}

}
