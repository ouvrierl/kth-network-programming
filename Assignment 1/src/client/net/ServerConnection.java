package client.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import client.controller.Controller;

public class ServerConnection {
	private static final int TIMEOUT_HALF_HOUR = 1800000;
	private static final int TIMEOUT_HALF_MINUTE = 30000;
	private Socket socket;
	private BufferedReader input;
	public PrintWriter output;
	private volatile boolean connected;
	private Controller controller;

	public ServerConnection(Controller controller) {
		this.controller = controller;
	}

	public void connect(String host, int port) {
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(host, port), TIMEOUT_HALF_MINUTE);
			socket.setSoTimeout(TIMEOUT_HALF_HOUR);
			connected = true;
			output = new PrintWriter(socket.getOutputStream());
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			new Thread(new Listener()).start();
		} catch (Exception e) {
			// Manage connection client error
		}
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
					System.out.println(message);
					if (message.startsWith("WELCOME")) {
						int wordSize = Integer.parseInt(message.substring(8));
						controller.setNumberOfRemainingFailedAttempts(wordSize);
						String wordPrepared = "";
						for (int i = 0; i < wordSize; i++) {
							wordPrepared = wordPrepared.concat(" _ ");
						}
						controller.setWord(wordPrepared);
					} else if (message.startsWith("ATTEMPT")) {
						int newScore = Integer.parseInt(message.substring(8));
						controller.setNumberOfRemainingFailedAttempts(newScore);
					} else if (message.startsWith("FIND")) {
						char letter = message.charAt(5);
						int position = Integer.parseInt(message.substring(7));
						controller.setLetter(letter, position);

					} else if (message.startsWith("VICTORY")) {
						String finalWord = message.substring(8, message.length() - 2);
						int score = Integer.parseInt(message.substring(message.length() - 2));
						String wordPrepared = "";
						for (int i = 0; i < finalWord.length(); i++) {
							wordPrepared = wordPrepared.concat(" " + finalWord.charAt(i) + " ");
						}
						controller.setWord(wordPrepared);
						controller.setNumberOfRemainingFailedAttempts(-1);
						controller.setScore(score);
					} else if (message.startsWith("DEFEAT")) {
						String finalWord = message.substring(7, message.length() - 2);
						int score = Integer.parseInt(message.substring(message.length() - 2));
						String wordPrepared = "";
						for (int i = 0; i < finalWord.length(); i++) {
							wordPrepared = wordPrepared.concat(" " + finalWord.charAt(i) + " ");
						}
						controller.setWord(wordPrepared);
						controller.setNumberOfRemainingFailedAttempts(-1);
						controller.setScore(score);
					}
				}
			} catch (Throwable connectionFailure) {
				if (connected) {
					// Manage lost connexion
				}
			}
		}

	}

	public void sendMessage(String message) {
		output.println(message);
		output.flush();
	}

}
