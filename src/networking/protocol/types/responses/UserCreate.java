package networking.protocol.types.responses;

public enum UserCreate {
	CREATE_SUCCESS(0),
	USER_ALREADY_EXISTS(1),
	ALREADY_LOGGED_IN(2),
	BADLY_FORMATTED(3);
	
	private int i;
	private UserCreate(int i) {
		this.i = i;
	}
	
	public int getInt() { return i; }
	
	public static UserCreate ofInt(int i) {
		for (UserCreate uc : UserCreate.values())
			if (uc.i == i)
				return uc;
		return BADLY_FORMATTED;
	}
}
