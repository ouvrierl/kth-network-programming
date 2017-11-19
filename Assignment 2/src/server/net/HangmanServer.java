package server.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class HangmanServer {

	private static final String ADDRESS = "localhost";
	private static final int PORT = 8080;

	private Selector selector;
	private ServerSocketChannel listeningSocketChannel;

	public static void main(String[] args) {
		HangmanServer hangmanServer = new HangmanServer();
		hangmanServer.launchServer();
	}

	private void launchServer() {
		try {
			this.initSelector();
			this.initListeningSocketChannel();
			this.listening();
		} catch (Exception e) {
			System.err.println("Error while launching the server.");
		}
	}

	private void processing() {
		try {
			Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
			Iterator<SelectionKey> iterator = selectedKeys.iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				if (!key.isValid()) {
					continue;
				}
				if (key.isAcceptable()) {
					this.launchClient(key);
				} else if (key.isReadable()) {
					this.receiveMessages(key);
				} else if (key.isWritable()) {
					this.sendMessages(key);
				}
			}
		} catch (Exception e) {
			System.err.println("Error in treating server entry.");
		}
	}

	private void sendMessages(SelectionKey key) {
		ClientHandler client = (ClientHandler) key.attachment();
		client.sendAllMessages();
	}

	private void listening() {
		try {
			while (true) {
				if (this.selector.select() <= 0) {
					continue;
				}
				this.processing();
			}
		} catch (Exception e) {
			System.err.println("Error while server listening.");
		}
	}

	private void initSelector() throws IOException {
		this.selector = Selector.open();
	}

	private void initListeningSocketChannel() throws IOException {
		this.listeningSocketChannel = ServerSocketChannel.open();
		this.listeningSocketChannel.configureBlocking(false);
		this.listeningSocketChannel.bind(new InetSocketAddress(ADDRESS, PORT));
		this.listeningSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
	}

	private void launchClient(SelectionKey key) throws IOException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		SocketChannel clientChannel = serverSocketChannel.accept();
		clientChannel.configureBlocking(false);
		ClientHandler handler = new ClientHandler(clientChannel);
		// The server will manage read and write for each client
		clientChannel.register(this.selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, handler);
	}

	private void receiveMessages(SelectionKey key) throws IOException {
		ClientHandler clientHandler = (ClientHandler) key.attachment();
		try {
			clientHandler.receiveMessage();
		} catch (IOException clientHasClosedConnection) {
			this.disconnectClient(key);
		}
	}

	private void disconnectClient(SelectionKey clientKey) throws IOException {
		ClientHandler clientHandler = (ClientHandler) clientKey.attachment();
		clientHandler.disconnectClient();
		clientKey.cancel();
	}

}