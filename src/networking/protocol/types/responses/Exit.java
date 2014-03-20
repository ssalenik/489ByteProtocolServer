package networking.protocol.types.responses;

public enum Exit {
	EXIT_OK(0);
	
	private int i;
	private Exit(int i) {
		this.i = i;
	}
	
	public int getInt() {return i;}
}
