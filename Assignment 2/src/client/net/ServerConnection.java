package client.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;

import common.message.Message;
import common.message.MessageType;

public class ServerConnection implements Runnable {

	private InetSocketAddress serverAddress;
	private boolean connected;
	private SocketChannel socketChannel;
	private Selector selector;
	private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
	private CommunicationListener listener;
	private final ByteBuffer messageFromServer = ByteBuffer.allocateDirect(MessageType.MESSAGELENGTH);
	private volatile boolean timeToSend = false;

	@Override
	public void run() {
		try {
			this.initConnection();
			this.initSelector();
			this.listening();
		} catch (Exception e) {
			System.err.println("Error in client connection to the server.");
		}
	}

	private void listening() throws IOException {
		while (this.connected || !messagesToSend.isEmpty()) {
			if (this.timeToSend) {
				// If I have data to send, I prepare the channel to write
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
					this.finishConnection(key);
				} else if (key.isReadable()) {
					this.receiveMessages(key);
				} else if (key.isWritable()) {
					this.sendMessages(key);
				}
			}
		} catch (Exception e) {
			System.err.println("Error in processing client input.");
		}
	}

	private void finishConnection(SelectionKey key) throws IOException {
		this.socketChannel.finishConnect();
		key.interestOps(SelectionKey.OP_READ);
	}

	private void receiveMessages(SelectionKey key) throws IOException {
		this.messageFromServer.clear();
		int numOfReadBytes = this.socketChannel.read(this.messageFromServer);
		if (numOfReadBytes == -1) {
			throw new IOException("Error while reading client input.");
		}
		String receivedString = extractMessageFromBuffer();
		// There can be several messages received in the buffer, so we have to
		// split them before taking care of each message
		String[] messages = receivedString.split(MessageType.ENDMESSAGE);
		// All the messages received are put in a queue and a pool is launched to manage all the messages
		for (String singleMessage : messages) {
			this.listener.receiveMessage(singleMessage);
		}
		ForkJoinPool.commonPool().execute(this.listener);
	}

	private String extractMessageFromBuffer() {
		this.messageFromServer.flip();
		byte[] bytes = new byte[this.messageFromServer.remaining()];
		this.messageFromServer.get(bytes);
		return new String(bytes);
	}

	private void sendMessages(SelectionKey key) throws IOException {
		ByteBuffer message;
		synchronized (this.messagesToSend) {
			while ((message = this.messagesToSend.peek()) != null) {
				this.socketChannel.write(message);
				if (message.hasRemaining()) {
					return;
				}
				this.messagesToSend.remove();
			}
			// Once I sent all the messages, I listen again for new messages
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
		this.listener = listener;
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
