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
		hangmanServer.serve();
	}

	private void serve() {
		try {
			this.initSelector();
			this.initListeningSocketChannel();
			this.listening();
		} catch (Exception e) {
			System.err.println("Server failure.");
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
					this.startHandler(key);
				} else if (key.isReadable()) {
					this.receivedFromClient(key);
				} else if (key.isWritable()) {
					this.sendToClient(key);
				}
			}
		} catch (Exception e) {
			System.err.println("Error in treating server entry");
		}
	}

	private void sendToClient(SelectionKey key) {
		ClientHandler client = (ClientHandler) key.attachment();
		client.sendAll();
		key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE); // Once I
																		// sent
																		// data,
																		// I can
																		// listen
																		// for
																		// data
																		// and
																		// write
																		// data
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
			System.err.println("Server listening failure.");
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

	private void startHandler(SelectionKey key) throws IOException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		SocketChannel clientChannel = serverSocketChannel.accept();
		clientChannel.configureBlocking(false);
		ClientHandler handler = new ClientHandler(clientChannel);
		// I can listen for data or write data
		clientChannel.register(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, handler);
	}

	private void receivedFromClient(SelectionKey key) throws IOException {
		ClientHandler clientHandler = (ClientHandler) key.attachment();
		try {
			clientHandler.receiveMessage();
		} catch (IOException clientHasClosedConnection) {
			this.removeClient(key);
		}
	}

	private void removeClient(SelectionKey clientKey) throws IOException {
		ClientHandler clientHandler = (ClientHandler) clientKey.attachment();
		clientHandler.disconnectClient();
		clientKey.cancel();
	}

}