package networking.protocol.types.responses;

public enum CancelReceiveFile {
	RECEIVE_CANCELLED(0),
	RECEIVE_NOT_IN_PROGRESS(1),
	NOT_LOGGED_IN(2);
	
	private int i;
	private CancelReceiveFile(int i) {
		this.i = i;
	}
	
	public int getInt() { return i; }
	
	public static CancelReceiveFile ofInt(int i) {
		for(CancelReceiveFile crf : CancelReceiveFile.values())
			if (crf.i == i)
				return crf;
		return RECEIVE_NOT_IN_PROGRESS;
	}
}
