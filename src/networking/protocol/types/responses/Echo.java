package networking.protocol.types.responses;

public enum Echo {
	ECHO_OK(0);
	
	private int i;
	private Echo(int i) {
		this.i = i;
	}
	
	public int getInt() {return i;}
}
