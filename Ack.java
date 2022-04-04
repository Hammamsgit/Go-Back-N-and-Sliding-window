import java.io.Serializable;

//Acknowledgment
public class Ack implements Serializable {
	
	private int packet;

	public Ack(int packet) {
		super();
		this.packet = packet;
	}

	public int getPacket() {
		return packet;
	}

	public void setPacket(int packet) {
		this.packet = packet;
	}
	
	

}