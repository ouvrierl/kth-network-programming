package client.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import client.controller.Controller;

public class Screen {

	private JFrame frame;
	private JLabel currentWord;
	private JLabel failedAttemptsRemainingNumber;
	private JLabel scoreValue;
	private List<Character> lettersProposed = new ArrayList<>();
	private Controller controller;

	/**
	 * Create the application.
	 */
	public Screen() {
	}

	public void startController() {
		this.controller = new Controller(this);
		this.controller.connect("localhost", 8080);
	}

	public void startView() {
		this.currentWord = new JLabel("");
		this.failedAttemptsRemainingNumber = new JLabel("-1");
		this.scoreValue = new JLabel("0");
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				controller.sendMessage("QUIT");
			}
		});
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		JLabel hangman = new JLabel("The Hangman game");
		hangman.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(hangman, BorderLayout.NORTH);

		JPanel send = new JPanel();
		frame.getContentPane().add(send, BorderLayout.SOUTH);
		send.setLayout(new BoxLayout(send, BoxLayout.Y_AXIS));

		JPanel guess = new JPanel();
		send.add(guess);

		JTextField guessWord = new JTextField();
		guessWord.setColumns(26);
		guess.add(guessWord);

		JButton guessButton = new JButton("Guess");
		guessButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				String proposition = guessWord.getText();
				if (proposition.length() != 1 && proposition.length() != currentWord.getText().length() / 3) {
					JOptionPane error = new JOptionPane();
					error.showMessageDialog(null, "The guess must be a letter or the entire word", "Wrong guess",
							JOptionPane.ERROR_MESSAGE);
					guessWord.setText("");
					return;
				}
				guessWord.setText("");
				if (proposition.length() == 1) {
					if (lettersProposed.contains(proposition.charAt(0))) {
						JOptionPane error = new JOptionPane();
						error.showMessageDialog(null, "The letter has already been proposed", "Wrong letter",
								JOptionPane.ERROR_MESSAGE);
						guessWord.setText("");
						return;
					} else {
						lettersProposed.add(proposition.charAt(0));
					}
					controller.sendMessage("LETTER " + proposition);
				} else {
					controller.sendMessage("WORD " + proposition);
				}
			}
		});
		guess.add(guessButton);

		JPanel buttons = new JPanel();
		send.add(buttons);

		JButton start = new JButton("Start a new turn");
		start.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				controller.sendMessage("START");
				lettersProposed.clear();
			}
		});
		buttons.add(start);

		JButton stop = new JButton("Stop the game");
		stop.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				controller.sendMessage("QUIT");
				frame.dispose();
			}
		});
		buttons.add(stop);

		JPanel game = new JPanel();
		frame.getContentPane().add(game, BorderLayout.CENTER);
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

		frame.setVisible(true);
	}

	public void setWord(String newWord) {
		currentWord.setText(newWord);
	}

	public void setScore(int newScore) {
		scoreValue.setText(Integer.toString(newScore));
	}

	public void setNumberOfRemainingFailedAttempts(int newNumber) {
		failedAttemptsRemainingNumber.setText(Integer.toString(newNumber));
	}
	
	public void setLetter(char letter, int position){
		StringBuilder newWord = new StringBuilder(currentWord.getText());
		newWord.setCharAt(1 + 3 * position, letter);
		currentWord.setText(newWord.toString());
	}

}
