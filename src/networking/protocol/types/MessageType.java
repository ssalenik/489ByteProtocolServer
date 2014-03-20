package networking.protocol.types;

public enum MessageType {
	EXIT(0),
	BADLY_FORMATTED_MESSAGE(1),
	ECHO(2), // <msg>
	LOGIN(3), // <username>,<password>
	LOGOFF(4),
	CREATE_USER(5), // <username>,<password>
	DELETE_USER(6),
	CREATE_STORE(7),
	SEND_TO_USER(8), // <username>,message
	QUERY_MESSAGES(9);
	
	private int i;
	
	private MessageType(int i) {
		this.i = i;
	}
	
	public int getInt() { return i; }
	
	public static MessageType ofInt(int i) {
		for (MessageType ty : MessageType.values())
			if (ty.i == i)
				return ty;
		return BADLY_FORMATTED_MESSAGE;
	}
}
