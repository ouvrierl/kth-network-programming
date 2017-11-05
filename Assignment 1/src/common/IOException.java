package common;

public class IOException extends RuntimeException {

	public IOException(String msg) {
		super(msg);
	}

	public IOException(Throwable rootCause) {
		super(rootCause);
	}
}
