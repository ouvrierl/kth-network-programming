package kth.thehangmangame.client.controller;

import kth.thehangmangame.client.net.OutputHandler;
import kth.thehangmangame.client.net.ServerConnection;

/**
 * Created by lucieouvrier-buffet on 08/12/2017.
 */

public class Controller {

    private ServerConnection serverConnection;

    public Controller() {
        this.serverConnection = new ServerConnection();
    }

    public void connect(String host, int port, OutputHandler outputHandler) {
        this.serverConnection.execute(host, port, outputHandler);
    }

    public void disconnect() {
        this.serverConnection.disconnect();
    }

    public void start() {
        this.serverConnection.start();
    }

    public void sendMessage(String message) {
        this.serverConnection.sendMessage(message);
    }

}