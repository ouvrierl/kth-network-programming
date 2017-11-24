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

	private static final String FILES_DIRECTORY = "./files/";
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
			throw new ConnectionException("Error during the server connection");
		}
		while (this.connected) {
			try {
				int type = this.input.read();
				if (type == 0) {
					String message = this.input.readUTF();
					if (message != null && !message.equals("")) {
						switch (message) {
						case Constants.QUIT:
							this.quit();
							break;
						case Constants.FILE:
							long size = this.input.readLong();
							String name = this.input.readUTF();
							File file = new File(FILES_DIRECTORY + name);
							FileOutputStream fos = null;
							if (!file.exists()) {
								fos = new FileOutputStream(file.getAbsolutePath());
							}
							byte[] buffer = new byte[Constants.BUFFER_SIZE];
							int read = 0;
							long remaining = size;
							while ((read = this.input.read(buffer, 0, Math.min(buffer.length, (int) remaining))) > 0) {
								remaining -= read;
								if (!file.exists()) {
									fos.write(buffer, 0, read);
								}
							}
							if (!file.exists()) {
								fos.close();
							}
							break;
						default:
							throw new MessageException("Invalid message received: " + message);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
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

	private void sendMessage(String message) {
		try {
			this.output.writeUTF(message);
			this.output.flush();
		} catch (Exception e) {
			throw new MessageException("Error while sending message to server.");
		}
	}

	public void sendFile(String name) {
		File file = new File(FILES_DIRECTORY + name);
		try (FileInputStream fis = new FileInputStream(file);) {
			this.output.writeLong(file.length());
			this.output.flush();
			this.output.writeUTF(file.getName());
			this.output.flush();
			byte[] buffer = new byte[Constants.BUFFER_SIZE];
			while (fis.read(buffer) > 0) {
				this.output.write(buffer);
				this.output.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new MessageException("Error while sending file to server.");
		}
	}

}