import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;



public class UDPServer {



	public static void main(String[] args) throws Exception{

		//Create probability of failure
		final double chance = 0.1;
		//Create socket
		DatagramSocket arrived = new DatagramSocket(500);

		byte[] receivedData = new byte[UDPClient.MSS + 100];

		//Create packet count
		int expected = 0;
		//Create an Array list for packets received
		ArrayList<Pkt> received = new ArrayList<Pkt>();
		//Condition to end transmission
		boolean end = false;


		while(!end){
			
			System.out.println("Waiting...\n\n\n");
			
			// Receive packet
			DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
			arrived.receive(receivedPacket);

			// Prep the Pkt object
			Pkt packet = (Pkt) Streamer.toObject(receivedPacket.getData());

			//Print data
			System.out.println("Packet: Sequence No " + (packet.getSeq()+1) +", received. (Is last packet? : " + packet.isLast() + " )");

			//If all packets received: end transmission
			if(packet.getSeq() == expected && packet.isLast()){
				
				expected++;
				received.add(packet);
				String s = new String(packet.data, StandardCharsets.UTF_8);
				System.out.println("Output : " + s);
				System.out.println("Last packet received");
				
				end = true;
				//Store correct packets
			}else if(packet.getSeq() == expected){
				received.add(packet);
				expected ++;
				//Print each letter as it arrives
				String s = new String(packet.data, StandardCharsets.UTF_8);
				System.out.println("Output : " + s);
				System.out.println("Packet stored");
			}else{
				System.out.println("Unordered packet not accepted ");
			}
			
			// Create an Ack object
			Ack ackObject = new Ack(expected);
			
			// To stream
			byte[] ackBytes = Streamer.toBytes(ackObject);
			

			DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length, receivedPacket.getAddress(), receivedPacket.getPort());
			
			// Send packet with some probability of loss
			if(Math.random() > chance){
				TimeUnit.SECONDS.sleep(1);

				arrived.send(ackPacket);
			}else{
				System.out.println("xxxxxxxxxxxx Lost acknowledgment: Sequence No " + ackObject.getPacket()+"xxxxxxxxxxxx");
			}
			
			System.out.println("<<<<<<<<<<<<<< Sending acknowledgment: Sequence No " + (expected) );
			

		}
		
		// Print all received data
		System.out.println(" \n************DATA RECEIVED*************");
		
		for(Pkt p : received){
			for(byte b: p.getData()){
				System.out.print((char) b);
			}
		}
		
	}}
	
	