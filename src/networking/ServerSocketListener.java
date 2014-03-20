package networking;

import java.net.*;
import java.io.IOException;

import networking.auth.AuthenticationManager;
import networking.auth.IAuthenticator;
import database.IResource;
import logging.LogLevel;
import logging.Logfile;

public class ServerSocketListener extends Thread implements IAuthenticator {

	private boolean alive;
	private ServerSocket socket;
	private IResource resource;
	
	private AuthenticationManager manager;
	
	public ServerSocketListener(int port, IResource resource) {
		try {
			this.socket = new ServerSocket(port);
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
				Socket client = socket.accept();
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
