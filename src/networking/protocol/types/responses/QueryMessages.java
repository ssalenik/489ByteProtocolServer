package networking.protocol.types.responses;

public enum QueryMessages {
	NO_MESSAGES(0),
	MESSAGES(1),
	NOT_LOGGED_IN(2);
	
	private int i;
	private QueryMessages(int i) {
		this.i = i;
	}
	
	public int getInt() {return i;}
	public static QueryMessages ofInt(int i) {
		for (QueryMessages qm : QueryMessages.values())
			if (qm.i == i)
				return qm;
		return NO_MESSAGES;
	}
}
