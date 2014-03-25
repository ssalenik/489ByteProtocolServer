package database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUploader {
	
	SimpleDateFormat dateFormat;
	File currentFile;
	private String currentDBFilename;
	private boolean uploadInProgress;
	private FileWriter fw;
	private BufferedWriter bw;
	
	public FileUploader() {
		dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
		uploadInProgress = false;
	}
	
	public boolean startFileUpload(String username, String filename, int filesize) {
		
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
		
		// start writer
		try {
			fw = new FileWriter(currentFile.getAbsoluteFile());
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		// assume everything went OK
		currentDBFilename = currentFile.toString();
		uploadInProgress = true;
		return true;
	}
	
//	public uploadFileChunk()
	
	public boolean isUploadInProgress() {
		return uploadInProgress;
	}
	
	public String getCurrentDBFilename() {
		return currentDBFilename;
	}
	
}
