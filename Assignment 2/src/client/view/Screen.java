package client.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import client.net.CommunicationListener;
import client.net.ServerConnection;
import common.exception.MessageException;
import common.message.Message;
import common.message.MessageType;

public class Screen implements Runnable {

	private static final int PORT = 8080;
	private static final String ADDRESS = "localhost";

	private ServerConnection server;

	private JFrame frame;
	private JLabel currentWord = new JLabel("");
	private JLabel failedAttemptsRemainingNumber = new JLabel("-1");
	private JLabel scoreValue = new JLabel("0");
	private boolean receivingMessages = false;

	public void start() {

		if (this.receivingMessages) {
			return;
		}
		this.receivingMessages = true;

		// Server
		this.server = new ServerConnection();

		// View
		// new Thread(this).start();
		this.initialize();

		// Automatic connection
		this.server.connect(ADDRESS, PORT, new Listener());

	}

	@Override
	public void run() {
		while (this.receivingMessages) {
			// Do something?
		}
	}

	private void initialize() {
		this.frame = new JFrame();
		this.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					receivingMessages = false;
					server.disconnect();
				} catch (IOException e1) {
					throw new common.exception.ConnectionException("Error while deconnecting client from server");
				}
			}
		});
		this.frame.setBounds(100, 100, 450, 300);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.getContentPane().setLayout(new BorderLayout(0, 0));

		JLabel hangman = new JLabel("The Hangman game");
		hangman.setHorizontalAlignment(SwingConstants.CENTER);
		this.frame.getContentPane().add(hangman, BorderLayout.NORTH);

		JPanel send = new JPanel();
		this.frame.getContentPane().add(send, BorderLayout.SOUTH);
		send.setLayout(new BoxLayout(send, BoxLayout.Y_AXIS));

		JPanel guess = new JPanel();
		send.add(guess);

		JTextField guessWord = new JTextField("");
		guessWord.setColumns(26);
		guess.add(guessWord);

		JButton guessButton = new JButton("Guess");
		guessButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				String proposition = guessWord.getText();
				guess(proposition);
				guessWord.setText("");
			}
		});
		guess.add(guessButton);

		JPanel buttons = new JPanel();
		send.add(buttons);

		JButton start = new JButton("Start a new turn");
		start.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				server.startGame();
			}
		});
		buttons.add(start);

		JButton stop = new JButton("Stop the game");
		stop.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				try {
					receivingMessages = false;
					server.disconnect();
				} catch (IOException e1) {
					throw new common.exception.ConnectionException("Error while deconnecting client from server");
				}
				frame.dispose();
			}
		});
		buttons.add(stop);

		JPanel game = new JPanel();
		this.frame.getContentPane().add(game, BorderLayout.CENTER);
		game.setLayout(new BoxLayout(game, BoxLayout.Y_AXIS));

		Component rigidArea = Box.createRigidArea(new Dimension(20, 20));
		game.add(rigidArea);

		JPanel word = new JPanel();
		game.add(word);

		JLabel labelWord = new JLabel("Word = ");
		word.add(labelWord);

		word.add(this.currentWord);

		JPanel attempts = new JPanel();
		game.add(attempts);

		JLabel labelAttempts = new JLabel("Number of remaining failed attempts = ");
		attempts.add(labelAttempts);

		attempts.add(this.failedAttemptsRemainingNumber);

		JPanel score = new JPanel();
		game.add(score);

		JLabel scoreLabel = new JLabel("Score = ");
		score.add(scoreLabel);

		score.add(this.scoreValue);

		this.frame.setVisible(true);
	}

	private void guess(String proposition) {
		if (proposition.length() != 1 && proposition.length() != currentWord.getText().length() / 3) {
			JOptionPane error = new JOptionPane();
			error.showMessageDialog(null, "The guess must be a letter or the entire word", "Wrong guess",
					JOptionPane.ERROR_MESSAGE);

		} else {
			if (proposition.length() == 1) {
				this.sendMessage(Message.prepareMessage(MessageType.LETTER, proposition));
			} else {
				this.sendMessage(Message.prepareMessage(MessageType.WORD, proposition));
			}
		}

	}

	private void sendMessage(String message) {
		this.server.sendMessage(message);
	}

	private class Listener implements CommunicationListener {

		private void setLetter(char letter, int position) {
			StringBuilder newWord = new StringBuilder(currentWord.getText());
			newWord.setCharAt(1 + 3 * position, letter);
			currentWord.setText(newWord.toString());
		}

		private void setWord(String newWord) {
			currentWord.setText(newWord);
		}

		private void setScore(int newScore) {
			int oldScore = Integer.parseInt(scoreValue.getText());
			if (!(newScore == oldScore - 1 || newScore == oldScore + 1)) {
				throw new MessageException("Invalid score received: must be the old score +/- 1");
			}
			scoreValue.setText(Integer.toString(newScore));
		}

		private void setNumberOfRemainingFailedAttempts(int newNumber) {
			failedAttemptsRemainingNumber.setText(Integer.toString(newNumber));
		}

		private void welcome(Message message) {
			if (message.getMessageBody().size() != 1) {
				throw new MessageException("Invalid WELCOME message received (one argument needed):" + message);
			}
			int wordSize;
			try {
				wordSize = Integer.parseInt(message.getMessageBody().get(0));
			} catch (NumberFormatException e) {
				throw new MessageException("The size of the word received is invalid (not an integer):" + message);
			}
			this.setNumberOfRemainingFailedAttempts(wordSize);
			StringBuilder wordPrepared = new StringBuilder();
			for (int i = 0; i < wordSize; i++) {
				wordPrepared.append(" _ ");
			}
			this.setWord(wordPrepared.toString());
		}

		private void defeat(Message message) {
			if (message.getMessageBody().size() != 2) {
				throw new MessageException("Invalid DEFEAT message received (2 arguments needed):" + message);
			}
			String finalWord = message.getMessageBody().get(0);
			if (finalWord.length() != currentWord.getText().length() / 3) {
				throw new MessageException("Invalid final word received (wrong size):" + message);
			}
			int score;
			try {
				score = Integer.parseInt(message.getMessageBody().get(1));
			} catch (NumberFormatException e) {
				throw new MessageException("Invalid score received (not an integer):" + message);
			}
			StringBuilder wordPrepared = new StringBuilder();
			for (int i = 0; i < finalWord.length(); i++) {
				wordPrepared.append(" " + finalWord.charAt(i) + " ");
			}
			this.setWord(wordPrepared.toString());
			this.setNumberOfRemainingFailedAttempts(-1);
			this.setScore(score);
		}

		private void victory(Message message) {
			if (message.getMessageBody().size() != 2) {
				throw new MessageException("Invalid VICTORY message received (2 arguments needed):" + message);
			}
			String finalWord = message.getMessageBody().get(0);
			if (finalWord.length() != currentWord.getText().length() / 3) {
				throw new MessageException("Invalid final word received (wrong size):" + message);
			}
			int score;
			try {
				score = Integer.parseInt(message.getMessageBody().get(1));
			} catch (NumberFormatException e) {
				throw new MessageException("Invalid score received (not an integer):" + message);
			}
			StringBuilder wordPrepared = new StringBuilder();
			for (int i = 0; i < finalWord.length(); i++) {
				wordPrepared.append(" " + finalWord.charAt(i) + " ");
			}
			this.setWord(wordPrepared.toString());
			this.setNumberOfRemainingFailedAttempts(-1);
			this.setScore(score);
		}

		private void attempt(Message message) {
			if (message.getMessageBody().size() != 1) {
				throw new MessageException("Invalid ATTEMPT received (1 argument needed):" + message);
			}
			int newScore;
			try {
				newScore = Integer.parseInt(message.getMessageBody().get(0));
			} catch (NumberFormatException e) {
				throw new MessageException(
						"Invalid number of remaining failed attempts received (not an integer):" + message);
			}
			this.setNumberOfRemainingFailedAttempts(newScore);
		}

		private void find(Message message) {
			if (message.getMessageBody().size() != 2) {
				throw new MessageException("Invalid FIND message received (2 arguments needed):" + message);
			}
			if (message.getMessageBody().get(0).length() != 1) {
				throw new MessageException("Invalid letter received (not 1 character):" + message);
			}
			char letter = message.getMessageBody().get(0).charAt(0);
			if (!Character.isLetter(letter)) {
				throw new MessageException("Invalid letter received (not a letter)" + message);
			}
			int position;
			try {
				position = Integer.parseInt(message.getMessageBody().get(1));
			} catch (NumberFormatException e) {
				throw new MessageException("Invalid position received (not an integer):" + message);
			}
			if (!(position >= 0 && position < currentWord.getText().length() / 3)) {
				throw new MessageException("Invalid position received (not in boundaries):" + message);
			}
			this.setLetter(letter, position);
		}

		private void errorLetter(Message message) {
			if (!message.getMessageBody().isEmpty()) {
				throw new MessageException("Invalid ERRORLETTER received (no argument needed): " + message);
			}
			JOptionPane error = new JOptionPane();
			error.showMessageDialog(null, "The letter has already been proposed", "Wrong letter",
					JOptionPane.ERROR_MESSAGE);
		}

		private void errorTurn(Message message) {
			if (!message.getMessageBody().isEmpty()) {
				throw new MessageException("Invalid ERRORTURN received (no argument needed): " + message);
			}
			JOptionPane error = new JOptionPane();
			error.showMessageDialog(null, "Start a new turn before guessing", "Guess not possible",
					JOptionPane.ERROR_MESSAGE);
		}

		private void notALetter(Message message) {
			if (!message.getMessageBody().isEmpty()) {
				throw new MessageException("Invalid ERRORTURN received (no argument needed): " + message);
			}
			JOptionPane error = new JOptionPane();
			error.showMessageDialog(null, "The guess must be a letter", "Not a letter", JOptionPane.ERROR_MESSAGE);
		}

		@Override
		public void receivedMessage(String messageReceived) {
			Message message = new Message(messageReceived);
			switch (message.getMessageType()) {
			case MessageType.WELCOME:
				this.welcome(message);
				break;
			case MessageType.ATTEMPT:
				this.attempt(message);
				break;
			case MessageType.DEFEAT:
				this.defeat(message);
				break;
			case MessageType.VICTORY:
				this.victory(message);
				break;
			case MessageType.FIND:
				this.find(message);
				break;
			case MessageType.ERRORLETTER:
				this.errorLetter(message);
				break;
			case MessageType.ERRORTURN:
				this.errorTurn(message);
				break;
			case MessageType.NOTALETTER:
				this.notALetter(message);
				break;
			default:
				throw new MessageException("Invalid message received: " + message);
			}
		}
	}

}