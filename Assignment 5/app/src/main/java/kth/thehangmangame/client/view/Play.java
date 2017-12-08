package kth.thehangmangame.client.view;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import kth.thehangmangame.R;
import kth.thehangmangame.client.controller.Controller;
import kth.thehangmangame.client.net.OutputHandler;
import kth.thehangmangame.common.exception.MessageException;
import kth.thehangmangame.common.message.Message;
import kth.thehangmangame.common.message.MessageType;

/**
 * Created by lucieouvrier-buffet on 01/12/2017.
 */

public class Play extends AppCompatActivity {

    private static final int PORT = 1234;
    private static final String ADDRESS = "130.229.149.247";

    private Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Button quit = (Button) findViewById(R.id.quitPlay);
        quit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View login) {
                new quitGame().execute();
                finish();
            }
        });

        Button turn = (Button) findViewById(R.id.turn);
        turn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View login) {
                startTurn();
            }
        });

        Button guess = (Button) findViewById(R.id.guess);
        guess.setOnClickListener(new View.OnClickListener() {
            public void onClick(View login) {
                String guessValue = ((EditText) findViewById(R.id.guessValue)).getText().toString();
                guessButton(guessValue);
            }
        });

        this.controller = new Controller();
        this.controller.connect(ADDRESS, PORT, new OutputManager());
    }

    private void startTurn() {
        new StartTurn().execute();
    }

    private void guessButton(String guess) {
        TextView guessValue = findViewById(R.id.guessValue);
        String proposition = guessValue.getText().toString();
        new Guess(proposition).execute();
        guessValue.setText("");
    }

    private void setNumberOfRemainingFailedAttempts(int newNumber) {
        TextView attempts = findViewById(R.id.attemptsValue);
        attempts.setText(Integer.toString(newNumber));
    }

    private String getWord() {
        TextView word = findViewById(R.id.wordValue);
        return word.getText().toString();
    }

    private void setWord(String newWord) {
        TextView word = findViewById(R.id.wordValue);
        word.setText(newWord);
    }

    private void setScore(int newScore) {
        TextView score = findViewById(R.id.scoreValue);
        int oldScore = Integer.parseInt(score.getText().toString());
        if (!(newScore == oldScore - 1 || newScore == oldScore + 1)) {
            throw new MessageException("Invalid score received: must be the old score +/- 1");
        }
        score.setText(Integer.toString(newScore));
    }

    public class Guess extends AsyncTask<Void, Void, Void> {

        String proposition;

        public Guess(String proposition) {
            this.proposition = proposition;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            if (proposition.length() != 1 && proposition.length() != getWord().length() / 3) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), "The guess must be a letter or the entire word.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
            } else {
                if (proposition.length() == 1) {
                    controller.sendMessage(Message.prepareMessage(MessageType.LETTER, proposition));
                } else {
                    controller.sendMessage(Message.prepareMessage(MessageType.WORD, proposition));
                }
            }
            return null;
        }

    }

    private class StartTurn extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            controller.start();
            return null;
        }
    }

    private class quitGame extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            controller.disconnect();
            return null;
        }
    }

    private class OutputManager implements OutputHandler {

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
            if (!(position >= 0 && position < getWord().length() / 3)) {
                throw new MessageException("Invalid position received (not in boundaries):" + message);
            }
            this.setLetter(letter, position);
        }

        private void setLetter(char letter, int position) {
            final StringBuilder newWord = new StringBuilder(getWord());
            newWord.setCharAt(1 + 3 * position, letter);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setWord(newWord.toString());
                }
            });
        }

        private void errorLetter(Message message) {
            if (!message.getMessageBody().isEmpty()) {
                throw new MessageException("Invalid ERRORLETTER received (no argument needed): " + message);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(), "The letter has already been proposed.", Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }

        private void errorTurn(Message message) {
            if (!message.getMessageBody().isEmpty()) {
                throw new MessageException("Invalid ERRORTURN received (no argument needed): " + message);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(), "Start a new turn before guessing.", Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }

        private void notALetter(Message message) {
            if (!message.getMessageBody().isEmpty()) {
                throw new MessageException("Invalid ERRORTURN received (no argument needed): " + message);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(), "The guess must be a letter.", Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }

        private void victory(Message message) {
            if (message.getMessageBody().size() != 2) {
                throw new MessageException("Invalid VICTORY message received (2 arguments needed):" + message);
            }
            String finalWord = message.getMessageBody().get(0);
            if (finalWord.length() != getWord().length() / 3) {
                System.err.println("Invalid final word received (wrong size):" + message);
            }
            final int score;
            try {
                score = Integer.parseInt(message.getMessageBody().get(1));
            } catch (NumberFormatException e) {
                throw new MessageException("Invalid score received (not an integer):" + message);
            }
            final StringBuilder wordPrepared = new StringBuilder();
            for (int i = 0; i < finalWord.length(); i++) {
                wordPrepared.append(" " + finalWord.charAt(i) + " ");
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setWord(wordPrepared.toString());
                    setNumberOfRemainingFailedAttempts(-1);
                    setScore(score);
                    Toast toast = Toast.makeText(getApplicationContext(), "Congratulations! You guessed the word.", Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }

        private void welcome(Message message) {
            if (message.getMessageBody().size() != 1) {
                throw new MessageException("Invalid WELCOME message received (one argument needed):" + message);
            }
            final int wordSize;
            try {
                wordSize = Integer.parseInt(message.getMessageBody().get(0));
            } catch (NumberFormatException e) {
                throw new MessageException("The size of the word received is invalid (not an integer):" + message);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setNumberOfRemainingFailedAttempts(wordSize);
                    StringBuilder wordPrepared = new StringBuilder();
                    for (int i = 0; i < wordSize; i++) {
                        wordPrepared.append(" _ ");
                    }
                    setWord(wordPrepared.toString());
                }
            });
        }

        private void attempt(Message message) {
            if (message.getMessageBody().size() != 1) {
                System.err.println("Invalid ATTEMPT received (1 argument needed):" + message);
            }
            final int newScore;
            try {
                newScore = Integer.parseInt(message.getMessageBody().get(0));
            } catch (NumberFormatException e) {
                throw new MessageException("Invalid number of remaining failed attempts received (not an integer):" + message);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setNumberOfRemainingFailedAttempts(newScore);
                }
            });
        }

        private void defeat(Message message) {
            if (message.getMessageBody().size() != 2) {
                System.err.println("Invalid DEFEAT message received (2 arguments needed):" + message);
            }
            String finalWord = message.getMessageBody().get(0);
            if (finalWord.length() != getWord().length() / 3) {
                throw new MessageException("Invalid final word received (wrong size):" + message);
            }
            final int score;
            try {
                score = Integer.parseInt(message.getMessageBody().get(1));
            } catch (NumberFormatException e) {
                throw new MessageException("Invalid score received (not an integer):" + message);
            }
            final StringBuilder wordPrepared = new StringBuilder();
            for (int i = 0; i < finalWord.length(); i++) {
                wordPrepared.append(" " + finalWord.charAt(i) + " ");
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setWord(wordPrepared.toString());
                    setNumberOfRemainingFailedAttempts(-1);
                    setScore(score);
                    Toast toast = Toast.makeText(getApplicationContext(), "Sorry... You didn't guess the word in time.", Toast.LENGTH_LONG);
                    toast.show();
                }
            });

        }

        public void handleMessage(String messageReceived) {
            Message message = new Message(messageReceived);
            switch (message.getMessageType()) {
                case MessageType.WELCOME:
                    welcome(message);
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
