import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class UDPClient {

    static int windowSize = 0;
    static int MSS = 1; //Maximum segment size
    static int sequenceNo = 0;
    static int lastAck = 0; //No of last acknowledgment
    static double chance=0.1; //Create probability of failure

    public static void main(String[] args) throws Exception{
        Scanner scr=new Scanner(System.in);
        //Prompt for window size input
        System.out.println( "Welcome\nEnter 1 for Stop and wait\nEnter 5 for Go-back-N\nPlease enter window size:");

        windowSize = scr.nextInt();

        //Create a socket
        DatagramSocket clientSocket = new DatagramSocket();
        //Set socket timer
        clientSocket.setSoTimeout(3000);
        byte[] sendData;
        //Load file
        BufferedReader inFromFile = new BufferedReader(new FileReader("src/testData.txt"));
        //Get required string
        String sentence = inFromFile.readLine();
        //Encode String
        sendData = sentence.getBytes();
        //Get host name
        InetAddress IPAddress = InetAddress.getByName("localhost" );
        //Create an array list for packets sent
        ArrayList<Pkt> sent = new ArrayList<>();
        //Get the ceiling length of string
        int lastSeq = (int) Math.ceil( (double) sendData.length / MSS);


        while(true){

            // Create a sending loop
            while(sequenceNo - lastAck < windowSize && sequenceNo < lastSeq){

                // Array for storage
                byte[] receiveData;

                // Copy segment of data bytes to array
                receiveData = Arrays.copyOfRange(sendData, sequenceNo*MSS, sequenceNo*MSS+MSS);

                // Create a new Pkt object
                Pkt packetObject = new Pkt(sequenceNo, receiveData, (sequenceNo == lastSeq-1));


                // Prep the Pkt object
                byte[] snt = Streamer.toBytes(packetObject);

                // Create the packet
                DatagramPacket packet = new DatagramPacket(snt, snt.length, IPAddress, 500);

                System.out.println("Sending packet: sequence number " + (sequenceNo+1) );

                // Add the packet to the sent array
                sent.add(packetObject);
                //Add a One-second delay
                TimeUnit.SECONDS.sleep(1);
                clientSocket.send(packet);
                sequenceNo++;

            } // End of while loop

                // Array for acknowledgments
                byte[] ackBytes = new byte[100];

                // Creating packet for the ACK
                DatagramPacket ack = new DatagramPacket(ackBytes, ackBytes.length);

                try{
                    // Receive the packet
                    clientSocket.receive(ack);

                    // Prep the acknowledgment
                    Ack ackObject = (Ack) Streamer.toObject(ack.getData());
                    lastAck = Math.max(lastAck, ackObject.getPacket());

                    System.out.println("Received acknowledgment for packet No:" + lastAck);

                    // Stop the program when last is true
                    if(ackObject.getPacket() == lastSeq){
                        break;
                    }




                }catch(SocketTimeoutException e){
                    // resend problem packets

                    for(int j = lastAck; j <sequenceNo; j++){

                        // Prep the Pkt object
                        byte [ ] snt = Streamer.toBytes(sent.get(j));

                        // Create the packet
                        DatagramPacket packet = new DatagramPacket(snt, snt.length, IPAddress, 500 );

                        // Send with some chance of loss
                        if(Math.random() > chance){
                        clientSocket.send(packet);
                        }else{
                            System.out.println("xxxxxxxxxxx Lost packet number " + sent.get(j).getSeq()+"xxxxxxxxxxx ");
                        }

                        System.out.println("<<<<<<<<<<<<< Resending packet :Sequence No " + (sent.get(j).getSeq()+1) );

                    }
                }


            }

            System.out.println("End of session");

        }

    }










