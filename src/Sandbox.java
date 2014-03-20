import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import database.SQLiteJDBC;
import logging.Logfile;
import networking.ServerSocketListener;

public class Sandbox {

	public static ServerSocketListener ssl;
	public static SQLiteJDBC database;
	
	public static final String configfile = "./config/config.properties";
	
	public static boolean running = false;
	
	private static void checkDirectories() {
		File configfolder = new File("./config");
		if (!configfolder.exists()) {
			System.out.println("Creating config directory");
			configfolder.mkdir();
		}
		
		File dbfolder = new File("./db");
		if (!dbfolder.exists()) {
			System.out.println("Creating database directory");
			dbfolder.mkdir();
		}
	}
	
	public static void main(String[] args) throws IOException {
		Properties props = new Properties();
		try {
			checkDirectories();
			
			props.load(new FileInputStream(configfile));
			
			int port = Integer.parseInt(props.getProperty("port","8000"));
			Logfile.DEBUG_LEVEL = Integer.parseInt(props.getProperty("log_level", "5"));
			Logfile.logfile = props.getProperty("logfile", "logfile.txt");
			
			boolean log = Boolean.parseBoolean(props.getProperty("enable_file_logging", "false"));
			if (log)
				Logfile.log = true;
			
			boolean autostart = Boolean.parseBoolean(props.getProperty("autostart", "false"));
			if (autostart)
				start(port);
			
			commandProcessor(port);
			
		} catch (FileNotFoundException e) {
			System.err.println("Could not find the server config file, check the properties folder and make sure the config.properties file exists");
		} catch (IllegalArgumentException e) { // bad string parsing
			System.err.println("Something in the config file is mal-formatted, check the datatypes to make sure they're good");
		}
	}
	
	private static void start(int port) {
		database = new SQLiteJDBC();
		ssl = new ServerSocketListener(port, database);
		ssl.start();
		running = true;
		System.out.println("System started");
	}
	
	private static void stop() {
		if (running) {
			try { ssl.kill(); } catch (Exception e) { }
			try { database.CloseDBConnection(); } catch (Exception e) {}
			System.out.println("Server stopped...");
		} else {
			System.err.println("Server not running");
		}
	}
	
	public static void commandProcessor(int port) {
		Scanner scan = new Scanner(System.in);
		boolean loop = true;
		while (loop) {
			System.out.println("Input command");
			switch (Command.ofString(scan.nextLine())) {
			case EXIT:
				System.out.println("Exit requested");
				stop();
				loop = false;
				System.out.println("Exiting...");
				break;
			case START:
				System.out.println("System start requested");
				if (!running)
					start(port);
				else
					System.err.println("System already started");
				break;
			case STOP:
				System.out.println("System stop requested");
				stop(); // keep looping
				break;
			case LOG:
				Logfile.log = !Logfile.log;
				System.out.println("Toggling log option, Log option is now: " + (Logfile.log ? "on" : "off"));
				break;
			case ERROR:
				System.out.println("Bad command input, must be either start, stop, log, or exit");
				break;
			}
		}
		scan.close();
		System.exit(0);
	}

}
