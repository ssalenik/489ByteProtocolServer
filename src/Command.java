
public enum Command {
	START("start"),
	STOP("stop"),
	EXIT("exit"),
	LOG("log"),
	ERROR("garbage");
	
	private String s;
	
	private Command(String s) {
		this.s = s;
	}
	
	public static Command ofString(String s) {
		for (Command c : Command.values()) 
			if (c.s.toLowerCase().equals(s.toLowerCase()))
				return c;
		return ERROR;
	}
}
