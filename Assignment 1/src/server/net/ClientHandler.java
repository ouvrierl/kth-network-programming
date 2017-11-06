package server.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import common.ConnectionException;
import common.IOException;
import common.MessageException;
import common.MessageType;

public class ClientHandler implements Runnable {

	private static final String WORDS_FILE = "src/server/net/words.txt";

	private final Socket clientSocket;
	private BufferedReader input;
	private PrintWriter output;
	private boolean connected = true;
	private String chosenWord = "";
	private int remainingFailedAttempts = 0;
	private int numberOfLettersFound = 0;
	private int score = 0;
	private List<Character> lettersProposed = new ArrayList<>();

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
				String messageReceived = this.input.readLine();
				Message message = new Message(messageReceived);
				switch (message.messageType) {
				case MessageType.START:
					this.start();
					break;
				case MessageType.QUIT:
					this.quit();
					break;
				case MessageType.LETTER:
					manageLetter(message);
					break;
				case MessageType.WORD:
					manageWord(message);
					break;
				default:
					throw new MessageException("Invalid message received: " + message);
				}
			} catch (Exception e) {
				quit();
				throw new MessageException("Error in reading server input");
			}
		}
	}

	/**
	 * @return a random word taken from the file words.txt, in lower case
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

	private void letterAlreadyProposed() {
		sendMessage(MessageType.ERRORLETTER);
	}

	private void manageWord(Message message) {
		if (message.messageBody.isEmpty() || message.messageBody.size() > 1
				|| message.messageBody.get(0).length() != chosenWord.length()) {
			throw new MessageException("Invalid WORD message received (word of invalid length): " + message);
		}
		String wordProposed = message.messageBody.get(0).toLowerCase();
		if (wordProposed.equals(this.chosenWord)) {
			this.victory();
		} else {
			this.failedAttempt();
		}
		if (this.remainingFailedAttempts == 0) {
			this.defeat();
		}
	}

	private void manageLetter(Message message) {
		if (message.messageBody.isEmpty() || message.messageBody.size() > 1
				|| message.messageBody.get(0).length() != 1) {
			throw new MessageException("Invalid LETTER message received (one letter needed): " + message);
		}
		char letter = message.messageBody.get(0).toLowerCase().charAt(0);
		if (!Character.isLetter(letter)) {
			throw new MessageException("Invalid LETTER message received (not a letter): " + message);
		}
		if (this.lettersProposed.contains(letter)) {
			letterAlreadyProposed();
		} else {
			this.lettersProposed.add(letter);
			if (this.chosenWord.contains(Character.toString(letter))) {
				for (int i = 0; i < chosenWord.length(); i++) {
					if (this.chosenWord.charAt(i) == letter) {
						this.numberOfLettersFound++;
						this.sendMessage(MessageType.FIND, Character.toString(Character.toLowerCase(letter)),
								Integer.toString(i));
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
		}
	}

	private void quit() {
		try {
			this.clientSocket.close();
		} catch (Exception e) {
			throw new IOException("Error in closing the client socket");
		}
		this.connected = false;
	}

	private void sendMessage(String... args) {
		StringBuilder builder = new StringBuilder();
		for (String arg : args) {
			builder.append(arg);
			builder.append(MessageType.DELIMITER);
		}
		builder.setLength(builder.length() - 1);
		this.output.println(builder.toString());
		this.output.flush();
	}

	private void defeat() {
		this.score--;
		this.sendMessage(MessageType.DEFEAT, this.chosenWord, Integer.toString(this.score));
		this.reset();
	}

	private void victory() {
		this.score++;
		this.sendMessage(MessageType.VICTORY, this.chosenWord, Integer.toString(this.score));
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
		this.sendMessage(MessageType.WELCOME, Integer.toString(this.chosenWord.length()));
		this.lettersProposed.clear();
	}

	private void failedAttempt() {
		this.remainingFailedAttempts--;
		this.sendMessage(MessageType.ATTEMPT, Integer.toString(this.remainingFailedAttempts));
	}

	private static class Message {

		private String messageType;
		private List<String> messageBody;

		private Message(String message) {
			this.parse(message);
		}

		private void parse(String message) {
			String[] split = message.split(MessageType.DELIMITER);
			this.messageType = split[0];
			this.messageBody = new ArrayList<>();
			for (int i = 1; i < split.length; i++) {
				this.messageBody.add(split[i]);
			}
		}
	}

}