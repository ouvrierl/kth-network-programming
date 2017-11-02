package server.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.Socket;

import common.MessageException;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private boolean connected;
    private String chosenWord;
    private int remainingFailedAttempts;
    private int numberOfLettersFound;
    
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        connected = true;
    }
    
    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream());
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        while (connected) {
            try {
                String message = input.readLine();
                if(message == null){
                	message = "";
                }
                if(message.startsWith("START")){
                	chosenWord = randomWord().toLowerCase();
                	System.out.println(chosenWord);
                	remainingFailedAttempts = chosenWord.length();
                	numberOfLettersFound = 0;
                	output.println("WELCOME " + chosenWord.length());
                	output.flush();
                }
                else if(message.equals("QUIT")){
                	disconnectClient();
                }
                else if(message.startsWith("LETTER")){
                	char letter = message.substring(7).charAt(0);
                	boolean goodLetter = false;
                	for(int i = 0; i < chosenWord.length(); i++){
                		if(Character.toLowerCase(chosenWord.charAt(i)) == Character.toLowerCase(letter)){
                			numberOfLettersFound++;
                			output.println("FIND " + Character.toLowerCase(letter) + " " + i);
                			output.flush();
                			goodLetter = true;
                		}
                	}
                	if(!goodLetter){
                		remainingFailedAttempts--;
                    	output.println("ATTEMPT");
                    	output.flush();
                	}
                	if(numberOfLettersFound == chosenWord.length()){
                		output.println("VICTORY " + chosenWord);
                		output.flush();
                		chosenWord = "";
                		remainingFailedAttempts = Integer.MAX_VALUE;
                		numberOfLettersFound = Integer.MAX_VALUE;
                	}
                	if(remainingFailedAttempts == 0){
                		output.println("DEFEAT" + chosenWord);
                		output.flush();
                		chosenWord = "";
                		remainingFailedAttempts = Integer.MAX_VALUE;
                		numberOfLettersFound = Integer.MAX_VALUE;
                	}
                }
                else if(message.startsWith("WORD")){
                	String wordProposed = message.substring(5);
                	if(wordProposed.equalsIgnoreCase(chosenWord)){
                		output.println("VICTORY " + chosenWord);
                		output.flush();
                		chosenWord = "";
                		remainingFailedAttempts = Integer.MAX_VALUE;
                		numberOfLettersFound = Integer.MAX_VALUE;
                	}
                	else {
                		remainingFailedAttempts--;
                    	output.println("ATTEMPT");
                    	output.flush();
                	}
                	if(remainingFailedAttempts == 0){
                		output.println("DEFEAT" + chosenWord);
                		output.flush();
                		chosenWord = "";
                		remainingFailedAttempts = Integer.MAX_VALUE;
                		numberOfLettersFound = Integer.MAX_VALUE;
                	}
                }
            } catch (IOException ioe) {
                disconnectClient();
                throw new MessageException(ioe);
            }
        }
    }
    
    private String randomWord() throws IOException{
    	String word = "";
    	BufferedReader words = new BufferedReader(new FileReader(new File("src/server/net/words.txt")));
    	String line = words.readLine();
    	int stop = (int)(51527 * Math.random());
    	int i = 0;
    	while(line != null){
    		if(i == stop){
    			word = line;
    			break;
    		}
    		line = words.readLine();
    		i++;
    	}
    	return word;
    }
    
    private void disconnectClient() {
        try {
            clientSocket.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        connected = false;
    }
    
}