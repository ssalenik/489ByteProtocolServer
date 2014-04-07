package networking.protocol.types.responses;

public enum SendFileChunk {
	RECEIVED_CHUNK(0),
	SEND_NOT_APPROVED(1),
	NOT_LOGGED_IN(2),
	CHUNK_EXCEEDS_EXPECTED_SIZE(3),
	IO_ERROR(4);
	
	private int i;
	private SendFileChunk(int i) {
		this.i = i;
	}
	
	public int getInt() { return i; }
	
	public static SendFileChunk ofInt(int i) {
		for(SendFileChunk fc : SendFileChunk.values())
			if (fc.i == i)
				return fc;
		return SEND_NOT_APPROVED;
	}
}
