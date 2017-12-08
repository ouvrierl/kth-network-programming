package kth.thehangmangame.client.net;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import kth.thehangmangame.common.exception.ConnectionException;
import kth.thehangmangame.common.message.Message;
import kth.thehangmangame.common.message.MessageType;

/**
 * Created by lucieouvrier-buffet on 08/12/2017.
 */

public class ServerConnection extends AsyncTask<Object, Void, Void> {

    BufferedReader input;
    PrintWriter output;
    Socket socket = null;
    boolean connected = true;

    @Override
    protected Void doInBackground(Object... args) {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress((String) args[0], (int) args[1]));
            output = new PrintWriter(socket.getOutputStream());
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(new Listener((OutputHandler) args[2])).start();
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

        OutputHandler outputHandler;

        public Listener(OutputHandler outputHandler) {
            this.outputHandler = outputHandler;
        }

        @Override
        public void run() {
            try {
                for (; ; ) {
                    String message = input.readLine();
                    this.outputHandler.handleMessage(message);
                }
            } catch (Exception e) {
                if (connected) {
                    e.printStackTrace();
                }
            }
        }

    }

}
