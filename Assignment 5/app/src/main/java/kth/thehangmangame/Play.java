package kth.thehangmangame;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by lucieouvrier-buffet on 01/12/2017.
 */

public class Play extends AppCompatActivity {

    private Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Button quit = (Button) findViewById(R.id.quitPlay);
        quit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View login) {
                client.disconnect();
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

        this.client = new Client();
        this.client.execute();
    }

    private void startTurn() {
        new StartTurn(this.client).execute();
    }

    private void guessButton(String guess) {
        TextView guessValue = findViewById(R.id.guessValue);
        String proposition = guessValue.getText().toString();
        new Guess(proposition, this.client).execute();
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

    private class StartTurn extends AsyncTask<Void, Void, Void> {

        Client client;

        public StartTurn(Client client) {
            this.client = client;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            this.client.start();
            return null;
        }
    }

    private class Guess extends AsyncTask<Void, Void, Void> {

        String proposition;
        Client client;

        public Guess(String proposition, Client client) {
            this.proposition = proposition;
            this.client = client;
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
                    this.client.sendMessage(Message.prepareMessage(MessageType.LETTER, proposition));
                } else {
                    this.client.sendMessage(Message.prepareMessage(MessageType.WORD, proposition));
                }
            }
            return null;
        }

    }

    private class Client extends AsyncTask<Void, Void, Void> {

        private final static String ADDRESS = "130.229.153.3";
        private final static int PORT = 1234;
        BufferedReader input;
        PrintWriter output;
        Socket socket = null;
        boolean connected = true;

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ADDRESS, PORT));
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                new Thread(new Listener()).start();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void start() {
            this.sendMessage(Message.prepareMessage(MessageType.START));
        }

        public void disconnect() {
            this.connected = false;
            this.sendMessage(Message.prepareMessage(MessageType.QUIT));
            try {
                this.socket.close();
            } catch (Exception e) {
                throw new ConnectionException("Error in closing the client socket");
            }
            this.socket = null;
        }

        public void sendMessage(String message) {
            this.output.println(message);
            this.output.flush();
        }

        private class Listener implements Runnable {

            @Override
            public void run() {
                try {
                    for (; ; ) {
                        String message = input.readLine();
                        this.handleMessage(message);
                    }
                } catch (Exception e) {
                    if (connected) {
                        e.printStackTrace();
                    }
                }
            }

            private void handleMessage(String messageReceived) {
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
                    }
                });
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

        }

    }

}
