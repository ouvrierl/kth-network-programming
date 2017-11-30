package server.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;

import common.constants.Constants;
import common.exception.ConnectionException;
import common.exception.IOException;
import common.exception.MessageException;

public class ClientHandler implements Runnable {

	public static final String FILES_DIRECTORY = "./files/";
	private final Socket clientSocket;
	private DataInputStream input;
	private DataOutputStream output;
	private boolean connected = true;

	public ClientHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	@Override
	public void run() {
		try {
			this.input = new DataInputStream(this.clientSocket.getInputStream());
			this.output = new DataOutputStream(this.clientSocket.getOutputStream());
		} catch (Exception e) {
			throw new ConnectionException("Error during the server connection.");
		}
		while (this.connected) {
			try {
				String message = this.input.readUTF();
				if (message != null && !message.equals("")) {
					switch (message) {
					case Constants.QUIT:
						this.quit();
						break;
					case Constants.FILE:
						String name = this.input.readUTF();
						long size = this.input.readLong();
						File file = new File(FILES_DIRECTORY + name);
						try (FileOutputStream out = new FileOutputStream(file);) {
							int n = 0;
							byte[] buffer = new byte[Constants.BUFFER_SIZE];
							while (size > 0
									&& (n = this.input.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
								out.write(buffer, 0, n);
								size -= n;
							}

						}
						break;
					default:
						throw new MessageException("Invalid message received: " + message);
					}
				}
			} catch (Exception e) {
				this.quit();
				throw new MessageException("Error while receiving message from the client.");
			}
		}
	}

	private void quit() {
		try {
			this.clientSocket.close();
		} catch (Exception e) {
			throw new IOException("Error in closing the client socket");
		}
		this.connected = false;
	}

	public void sendFile(String name) {
		File file = new File(FILES_DIRECTORY + name);
		try (FileInputStream inf = new FileInputStream(file);) {
			this.output.writeLong(file.length());
			this.output.flush();
			byte buffer[] = new byte[Constants.BUFFER_SIZE];
			int n;
			while ((n = inf.read(buffer)) != -1) {
				this.output.write(buffer, 0, n);
				this.output.flush();
			}
		} catch (Exception e) {
			throw new MessageException("Error while server sending file.");
		}
	}

}