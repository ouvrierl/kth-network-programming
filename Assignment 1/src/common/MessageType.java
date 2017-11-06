package common;

public class MessageType {

	/**
	 * From client to server
	 */
	public static final String START = "START";
	public static final String QUIT = "QUIT";
	public static final String LETTER = "LETTER";
	public static final String WORD = "WORD";

	/**
	 * From server to client
	 */
	public static final String WELCOME = "WELCOME";
	public static final String ATTEMPT = "ATTEMPT";
	public static final String VICTORY = "VICTORY";
	public static final String DEFEAT = "DEFEAT";
	public static final String FIND = "FIND";
	public static final String ERRORLETTER = "ERRORLETTER";

	public static final String DELIMITER = " ";

}
