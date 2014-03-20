package networking;

import java.util.Arrays;
import java.nio.*;

public class UnformattedPacket {

	private int size;
	private int header;
	private int header2;
	private byte[] payload;
	
	public UnformattedPacket(byte[] received) {
		byte[] hArray = Arrays.copyOfRange(received,0,4);
		byte[] h2Array = Arrays.copyOfRange(received,4,8);
		byte[] sArray = Arrays.copyOfRange(received, 8, 12);
		header = ByteBuffer.wrap(hArray).getInt();
		header2 = ByteBuffer.wrap(h2Array).getInt();
		size = ByteBuffer.wrap(sArray).getInt();
		payload = Arrays.copyOfRange(received, 8, size + 8);
	}
	
	public UnformattedPacket(int h, byte[] data) {
		this(h,data.length,data);
	}
	
	public UnformattedPacket(int h, int s, byte[] p) {
		header = h;
		size = s;
		payload = p;
		header2 = 0;
	}
	
	public UnformattedPacket(int h, int h2, int s, byte[] p) {
		header = h;
		size = s;
		header2 = h2;
		payload = p;
	}
	
	public UnformattedPacket(int h, String payload) {
		byte[] p = payload.getBytes();
		
		this.header = h;
		this.header2 = 0;
		this.payload = p;
		this.size = p.length;
	}
	
	public UnformattedPacket(byte[] h, byte[] h2, byte[] s, byte[] p) {
		header = ByteBuffer.wrap(h).getInt();
		header2 = ByteBuffer.wrap(h2).getInt();
		size = ByteBuffer.wrap(s).getInt();
		payload = p;
	}
	
	public int getSize() { return size; }
	public int getHeader() { return header; }
	public int getSubHeader() { return header2; }
	public byte[] getPayload() { return payload; }
	
	
	public static int ByteArrayToInt(byte[] b) {
		return ByteBuffer.wrap(b).getInt();
	}
	
	public byte[] serialize() {
		byte[] hArray = ByteBuffer.allocate(4).putInt(header).array();
		byte[] h2Array = ByteBuffer.allocate(4).putInt(header2).array();
		byte[] sArray = ByteBuffer.allocate(4).putInt(size).array();
		byte[] arr = new byte[12 + size];
		System.arraycopy(hArray, 0, arr, 0, 4);
		System.arraycopy(h2Array, 0, arr, 4, 4);
		System.arraycopy(sArray, 0, arr, 8, 4);
		System.arraycopy(payload, 0, arr, 12, size);
		return arr;
	}
	
	public String payloadAsString() {
		return new String(payload);
	}
	
	public static UnformattedPacket CreateResponsePacket(int h1, int h2, String payload) {
		byte[] p = payload.getBytes();
		return new UnformattedPacket(h1,h2,p.length,p);
	}
	
	public static UnformattedPacket CreateResponsePacket(int h1, int h2, byte[] payload) {
		return new UnformattedPacket(h1,h2,payload.length,payload);
	}
	
}
