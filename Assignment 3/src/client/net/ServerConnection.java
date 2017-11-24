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
	private volatile boolean connected = true;
	private File downloadFile;

	public void setDownloadFile(File downloadFile) {
		this.downloadFile = downloadFile;
	}

	public void connect(String host, int port) {
		try {
			this.socket = new Socket();
			this.socket.connect(new InetSocketAddress(host, port));
			this.input = new DataInputStream(socket.getInputStream());
			this.output = new DataOutputStream(this.socket.getOutputStream());
			new Thread(this).start();
		} catch (Exception e) {
			throw new ConnectionException("Error in connecting the client socket");
		}
	}

	public void disconnect() {
		this.sendMessage(Constants.QUIT);
		try {
			this.socket.close();
		} catch (Exception e) {
			throw new ConnectionException("Error in closing the client socket");
		}
		this.socket = null;
		this.connected = false;
	}

	public void sendMessage(String message) {
		try {
			this.output.write(0);
			this.output.writeUTF(message);
			this.output.flush();
		} catch (IOException e) {
			throw new MessageException("Error while sending message to server.");
		}
	}

	public void sendFile(File file) {
		try (FileInputStream fis = new FileInputStream(file);) {
			this.output.write(1);
			this.output.writeLong(file.length());
			this.output.writeUTF(file.getName());
			byte[] buffer = new byte[Constants.BUFFER_SIZE];
			while (fis.read(buffer) > 0) {
				this.output.write(buffer);
			}
			this.output.flush();
		} catch (Exception e) {
			e.printStackTrace();
			throw new MessageException("Error while sending file to server.");
		}
	}

	@Override
	public void run() {
		try {
			for (;;) {
				long size = this.input.readLong();
				String name = this.input.readUTF();
				// Check if fill null
				FileOutputStream fos = new FileOutputStream(this.downloadFile.getAbsolutePath());
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
			if (this.connected) {
				e.printStackTrace();
				throw new MessageException("Error while receiving message from the server.");
			}
		}
	}
}
