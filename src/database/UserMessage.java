package database;

import networking.protocol.IncomingPacketHandler;

public class UserMessage {

	public String username;
	public String message;
	public String time;
	
	public UserMessage() {
		username = "";
		message = "";
		time = "";
	}
	
	public UserMessage(String u, String m, String t) {
		username = u;
		message = m;
		time = t;
	}
	
	public String format() {
		StringBuilder sb = new StringBuilder();
		sb.append(username);
		sb.append(IncomingPacketHandler.FIELD_TERMINATOR);
		sb.append(time);
		sb.append(IncomingPacketHandler.FIELD_TERMINATOR);
		sb.append(message);
		return sb.toString();
	}
	
}
