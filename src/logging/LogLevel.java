package logging;

public enum LogLevel {
	CRITICAL(0, "Critical"),
	ERROR(1,"Error"),
	WARNING(2,"Warning"),
	INFO(3,"Info"),
	DEBUG(4,"Debug");
	
	private int i;
	private String s;
	
	private LogLevel(int i, String s) {
		this.i = i;
		this.s = s;
	}
	
	public int getValue() {
		return this.i;
	}
	
	public String toString() {
		return this.s;
	}
	
	public static LogLevel getLogLevel(int i ){ 
		for (LogLevel l : LogLevel.values()) {
			if (l.getValue() == i) {
				return l;
			}
		}
		return WARNING; // default log-level
	}
}
