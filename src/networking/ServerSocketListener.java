package networking;

import java.net.*;
import java.io.IOException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import networking.auth.AuthenticationManager;
import networking.auth.IAuthenticator;
import database.IResource;
import logging.LogLevel;
import logging.Logfile;

public class ServerSocketListener extends Thread implements IAuthenticator {

	private boolean alive;
	private SSLServerSocket socket;
	private IResource resource;
	
	private AuthenticationManager manager;
	
	public ServerSocketListener(int port, IResource resource) {
		String keyStorePath = "./ssl/keystore.jks";
		String keyStorePassword = "javachat";
		
		try {
//			System.out.println("Working Directory = " +  System.getProperty("user.dir"));
			System.setProperty("javax.net.ssl.keyStore", keyStorePath);
			System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
			
			
			SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			this.socket = (SSLServerSocket) ssf.createServerSocket(port);
//			this.socket = new ServerSocket(port);
			this.resource = resource; 
			alive = true;
			
			this.manager = new AuthenticationManager(this);
			
			Logfile.writeToFile("Listening for incoming connections on port: " + port, LogLevel.INFO);
		} catch (IOException e) {
			Logfile.writeToFile("Failed to open server socket due to error: " + e.getMessage(), LogLevel.CRITICAL);
			alive = false;
		}
	}
	
	public void run() {
		while(alive) {
			try {
				Logfile.writeToFile("Waiting for a new client connection", LogLevel.DEBUG);
				SSLSocket client = (SSLSocket) socket.accept();
				ClientProcessor cp = new ClientProcessor(client, resource, manager);
				cp.start();
				Logfile.writeToFile("Accepted connection from: " + client.getInetAddress().getHostAddress(), LogLevel.DEBUG);
			} catch (IOException e) {
				Logfile.writeToFile("Failed to create new client connection or socket closed", LogLevel.ERROR);
			}
		}
	}
	
	
	public void kill() {
		try {
			this.socket.close();
		} catch (IOException e) {
			Logfile.writeToFile("Failed to close server socket", LogLevel.CRITICAL);
		}
		this.alive = false;
	}

	@Override
	public boolean Authenticate(String username, String password) {
		return resource.login(username, password);
	}
}
