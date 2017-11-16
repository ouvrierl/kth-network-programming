package common.message;

import java.util.ArrayList;
import java.util.List;

public class Message {
	private String messageType;
	private List<String> messageBody;
	private String entireMessage;

	public Message(String message) {
		this.parse(message);
		this.entireMessage = message;
	}

	public String getMessageType() {
		return this.messageType;
	}

	public List<String> getMessageBody() {
		return this.messageBody;
	}

	private void parse(String message) {
		String[] split = message.split(MessageType.DELIMITER);
		this.messageType = split[0];
		this.messageBody = new ArrayList<>();
		for (int i = 1; i < split.length; i++) {
			this.messageBody.add(split[i]);
		}
	}

	@Override
	public String toString() {
		return this.entireMessage;
	}

	public static String prepareMessage(String... args) {
		StringBuilder message = new StringBuilder();
		for (String arg : args) {
			message.append(arg);
			message.append(MessageType.DELIMITER);
		}
		message.setLength(message.length() - 1); // Last useless space is
													// removed
		// message.append(MessageType.ENDMESSAGE);
		return message.toString();
	}
}
