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
import java.util.concurrent.ForkJoinPool;

import common.exception.MessageException;
import common.message.Message;
import common.message.MessageType;

public class ClientHandler implements Runnable {

	private static final String WORDS_FILE = "src/server/net/words.txt";

	private final SocketChannel clientChannel;
	private final ByteBuffer messageFromClient = ByteBuffer.allocateDirect(MessageType.MESSAGELENGTH);
	private final Queue<String> messages = new ArrayDeque<>();
	private StringBuilder receivedCharacters = new StringBuilder();
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

	@Override
	public void run() {
		while (!messages.isEmpty()) {
			try {
				String messageReceived = messages.poll();
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
			} catch (Exception e) {
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
			throw new common.exception.IOException("Error in reading words file");
		}
		return word.toLowerCase();
	}

	private void letterAlreadyProposed() {
		this.sendMessage(MessageType.ERRORLETTER);
	}

	private void notALetter() {
		this.sendMessage(MessageType.NOTALETTER);
	}

	private void turnNotBegan() {
		this.sendMessage(MessageType.ERRORTURN);
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
								this.sendMessage(Message.prepareMessage(MessageType.FIND, Character.toString(letter),
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
		int numOfReadBytes;
		numOfReadBytes = clientChannel.read(messageFromClient);
		if (numOfReadBytes == -1) {
			throw new IOException("Client has closed connection.");
		}
		String receivedString = extractMessageFromBuffer();
		this.receivedCharacters.append(receivedString);
		while (extractMsg())
			;
		ForkJoinPool.commonPool().execute(this);
	}

	private boolean extractMsg() {
		String allRecvdChars = receivedCharacters.toString();
		String[] splitAtHeader = allRecvdChars.split(MessageType.ENDMESSAGE);
		if (splitAtHeader.length < 2) {
			return false;
		}
		String lengthHeader = splitAtHeader[0];
		int lengthOfFirstMsg = Integer.parseInt(lengthHeader);
		if (hasCompleteMsg(lengthOfFirstMsg, splitAtHeader[1])) {
			String completeMsg = splitAtHeader[1].substring(0, lengthOfFirstMsg);
			messages.add(completeMsg);
			receivedCharacters.delete(0, lengthHeader.length() + 1 + lengthOfFirstMsg);
			return true;
		}
		return false;
	}

	private boolean hasCompleteMsg(int msgLen, String recvd) {
		return recvd.length() >= msgLen;
	}

	private String extractMessageFromBuffer() {
		messageFromClient.flip();
		byte[] bytes = new byte[messageFromClient.remaining()];
		messageFromClient.get(bytes);
		return new String(bytes);
	}

	void disconnectClient() throws IOException {
		this.clientChannel.close();
	}

	private void sendMessage(String message) {
		ByteBuffer byteMessage = stringToByte(message);
		try {
			clientChannel.write(byteMessage);
		} catch (IOException e) {
			throw new common.exception.IOException("Error while writing server output");
		}
	}

	private ByteBuffer stringToByte(String message) {
		return ByteBuffer.wrap(message.getBytes());
	}

	private void defeat() {
		this.score--;
		this.sendMessage(Message.prepareMessage(MessageType.DEFEAT, this.chosenWord, Integer.toString(this.score)));
		this.reset();
	}

	private void victory() {
		this.score++;
		this.sendMessage(Message.prepareMessage(MessageType.VICTORY, this.chosenWord, Integer.toString(this.score)));
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
		this.sendMessage(Message.prepareMessage(MessageType.WELCOME, Integer.toString(this.wordSize)));
		this.lettersProposed.clear();
		this.turnLaunched = true;
	}

	private void failedAttempt() {
		this.remainingFailedAttempts--;
		this.sendMessage(Message.prepareMessage(MessageType.ATTEMPT, Integer.toString(this.remainingFailedAttempts)));
	}

}