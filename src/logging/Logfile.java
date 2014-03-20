package logging;

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Logfile {

	public static String logfile = "logfile.txt";
	public static boolean log = false;
	public static int DEBUG_LEVEL = LogLevel.getLogLevel(-1).getValue();
	public static final int DEFAULT_LOGGING_LEVEL_UNSPECIFIED = LogLevel.CRITICAL.getValue();
	
	private static File file;
	private static BufferedWriter writer;
	
	public static void openLog() {
		file = new File(logfile);
		try {
			// append to existing logfile (if exists)
			writer = new BufferedWriter(new FileWriter(file, true));
		} catch (IOException e) {
			System.out.println("Cannot print to file!");
			e.printStackTrace();
		}
	}
	
	public static synchronized void writeToFile(String string) {
		writeToFile(string,DEFAULT_LOGGING_LEVEL_UNSPECIFIED);
	}
	
	public static synchronized void writeToFile(String string, LogLevel level) {
		writeToFile(string,level.getValue());
	}
	
	public static synchronized void writeToFile(String string, int debugLevel) {
		if (debugLevel <= DEBUG_LEVEL) {
			if (log) {
				openLog();
				Date todaysDate = new Date();
				SimpleDateFormat formattedDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				try {
					writer.write(LogLevel.getLogLevel(debugLevel).toString() + " : " + formattedDate.format(todaysDate) + " : " + string + "\n");
				} catch (IOException e) {
					System.out.println("Cannot write to file");
					e.printStackTrace();
				}
				closeLog();
			} else
				if (debugLevel <= LogLevel.ERROR.getValue())
					System.err.println(string);
				else
					System.out.println(string);
		}
	}
	
	public static void closeLog() {
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
