package server.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
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
						default:
							throw new MessageException("Invalid message received: " + message);
						}
					}
				} else if (type == 1) {
					long size = this.input.readLong();
					String name = this.input.readUTF();
					File file = new File(FILES_DIRECTORY + name);
					FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
					byte[] buffer = new byte[Constants.BUFFER_SIZE];
					int read = 0;
					long remaining = size;
					while ((read = this.input.read(buffer, 0, Math.min(buffer.length, (int) remaining))) > 0) {
						remaining -= read;
						fos.write(buffer, 0, read);
					}
					fos.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				this.quit();
				throw new MessageException(e.getMessage());
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

}