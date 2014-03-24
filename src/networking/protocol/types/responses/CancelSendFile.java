package networking.protocol.types.responses;

public enum CancelSendFile {
	SEND_CANCELLED(0),
	SEND_NOT_IN_PROGRESS(1),
	NOT_LOGGED_IN(2);
	
	private int i;
	private CancelSendFile(int i) {
		this.i = i;
	}
	
	public int getInt() { return i; }
	
	public static CancelSendFile ofInt(int i) {
		for(CancelSendFile csf : CancelSendFile.values())
			if (csf.i == i)
				return csf;
		return SEND_NOT_IN_PROGRESS;
	}
}
