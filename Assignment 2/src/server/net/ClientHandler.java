package server.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import common.exception.MessageException;
import common.message.Message;
import common.message.MessageType;

public class ClientHandler {

	private static final String WORDS_FILE = "src/server/net/words.txt";

	private final SocketChannel clientChannel;
	private final ByteBuffer messageFromClient = ByteBuffer.allocateDirect(MessageType.MESSAGELENGTH);
	private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
	private String chosenWord = "";
	private int wordSize = 0;
	private int remainingFailedAttempts = 0;
	private int numberOfLettersFound = 0;
	private int score = 0;
	private List<Character> lettersProposed = new ArrayList<>();
	private boolean turnLaunched = false;

	public ClientHandler(SocketChannel clientSocket) {
		this.clientChannel = clientSocket;
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
			throw new common.exception.IOException("Error in reading words file");
		}
		return word.toLowerCase();
	}

	private void letterAlreadyProposed() {
		this.prepareMessage(MessageType.ERRORLETTER);
	}

	private void notALetter() {
		this.prepareMessage(MessageType.NOTALETTER);
	}

	private void turnNotBegan() {
		this.prepareMessage(MessageType.ERRORTURN);
	}

	private void manageWord(Message message) {
		if (!this.turnLaunched) {
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
		if (!this.turnLaunched) {
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
				this.notALetter();
			} else {
				if (this.lettersProposed.contains(letter)) {
					this.letterAlreadyProposed();
				} else {
					this.lettersProposed.add(letter);
					if (this.chosenWord.contains(Character.toString(letter))) {
						for (int i = 0; i < chosenWord.length(); i++) {
							if (this.chosenWord.charAt(i) == letter) {
								this.numberOfLettersFound++;
								this.prepareMessage(Message.prepareMessage(MessageType.FIND, Character.toString(letter),
										Integer.toString(i)));
							}
						}
					} else {
						this.failedAttempt();
					}
					if (this.numberOfLettersFound == this.wordSize) {
						this.victory();
					}
					if (this.remainingFailedAttempts == 0) {
						this.defeat();
					}
				}
			}
		}
	}

	void receiveMessage() throws IOException {
		this.messageFromClient.clear();
		int numOfReadBytes = this.clientChannel.read(this.messageFromClient);
		if (numOfReadBytes == -1) {
			throw new IOException("Client has closed connection.");
		}
		String receivedString = extractMessageFromBuffer();
		System.out.println("message received by the server : " + receivedString);
		String[] messages = receivedString.split(MessageType.ENDMESSAGE);
		for (String singleMessage : messages) {
			this.messageHandler(singleMessage);
		}
	}

	private void messageHandler(String messageReceived) {
		Message message = new Message(messageReceived);
		switch (message.getMessageType()) {
		case MessageType.START:
			this.start(message);
			break;
		case MessageType.QUIT:
			this.disconnectClient();
			break;
		case MessageType.LETTER:
			this.manageLetter(message);
			break;
		case MessageType.WORD:
			this.manageWord(message);
			break;
		default:
			throw new MessageException("Invalid message received: " + message);
		}
	}

	private String extractMessageFromBuffer() {
		this.messageFromClient.flip();
		byte[] bytes = new byte[this.messageFromClient.remaining()];
		this.messageFromClient.get(bytes);
		return new String(bytes);
	}

	void disconnectClient() {
		try {
			this.clientChannel.close();
		} catch (IOException e) {
			System.err.println("Error in disconnection of the client");
		}
	}

	private void sendMessage(ByteBuffer message) {
		System.out.println("message sent by the server : " + new String(message.array()));
		try {
			this.clientChannel.write(message);
			if (message.hasRemaining()) {
				throw new MessageException("Server could not send message to client");
			}
		} catch (IOException e) {
			System.err.println("Server could not send message");
		}
	}

	public void sendAll() {
		ByteBuffer message = null;
		synchronized (this.messagesToSend) {
			while ((message = messagesToSend.peek()) != null) {
				this.sendMessage(message);
				this.messagesToSend.remove();
			}
		}
	}

	private void prepareMessage(String message) {
		ByteBuffer byteMessage = stringToByte(message);
		synchronized (this.messagesToSend) {
			this.messagesToSend.add(byteMessage);
		}
	}

	private ByteBuffer stringToByte(String message) {
		return ByteBuffer.wrap(message.getBytes());
	}

	private void defeat() {
		this.score--;
		this.prepareMessage(Message.prepareMessage(MessageType.DEFEAT, this.chosenWord, Integer.toString(this.score)));
		this.reset();
	}

	private void victory() {
		this.score++;
		this.prepareMessage(Message.prepareMessage(MessageType.VICTORY, this.chosenWord, Integer.toString(this.score)));
		this.reset();
	}

	private void reset() {
		this.chosenWord = "";
		this.remainingFailedAttempts = Integer.MAX_VALUE;
		this.numberOfLettersFound = Integer.MAX_VALUE;
		this.wordSize = Integer.MAX_VALUE;
		this.turnLaunched = false;
	}

	private void start(Message message) {
		if (!message.getMessageBody().isEmpty()) {
			throw new MessageException("Invalid START message received (no argument needed):" + message);
		}
		this.chosenWord = randomWord();
		this.wordSize = chosenWord.length();
		this.remainingFailedAttempts = wordSize;
		this.numberOfLettersFound = 0;
		this.prepareMessage(Message.prepareMessage(MessageType.WELCOME, Integer.toString(this.wordSize)));
		this.lettersProposed.clear();
		this.turnLaunched = true;
	}

	private void failedAttempt() {
		this.remainingFailedAttempts--;
		this.prepareMessage(
				Message.prepareMessage(MessageType.ATTEMPT, Integer.toString(this.remainingFailedAttempts)));
	}

}