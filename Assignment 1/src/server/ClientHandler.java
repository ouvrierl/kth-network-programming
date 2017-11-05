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
			this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			this.output = new PrintWriter(clientSocket.getOutputStream());
		} catch (Exception e) {
			throw new ConnectionException("Error during the server connection");
		}
		while (connected) {
			try {
				String message = this.input.readLine();
				if (message == null) {
					message = "";
				}
				if (message.startsWith("START")) {
					this.start();
				} else if (message.equals("QUIT")) {
					this.quit();
				} else if (message.startsWith("LETTER")) {
					if (message.length() != 8) {
						throw new MessageException("Invalid LETTER message received (one letter needed): " + message);
					}
					char letter = message.substring(7).toLowerCase().charAt(0);
					if (!Character.isLetter(letter)) {
						throw new MessageException("Invalid LETTER message received (not a letter): " + message);
					}
					if (this.chosenWord.contains(Character.toString(letter))) {
						for (int i = 0; i < chosenWord.length(); i++) {
							if (this.chosenWord.charAt(i) == letter) {
								this.numberOfLettersFound++;
								sendMessage("FIND " + Character.toLowerCase(letter) + " " + i);
							}
						}
					} else {
						this.failedAttempt();
					}
					if (this.numberOfLettersFound == this.chosenWord.length()) {
						this.victory();
					}
					if (this.remainingFailedAttempts == 0) {
						this.defeat();
					}
				} else if (message.startsWith("WORD")) {
					if (message.length() != this.chosenWord.length() + 5) {
						throw new MessageException(
								"Invalid WORD message received (word of invalid length): " + message);
					}
					String wordProposed = message.substring(5).toLowerCase();
					if (wordProposed.equals(this.chosenWord)) {
						this.victory();
					} else {
						this.failedAttempt();
					}
					if (this.remainingFailedAttempts == 0) {
						this.defeat();
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
			this.clientSocket.close();
		} catch (Exception e) {
			throw new IOException("Error in closing the client socket");
		}
		this.connected = false;
	}

	private void sendMessage(String message) {
		this.output.println(message);
		this.output.flush();
	}

	private void defeat() {
		this.score--;
		sendMessage("DEFEAT " + this.chosenWord + " " + this.score);
		reset();
	}

	private void victory() {
		this.score++;
		this.sendMessage("VICTORY " + this.chosenWord + " " + this.score);
		this.reset();
	}

	private void reset() {
		this.chosenWord = "";
		this.remainingFailedAttempts = Integer.MAX_VALUE;
		this.numberOfLettersFound = Integer.MAX_VALUE;
	}

	private void start() {
		this.chosenWord = randomWord();
		this.remainingFailedAttempts = chosenWord.length();
		this.numberOfLettersFound = 0;
		this.sendMessage("WELCOME " + chosenWord.length());
	}

	private void failedAttempt() {
		this.remainingFailedAttempts--;
		this.sendMessage("ATTEMPT " + remainingFailedAttempts);
	}

}