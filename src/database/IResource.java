package database;

public interface IResource {
	public boolean login(String username, String password);
	public boolean createUser(String username, String password);
	public boolean deleteUser(String username);
	
	public boolean createUserDataTable(String username);
	public boolean userDataTableExists(String username);
	
	public boolean sendMessageToUser(String dUsername, String sUsername, String message);
	public UserMessage[] getNewUserMessages(String username);
	
	public boolean createUserFilesTable(String username);
	public boolean userFilesTableExists(String username);
	
	public boolean sendFileToUser(String dUsername, String sUsername, String filename, String dbFilename, long filesize);
	public UserFile[] getNewUserFiles(String username, int id);
	public UserFile[] getUserFile(String username, int file_id);
	public boolean deleteUserFile(String username, int file_id);
	
}
