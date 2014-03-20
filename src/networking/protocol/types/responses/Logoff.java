package networking.protocol.types.responses;

public enum Logoff {
	LOGOFF_OK(0),
	SESSION_EXPIRED(2),
	NOT_LOGGED_IN(1);
	
	private int i;
	private Logoff(int i) {
		this.i = i;
	}
	
	public int getInt() { return i; }
	
	public static Logoff ofInt(int i) {
		for(Logoff l : Logoff.values())
			if (l.i == i)
				return l;
		return NOT_LOGGED_IN;
	}
	
}
