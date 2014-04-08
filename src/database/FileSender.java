package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileSender {
	private File currentFile;
	private UserFile currentUserFile;
	private long bytesSent;
	private long currentFileSize;
	private boolean sendInProgress;
	private boolean sendComplete;
	private FileInputStream fileStream;
	
	public static final int MAX_CHUNK_SIZE = 10000; //10000; //262144;
	
	public FileSender() {
		sendInProgress = false;
		sendComplete = false;
		bytesSent = -1;
	}
	
	public boolean startFileSend(UserFile file) {
		if(sendInProgress) {
			return false;
		}
		
		this.currentUserFile = file;
		
		this.currentFile = new File(currentUserFile.dbFilename);
		
		try {
			this.fileStream = new FileInputStream(currentFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		this.bytesSent = 0;
		this.currentFileSize = currentUserFile.filesize;
		this.sendInProgress = true;
		this.sendComplete = false;
		
		return true;
	}
	
	/**
	 * Gets the next chunk of bytes from the file;
	 * 
	 * @return
	 */
	public byte[] getNextChunk() {
		if(sendInProgress) {
			byte[] bytesToSend;
			if(currentFileSize - bytesSent >= MAX_CHUNK_SIZE) {
				bytesToSend = new byte[MAX_CHUNK_SIZE];
			} else {
				bytesToSend = new byte[(int) (currentFileSize - bytesSent)];
			}
			
			//TODO: check how many bytes you get from the read?
			try {
				fileStream.read(bytesToSend);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return new byte[0];
			}
			
			bytesSent += bytesToSend.length;
			// check if done
			if(bytesSent >= currentFileSize) {
				sendInProgress = false;
				sendComplete = true;
			}
			
			return bytesToSend;
		} else {
			return new byte[0];
		}
	}
	
	public boolean isSendComplete() {
		return sendComplete;
	}
	
	public boolean isSendInProgress() {
		return sendInProgress;
	}
	
	public long getBytesSent() {
		return bytesSent;
	}
	
	public int getFileID() {
		return this.currentUserFile.id;
	}
	
	public File getCurrentFile() {
		return this.currentFile;
	}
	
	public void cancelSend() {
		sendInProgress = false;
		sendComplete = false;
		try {
			fileStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
