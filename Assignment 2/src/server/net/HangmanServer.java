package server.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class HangmanServer {

	private static final int PORT = 8080;
	private Selector selector;
	private ServerSocketChannel listeningSocketChannel;

	public static void main(String[] args) {
		HangmanServer hangmanServer = new HangmanServer();
		hangmanServer.serve();
	}

	private void serve() {
		try {
			initSelector();
			initListeningSocketChannel();
			while (true) {
				selector.select();
				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();
					if (!key.isValid()) {
						continue;
					}
					if (key.isAcceptable()) {
						startHandler(key);
					} else if (key.isReadable()) {
						recvFromClient(key);
					} else if (key.isWritable()) {
						// Might be managed
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Server failure.");
		}
	}

	private void initSelector() throws IOException {
		selector = Selector.open();
	}

	private void initListeningSocketChannel() throws IOException {
		listeningSocketChannel = ServerSocketChannel.open();
		listeningSocketChannel.configureBlocking(false);
		listeningSocketChannel.bind(new InetSocketAddress(PORT));
		listeningSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	}

	private void startHandler(SelectionKey key) throws IOException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		SocketChannel clientChannel = serverSocketChannel.accept();
		clientChannel.configureBlocking(false);
		ClientHandler handler = new ClientHandler(clientChannel);
		clientChannel.register(selector, SelectionKey.OP_WRITE, handler);
	}

	private void recvFromClient(SelectionKey key) throws IOException {
		ClientHandler clientHandler = (ClientHandler) key.attachment();
		try {
			clientHandler.receiveMessage();
		} catch (IOException clientHasClosedConnection) {
			removeClient(key);
		}
	}

	private void removeClient(SelectionKey clientKey) throws IOException {
		ClientHandler clientHandler = (ClientHandler) clientKey.attachment();
		clientHandler.disconnectClient();
		clientKey.cancel();
	}

}