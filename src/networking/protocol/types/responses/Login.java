package networking.protocol.types.responses;

public enum Login {
	LOGIN_OK(0),
	ALREADY_LOGGED_IN(1),
	BAD_CREDENTIALS(2),
	BADLY_FORMATTED(3);
	
	private int i;
	private Login(int i) {
		this.i = i;
	}
	
	public int getInt() { return i; }
	
	public static Login ofInt(int i) {
		for(Login l : Login.values())
			if (l.i == i)
				return l;
		return BADLY_FORMATTED;
	}
}
