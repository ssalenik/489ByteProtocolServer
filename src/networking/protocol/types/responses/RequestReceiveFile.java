package networking.protocol.types.responses;

public enum RequestReceiveFile {
	RECEIVE_APPROVED(0),
	USER_DOESNT_EXIST(1),
	FILE_DOESNT_EXIST(2),
	ANOTHER_RECEIVE_IN_PROGRESS(3),
	BADLY_FORMATTED(4);
	
	private int i;
	private RequestReceiveFile(int i) {
		this.i = i;
	}
	
	public int getInt() { return i; }
	
	public static RequestReceiveFile ofInt(int i) {
		for(RequestReceiveFile rrf : RequestReceiveFile.values())
			if (rrf.i == i)
				return rrf;
		return BADLY_FORMATTED;
	}
}
