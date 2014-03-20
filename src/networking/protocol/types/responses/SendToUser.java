package networking.protocol.types.responses;

public enum SendToUser {
	MESSAGE_SENT(0),
	FAILED_TO_WRITE_TO_USER_STORE(1),
	USER_DOESNT_EXIST(2),
	NOT_LOGGED_IN(3),
	BADLY_FORMATTED(4);
	
	private int i;
	private SendToUser(int i) {
		this.i = i;
	}
	
	public int getInt() {return i;}
	
	public static SendToUser ofInt(int i) {
		for (SendToUser stu : SendToUser.values())
			if (stu.i == i)
				return stu;
		return BADLY_FORMATTED;
	}
}
