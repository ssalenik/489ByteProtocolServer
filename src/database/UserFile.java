package database;

import networking.protocol.IncomingPacketHandler;

public class UserFile {

	public int id;
	public String username;
	public String filename;
	public String dbFilename;
	public long filesize;
	public String timeUploaded;
	
	public UserFile() {
		id = -1;
		username = "";
		filename = "";
		dbFilename= "";
		filesize = -1;
		timeUploaded = "";
	}
	
	public UserFile(int id, String u, String fn, long fs, String dbfn, String t) {
		this.id = id;
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
		sb.append(id);
		sb.append(IncomingPacketHandler.FIELD_TERMINATOR);
		sb.append(timeUploaded);
		sb.append(IncomingPacketHandler.FIELD_TERMINATOR);
		sb.append(filesize);
		sb.append(IncomingPacketHandler.FIELD_TERMINATOR);
		sb.append(filename);
		return sb.toString();
	}
	
}
