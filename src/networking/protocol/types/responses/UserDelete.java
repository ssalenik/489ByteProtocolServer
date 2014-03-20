package networking.protocol.types.responses;

public enum UserDelete {
	DELETE_SUCCESS(0),
	NOT_LOGGED_IN(1),
	ERROR(2);
	
	private int i;
	private UserDelete(int i) {
		this.i = i;
	}
	
	public int getInt() {return i;}
	
	public static UserDelete ofInt(int i) {
		for (UserDelete ud : UserDelete.values())
			if (ud.i == i)
				return ud;
		return ERROR;
	}
}
