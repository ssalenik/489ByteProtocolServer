package database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUploader {
	
	SimpleDateFormat dateFormat;
	File currentFile;
	int currentFileSize;
	private int bytesWritten;
	private String currentDBFilename;
	private boolean uploadInProgress;
	private boolean uploadComplete;
	private FileOutputStream fileOutput;
	
	public FileUploader() {
		dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
		uploadInProgress = false;
		uploadComplete = false;
		bytesWritten = -1;
	}
	
	public boolean startFileUpload(String username, String filename, int filesize) {
		if(uploadInProgress) {
			return false;
		}
		// TODO: check filename validity?
		// get dbFilename
		String uploadStarted = dateFormat.format(new Date());
		String dbFilename = "" + username + "_file_" + uploadStarted;
		
		// check if we can write this file
		currentFile = new File("./" + dbFilename);
		long usableSpace = currentFile.getUsableSpace();
		if(usableSpace < filesize) {
			// not enough space
			// TODO: log error
			return false;
		}
		
		// TODO: make sure file doesn't exist already, add an integer if it does
		try {
			if(!currentFile.createNewFile()) {
				// TODO: log error
				return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		// create file output stream, do not append, file should be empty
		try {
			fileOutput = new FileOutputStream(currentFile, false);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		// assume everything went OK
		currentDBFilename = currentFile.toString();
		uploadInProgress = true;
		currentFileSize = filesize;
		bytesWritten = 0;
		return true;
	}
	
	public void uploadFileChunk(byte[] b) throws IOException {
		if(bytesWritten + b.length > currentFileSize) {
			throw new IllegalArgumentException("attempted write exceeds expected file size");
		}
		if (uploadInProgress) {
			fileOutput.write(b);
			bytesWritten += b.length;
			
			// check if we're done
			if(bytesWritten == currentFileSize) {
				uploadInProgress = false;
				uploadComplete = true;
			}
		} else {
			// upload not started
			return;
		}
	}
	public boolean isUploadComplete() {
		return uploadComplete;
	}
	
	public boolean isUploadInProgress() {
		return uploadInProgress;
	}
	
	public String getCurrentDBFilename() {
		return currentDBFilename;
	}
	
	public int getBytesWritten() {
		return bytesWritten;
	}
	
}
