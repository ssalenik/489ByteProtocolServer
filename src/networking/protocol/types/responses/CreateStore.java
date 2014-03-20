package networking.protocol.types.responses;

public enum CreateStore {
	STORE_CREATED(0),
	STORE_ALREADY_EXISTS(1),
	NOT_LOGGED_IN(2);
	
	private int i;
	private CreateStore(int i) {
		this.i = i;
	}
	
	public int getInt() {return i;}
	public static CreateStore ofInt(int i) {
		for (CreateStore cs : CreateStore.values())
			if (cs.i == i)
				return cs;
		return STORE_ALREADY_EXISTS;
	}
}
