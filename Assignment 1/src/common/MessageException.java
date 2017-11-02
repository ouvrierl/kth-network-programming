package common;

public class MessageException extends RuntimeException {
	
	public MessageException(String msg) {
        super(msg);
    }
    
    public MessageException(Throwable rootCause) {
        super(rootCause);
    }
}
