package client.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import common.ConnectionException;
import common.IOException;

public class ServerConnection {
	private Socket socket;
	private BufferedReader input;
	public PrintWriter output;
	private volatile boolean connected;

	public void connect(String host, int port, OutputHandler outputHandler) {
		try {
			this.socket = new Socket();
			this.socket.connect(new InetSocketAddress(host, port));
			this.connected = true;
			this.output = new PrintWriter(socket.getOutputStream());
			this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			new Thread(new Listener(outputHandler)).start();
		} catch (Exception e) {
			throw new ConnectionException("Error in connecting the client socket");
		}
	}

	public void disconnect() {
		this.sendMessage("QUIT");
		try {
			this.socket.close();
		} catch (Exception e) {
			throw new ConnectionException("Error in closing the client socket");
		}
		this.socket = null;
		this.connected = false;
	}

	public void sendMessage(String message) {
		this.output.println(message);
		this.output.flush();
	}

	private class Listener implements Runnable {

		OutputHandler outputHandler;

		private Listener(OutputHandler outputHandler) {
			this.outputHandler = outputHandler;
		}

		@Override
		public void run() {
			try {
				for (;;) {
					String message = input.readLine();
					outputHandler.handleMessage(message);
				}
			} catch (Exception e) {
				if (connected) {
					throw new IOException("Error in reading client input");
				}
			}
		}

	}
}
