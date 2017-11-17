package client.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import common.message.Message;
import common.message.MessageType;

public class ServerConnection implements Runnable {

	private InetSocketAddress serverAddress;
	private boolean connected;
	private SocketChannel socketChannel;
	private Selector selector;
	private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
	private List<CommunicationListener> listeners = new ArrayList<>();
	private final ByteBuffer messageFromServer = ByteBuffer.allocateDirect(MessageType.MESSAGELENGTH);
	private volatile boolean timeToSend = false;

	@Override
	public void run() {
		try {
			this.initConnection();
			this.initSelector();
			this.listening();
		} catch (Exception e) {
			System.err.println("Error in client connection to the server");
		}
	}

	private void listening() throws IOException {
		while (this.connected || !messagesToSend.isEmpty()) {
			if (this.timeToSend) {
				this.socketChannel.keyFor(this.selector).interestOps(SelectionKey.OP_WRITE);
				this.timeToSend = false;
			}
			if (this.selector.select() > 0) {
				this.processing();
			}
		}
	}

	private void processing() {
		try {
			for (SelectionKey key : this.selector.selectedKeys()) {
				this.selector.selectedKeys().remove(key);
				if (!key.isValid()) {
					continue;
				}
				if (key.isConnectable()) {
					this.completeConnection(key);
				} else if (key.isReadable()) {
					this.receivedFromServer(key);
				} else if (key.isWritable()) {
					this.sendToServer(key);
				}
			}
		} catch (Exception e) {
			System.err.println("Error in processing client input");
		}
	}

	private void completeConnection(SelectionKey key) throws IOException {
		this.socketChannel.finishConnect();
		key.interestOps(SelectionKey.OP_READ);
	}

	private void receivedFromServer(SelectionKey key) throws IOException {
		this.messageFromServer.clear();
		int numOfReadBytes = this.socketChannel.read(this.messageFromServer);
		if (numOfReadBytes == -1) {
			throw new IOException("Error while reading client input");
		}
		String receivedString = extractMessageFromBuffer();
		System.out.println("message received by the client : " + receivedString);
		String[] messages = receivedString.split(MessageType.ENDMESSAGE);
		for (String singleMessage : messages) {
			for (CommunicationListener listener : this.listeners) {
				listener.receivedMessage(singleMessage);
			}
		}
	}

	private String extractMessageFromBuffer() {
		this.messageFromServer.flip();
		byte[] bytes = new byte[this.messageFromServer.remaining()];
		this.messageFromServer.get(bytes);
		return new String(bytes);
	}

	private void sendToServer(SelectionKey key) throws IOException {
		ByteBuffer message;
		synchronized (this.messagesToSend) {
			while ((message = this.messagesToSend.peek()) != null) {
				System.out.println("message sent by the client : " + new String(message.array()));
				this.socketChannel.write(message);
				if (message.hasRemaining()) {
					return;
				}
				this.messagesToSend.remove();
			}
			key.interestOps(SelectionKey.OP_READ);
		}
	}

	private void initSelector() throws IOException {
		this.selector = Selector.open();
		this.socketChannel.register(selector, SelectionKey.OP_CONNECT);
	}

	private void initConnection() throws Exception {
		this.socketChannel = SocketChannel.open();
		this.socketChannel.configureBlocking(false);
		this.socketChannel.connect(this.serverAddress);
		this.connected = true;
	}

	public void connect(String host, int port, CommunicationListener listener) {
		this.serverAddress = new InetSocketAddress(host, port);
		this.listeners.add(listener);
		new Thread(this).start();
	}

	public void startGame() {
		this.sendMessage(Message.prepareMessage(MessageType.START));
	}

	public void disconnect() throws IOException {
		this.connected = false;
		this.sendMessage(Message.prepareMessage(MessageType.QUIT));
	}

	public void sendMessage(String message) {
		synchronized (this.messagesToSend) {
			this.messagesToSend.add(ByteBuffer.wrap(message.getBytes()));
		}
		this.timeToSend = true;
		this.selector.wakeup();
	}

}
