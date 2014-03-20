package networking;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import database.IResource;
import logging.LogLevel;
import logging.Logfile;
import networking.auth.AuthenticationManager;
import networking.protocol.*;
import networking.protocol.types.MessageType;

public class ClientProcessor extends Thread implements IAsyncClientWriter {

	private boolean alive;
	private Socket s;

	private InputStream rawIn;
	private OutputStream rawOut;

	private IncomingPacketHandler processor;
	private static final int MAXIMUM_PACKET_SIZE = 262144; // bytes = 256 KB

	public ClientProcessor(Socket s, IResource resource,
			AuthenticationManager manager) {
		try {
			this.s = s;
			this.alive = true;
			this.rawIn = s.getInputStream();
			this.rawOut = s.getOutputStream();

			processor = new IncomingPacketHandler(manager, resource, this);
		} catch (IOException e) {
			Logfile.writeToFile(
					"Failed to create the client stream readers and writers",
					LogLevel.ERROR);
			this.alive = false;
		}
	}

	public UnformattedPacket readPacket() {
		try {
			while (rawIn.available() <= 12) {
				Thread.sleep(50);
			}
			byte[] hArray = new byte[4];
			byte[] h2Array = new byte[4];
			byte[] sArray = new byte[4];

			rawIn.read(hArray, 0, 4);
			rawIn.read(h2Array, 0, 4);
			rawIn.read(sArray, 0, 4);

			int s = ByteBuffer.wrap(sArray).getInt();

			if (s >= MAXIMUM_PACKET_SIZE || s < 0) {
				// dump the remaining data on the socket (it's assumed garbage)
				int avail = 0;
				while ((avail = rawIn.available()) > 0) {
					rawIn.read(new byte[avail], 0, avail);
				}
				Logfile.writeToFile("Bad packet size of " + s + " received from " + getHost().getHostAddress(), LogLevel.ERROR);
				
				return new UnformattedPacket(
						0,
						0,
						("Bad packet size in the header. Size must be between 0 and "
								+ MAXIMUM_PACKET_SIZE + ", inclusive")
								.getBytes());
			}

			byte[] data = new byte[s];
			rawIn.read(data, 0, s);

			return new UnformattedPacket(hArray, h2Array, sArray, data);
		} catch (InterruptedException ex) {
			Logfile.writeToFile("Thread interrupted on socket polling loop",
					LogLevel.ERROR);
			this.closeConnection();
			return null;
		} catch (IOException e) {
			Logfile.writeToFile("Failed to read from socket "
					+ getHost().getHostAddress(), LogLevel.ERROR);
			this.closeConnection();
			return null;
		}
	}

	public void writePacket(UnformattedPacket pkt) {
		try {
			rawOut.write(pkt.serialize());
			rawOut.flush();
		} catch (IOException e) {
			Logfile.writeToFile("Could not send packet to client "
					+ getHost().getHostAddress(), LogLevel.INFO);
			this.closeConnection();
		}
	}

	public void run() {
		while (alive) {
			// packet processing
			UnformattedPacket p = readPacket();
			if (p == null) {
				closeConnection();
				continue;
			}

			Logfile.writeToFile("Received message from "
					+ getHost().getHostAddress(), LogLevel.INFO);
			UnformattedPacket response = processor.keepAliveProcess(p);
			if (response.getHeader() == MessageType.EXIT.getInt()) {
				this.closeConnection();
				Logfile.writeToFile("Exit requested from client "
						+ getHost().getHostAddress(), LogLevel.INFO);
			} else {
				writePacket(response);
			}
		}
	}

	public void closeConnection() {
		this.alive = false;
		try {
			this.rawOut.close();
			this.rawIn.close();
			this.s.close();
			Logfile.writeToFile("Closed connection from: "
					+ getHost().getHostAddress(), LogLevel.INFO);
		} catch (IOException e) {
			Logfile.writeToFile("Failed to close the client socket",
					LogLevel.ERROR);
		}
	}

	public InetAddress getHost() {
		return this.s.getInetAddress();
	}

}
