package client.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import common.constants.Constants;
import common.exception.ConnectionException;
import common.exception.MessageException;

public class ServerConnection implements Runnable {

	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	private boolean connected = true;
	private File downloadFile;

	public void setDownloadFile(File downloadFile) {
		this.downloadFile = downloadFile;
	}

	public void connect(String host, int port) {
		try {
			this.socket = new Socket();
			this.socket.connect(new InetSocketAddress(host, port));
			this.input = new DataInputStream(this.socket.getInputStream());
			this.output = new DataOutputStream(this.socket.getOutputStream());
			new Thread(this).start(); // Communication established for each
			// client
		} catch (Exception e) {
			throw new ConnectionException("Error in connecting the client socket");
		}
	}

	public void disconnect() {
		this.connected = false;
		this.sendMessage(Constants.QUIT);
		try {
			this.socket.close();
		} catch (Exception e) {
			throw new ConnectionException("Error in closing the client socket");
		}
		this.socket = null;
	}

	public void sendMessage(String message) {
		try {
			this.output.writeUTF(message);
			this.output.flush();
		} catch (IOException e) {
			throw new MessageException("Error while sending message to server.");
		}
	}

	public void sendFile(File file, String outputName) {
		try (FileInputStream inf = new FileInputStream(file);) {
			this.output.writeUTF(Constants.FILE);
			this.output.flush();
			this.output.writeUTF(outputName);
			this.output.flush();
			this.output.writeLong(file.length());
			this.output.flush();
			byte buffer[] = new byte[Constants.BUFFER_SIZE];
			int n;
			while ((n = inf.read(buffer)) != -1) {
				this.output.write(buffer, 0, n);
				this.output.flush();
			}
		} catch (Exception e) {
			throw new MessageException("Error while sending file to server.");
		}
	}

	@Override
	public void run() {
		try {
			for (;;) {
				long size = input.readLong();
				int n = 0;
				byte[] buffer = new byte[Constants.BUFFER_SIZE];
				try (FileOutputStream out = new FileOutputStream(this.downloadFile);) {
					while (size > 0 && (n = this.input.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
						out.write(buffer, 0, n);
						size -= n;
					}
				}
			}
		} catch (Exception e) {
			if (this.connected) {
				throw new MessageException("Error while receiving message from the server.");
			}
		}
	}

}
