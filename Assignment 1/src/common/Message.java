package common;

import java.util.ArrayList;
import java.util.List;

public class Message {
	private String messageType;
	private List<String> messageBody;

	public Message(String message) {
		this.parse(message);
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
		StringBuilder builder = new StringBuilder();
		builder.append(messageType);
		for (String arg : messageBody) {
			builder.append(" " + arg);
		}
		return builder.toString();
	}
}
