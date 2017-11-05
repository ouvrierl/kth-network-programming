package common;

public class ConnectionException extends RuntimeException {

	public ConnectionException(String msg) {
		super(msg);
	}

	public ConnectionException(Throwable rootCause) {
		super(rootCause);
	}
}
