package database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

import logging.LogLevel;
import logging.Logfile;

public class FileUploader {
	
	SimpleDateFormat dateFormat;
	File currentFile;
	long currentFileSize;
	private int bytesWritten;
	private String currentDBFilename;
	private String currentFilename;
	private boolean uploadInProgress;
	private boolean uploadComplete;
	private FileOutputStream fileOutput;
	private String currentDestUsername;
	
	public FileUploader() {
		dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
		uploadInProgress = false;
		uploadComplete = false;
		bytesWritten = -1;
	}
	
	public boolean startFileUpload(String username, String filename, long filesize) {
		if(uploadInProgress) {
			Logfile.writeToFile("Cannot start new upload, alreayd in progress.", LogLevel.INFO);
			return false;
		}
		// TODO: check filename validity?
		// get dbFilename
		String uploadStarted = dateFormat.format(new Date());
		String dbFilename = "" + username + "_file_" + uploadStarted;
		
		// save dest user
		currentDestUsername = username;
		// save original filename
		currentFilename = filename;
		
		// check if we can write this file
		currentFile = new File("./" + dbFilename);
		// for some reason get space returns 0
//		long usableSpace = currentFile.getUsableSpace();
//		if(usableSpace < filesize) {
//			// not enough space
//			// TODO: log error
//			Logfile.writeToFile("Cannot start new upload, not enough space.", LogLevel.INFO);
//			return false;
//		}
		
		// TODO: make sure file doesn't exist already, add an integer if it does
		try {
			if(!currentFile.createNewFile()) {
				// TODO: log error
				Logfile.writeToFile("Cannot start new upload, could not create file on disk.", LogLevel.INFO);
				return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logfile.writeToFile("Cannot start new upload, error creating file on disk.", LogLevel.INFO);
			return false;
		}
		
		// create file output stream, do not append, file should be empty
		try {
			fileOutput = new FileOutputStream(currentFile, false);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logfile.writeToFile("Cannot start new upload, error creating file output stream.", LogLevel.INFO);
			
			// delete file
			try {
				Files.deleteIfExists(currentFile.toPath());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return false;
		}
		
		// assume everything went OK
		try {
			// get path
			currentDBFilename = currentFile.getCanonicalPath().toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// delete current file
			try {
				Files.deleteIfExists(currentFile.toPath());
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				fileOutput.close();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			return false;
		}
		uploadInProgress = true;
		uploadComplete = false;
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
				fileOutput.close();
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
	
	public String getCurrentFilename() {
		return currentFilename;
	}
	
	public String getCurrentDestUsername() {
		return currentDestUsername;
	}
	
	public int getBytesWritten() {
		return bytesWritten;
	}
	
	public long getCurrentFileSize() {
		return currentFileSize;
	}
	
	public double getUploadPercent() {
		return (double)bytesWritten/(double)currentFileSize * 100.0;
	}
	
	public void cancelUpload() {
		// only cancel if in progress
		if(uploadInProgress) {
			uploadInProgress = false;
			uploadComplete = false;
			
			// delete current file
			try {
				Files.deleteIfExists(currentFile.toPath());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				fileOutput.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// nothing
			return;
		}
		
	}
	
}
