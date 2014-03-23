package networking.protocol.types.responses;

public enum ReceiveFileChunk {
	SENDING_CHUNK(0),
	RECEIVE_NOT_APPROVED(1);
	
	private int i;
	private ReceiveFileChunk(int i) {
		this.i = i;
	}
	
	public int getInt() { return i; }
	
	public static ReceiveFileChunk ofInt(int i) {
		for(ReceiveFileChunk fc : ReceiveFileChunk.values())
			if (fc.i == i)
				return fc;
		return RECEIVE_NOT_APPROVED;
	}
}
