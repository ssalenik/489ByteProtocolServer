package networking.protocol;

import networking.UnformattedPacket;

public interface IAsyncClientWriter {

	public void writePacket(UnformattedPacket pkt);
	
}
