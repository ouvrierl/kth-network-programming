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
import common.Message;
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
	private boolean turnLaunch = false;
	private int wordSize = 0;

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
				switch (message.getMessageType()) {
				case MessageType.START:
					this.start(message);
					break;
				case MessageType.QUIT:
					this.quit(message);
					break;
				case MessageType.LETTER:
					this.manageLetter(message);
					break;
				case MessageType.WORD:
					this.manageWord(message);
					break;
				default:
					throw new MessageException("Invalid message received: " + messageReceived);
				}
			} catch (Exception e) {
				this.quit();
				throw new MessageException(e.getMessage());
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
		this.sendMessage(MessageType.ERRORLETTER);
	}

	private void turnNotBegan() {
		this.sendMessage(MessageType.ERRORTURN);
	}

	private void manageWord(Message message) {
		if (!this.turnLaunch) {
			this.turnNotBegan();
		} else {
			if (message.getMessageBody().size() != 1) {
				throw new MessageException("Invalid WORD message received (invalid number of arguments): " + message);
			}
			String wordProposed = message.getMessageBody().get(0).toLowerCase();
			if (wordProposed.length() != this.wordSize) {
				throw new MessageException("Invalid WORD message received (word of invalid length): " + message);
			}
			if (wordProposed.equals(this.chosenWord)) {
				this.victory();
			} else {
				this.failedAttempt();
			}
			if (this.remainingFailedAttempts == 0) {
				this.defeat();
			}
		}
	}

	private void manageLetter(Message message) {
		if (!this.turnLaunch) {
			this.turnNotBegan();
		} else {
			if (message.getMessageBody().size() != 1) {
				throw new MessageException("Invalid LETTER message received (invalid number of arguments): " + message);
			}
			if (message.getMessageBody().get(0).length() != 1) {
				throw new MessageException("Invalid LETTER message received (one letter needed): " + message);
			}
			char letter = message.getMessageBody().get(0).toLowerCase().charAt(0);
			if (!Character.isLetter(letter)) {
				throw new MessageException("Invalid LETTER message received (not a letter): " + message);
			}
			if (this.lettersProposed.contains(letter)) {
				this.letterAlreadyProposed();
			} else {
				this.lettersProposed.add(letter);
				if (this.chosenWord.contains(Character.toString(letter))) {
					for (int i = 0; i < chosenWord.length(); i++) {
						if (this.chosenWord.charAt(i) == letter) {
							this.numberOfLettersFound++;
							this.sendMessageToPrepare(MessageType.FIND, Character.toString(letter),
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
	}

	private void quit(Message message) {
		if (!message.getMessageBody().isEmpty()) {
			throw new MessageException("Invalid QUIT message received (no argument needed):" + message);
		}
		this.quit();
	}

	private void quit() {
		try {
			this.clientSocket.close();
		} catch (Exception e) {
			throw new IOException("Error in closing the client socket");
		}
		this.connected = false;
	}

	private void sendMessageToPrepare(String... args) {
		StringBuilder message = new StringBuilder();
		for (String arg : args) {
			message.append(arg);
			message.append(MessageType.DELIMITER);
		}
		message.setLength(message.length() - 1); // Last useless space is
													// removed
		this.sendMessage(message.toString());
	}

	private void sendMessage(String message) {
		this.output.println(message);
		this.output.flush();
	}

	private void defeat() {
		this.score--;
		this.sendMessageToPrepare(MessageType.DEFEAT, this.chosenWord, Integer.toString(this.score));
		this.reset();
	}

	private void victory() {
		this.score++;
		this.sendMessageToPrepare(MessageType.VICTORY, this.chosenWord, Integer.toString(this.score));
		this.reset();
	}

	private void reset() {
		this.chosenWord = "";
		this.remainingFailedAttempts = Integer.MAX_VALUE;
		this.numberOfLettersFound = Integer.MAX_VALUE;
		this.wordSize = Integer.MAX_VALUE;
		this.turnLaunch = false;
	}

	private void start(Message message) {
		if (!message.getMessageBody().isEmpty()) {
			throw new MessageException("Invalid START message received (no argument needed):" + message);
		}
		this.chosenWord = randomWord();
		this.wordSize = chosenWord.length();
		this.remainingFailedAttempts = wordSize;
		this.numberOfLettersFound = 0;
		this.sendMessageToPrepare(MessageType.WELCOME, Integer.toString(this.chosenWord.length()));
		this.lettersProposed.clear();
		this.turnLaunch = true;
	}

	private void failedAttempt() {
		this.remainingFailedAttempts--;
		this.sendMessageToPrepare(MessageType.ATTEMPT, Integer.toString(this.remainingFailedAttempts));
	}

}