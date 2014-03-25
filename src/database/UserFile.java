package database;

import networking.protocol.IncomingPacketHandler;

public class UserFile {

	public String username;
	public String filename;
	public String dbFilename;
	public int filesize;
	public String timeUploaded;
	
	public UserFile() {
		username = "";
		filename = "";
		dbFilename= "";
		filesize = -1;
		timeUploaded = "";
	}
	
	public UserFile(String u, String fn, int fs, String dbfn, String t) {
		username = u;
		filename = fn;
		dbFilename = dbfn;
		filesize = fs;
		timeUploaded = t;
	}
	
	public String format() {
		StringBuilder sb = new StringBuilder();
		sb.append(username);
		sb.append(IncomingPacketHandler.FIELD_TERMINATOR);
		sb.append(timeUploaded);
		sb.append(IncomingPacketHandler.FIELD_TERMINATOR);
		sb.append(filesize);
		sb.append(IncomingPacketHandler.FIELD_TERMINATOR);
		sb.append(filename);
		return sb.toString();
	}
	
}
