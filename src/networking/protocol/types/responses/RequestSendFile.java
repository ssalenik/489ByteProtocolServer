package networking.protocol.types.responses;

public enum RequestSendFile {
	SEND_APPROVED(0),
	USER_STORE_DOESNT_EXIST(1),
	USER_DOESNT_EXIST(2),
	MAX_FILE_SIZE_EXCEEDED(3),
	ANOTHER_SEND_IN_PROGRESS(4),
	BADLY_FORMATTED(5),
	NOT_LOGGED_IN(6);
	
	private int i;
	private RequestSendFile(int i) {
		this.i = i;
	}
	
	public int getInt() { return i; }
	
	public static RequestSendFile ofInt(int i) {
		for(RequestSendFile rsf : RequestSendFile.values())
			if (rsf.i == i)
				return rsf;
		return BADLY_FORMATTED;
	}
}
