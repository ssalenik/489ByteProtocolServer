package networking.protocol.types.responses;

public enum RequestSendFile {
	SEND_APPROVED(0),
	USER_DOESNT_EXIST(1),
	MAX_FILE_SIZE_EXCEEDED(2),
	ANOTHER_SEND_IN_PROGRESS(3),
	BADLY_FORMATTED(4);
	
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
