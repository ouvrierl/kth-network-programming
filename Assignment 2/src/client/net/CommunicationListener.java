package client.net;

public interface CommunicationListener extends Runnable {

	public void receiveMessage(String message);

}
