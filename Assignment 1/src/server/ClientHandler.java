package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;

import common.ConnectionException;
import common.IOException;
import common.MessageException;

public class ClientHandler implements Runnable {

	private static final String WORDS_FILE = "src/server/words.txt";

	private final Socket clientSocket;
	private BufferedReader input;
	private PrintWriter output;
	private boolean connected = true;
	private String chosenWord = "";
	private int remainingFailedAttempts = 0;
	private int numberOfLettersFound = 0;
	private int score = 0;

	public ClientHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	@Override
	public void run() {
		try {
			input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			output = new PrintWriter(clientSocket.getOutputStream());
		} catch (Exception e) {
			throw new ConnectionException("Error during the server connection");
		}
		while (connected) {
			try {
				String message = input.readLine();
				if (message == null) {
					message = "";
				}
				if (message.startsWith("START")) {
					start();
				} else if (message.equals("QUIT")) {
					quit();
				} else if (message.startsWith("LETTER")) {
					if (message.length() != 8) {
						throw new MessageException("Invalid LETTER message received (one letter needed): " + message);
					}
					char letter = message.substring(7).toLowerCase().charAt(0);
					if (!Character.isLetter(letter)) {
						throw new MessageException("Invalid LETTER message received (not a letter): " + message);
					}
					if (chosenWord.contains(Character.toString(letter))) {
						for (int i = 0; i < chosenWord.length(); i++) {
							if (chosenWord.charAt(i) == letter) {
								numberOfLettersFound++;
								sendMessage("FIND " + Character.toLowerCase(letter) + " " + i);
							}
						}
					} else {
						failedAttempt();
					}
					if (numberOfLettersFound == chosenWord.length()) {
						victory();
					}
					if (remainingFailedAttempts == 0) {
						defeat();
					}
				} else if (message.startsWith("WORD")) {
					if (message.length() != chosenWord.length() + 5) {
						throw new MessageException(
								"Invalid WORD message received (word of invalid length): " + message);
					}
					String wordProposed = message.substring(5).toLowerCase();
					if (wordProposed.equals(chosenWord)) {
						victory();
					} else {
						failedAttempt();
					}
					if (remainingFailedAttempts == 0) {
						defeat();
					}
				} else {
					throw new MessageException("Invalid message received: " + message);
				}
			} catch (Exception e) {
				quit();
				throw new MessageException("Error in reading server input");
			}
		}
	}

	/**
	 * @return a random word taken from the file words.txt
	 */
	private String randomWord() {
		String word = "";
		try (Reader reader = new FileReader(new File(WORDS_FILE));
				BufferedReader buffered = new BufferedReader(reader)) {
			String line = buffered.readLine();
			int stop = (int) (51527 * Math.random());
			int i = 0;
			while (line != null) {
				if (i == stop) {
					word = line;
					break;
				}
				line = buffered.readLine();
				i++;
			}
		} catch (Exception e) {
			throw new IOException("Error in reading words file");
		}
		return word.toLowerCase();
	}

	private void quit() {
		try {
			clientSocket.close();
		} catch (Exception e) {
			throw new IOException("Error in closing the client socket");
		}
		connected = false;
	}

	private void sendMessage(String message) {
		output.println(message);
		output.flush();
	}

	private void defeat() {
		score--;
		sendMessage("DEFEAT " + chosenWord + " " + score);
		reset();
	}

	private void victory() {
		score++;
		sendMessage("VICTORY " + chosenWord + " " + score);
		reset();
	}

	private void reset() {
		chosenWord = "";
		remainingFailedAttempts = Integer.MAX_VALUE;
		numberOfLettersFound = Integer.MAX_VALUE;
	}

	private void start() {
		chosenWord = randomWord();
		remainingFailedAttempts = chosenWord.length();
		numberOfLettersFound = 0;
		sendMessage("WELCOME " + chosenWord.length());
	}

	private void failedAttempt() {
		remainingFailedAttempts--;
		sendMessage("ATTEMPT " + remainingFailedAttempts);
	}

}