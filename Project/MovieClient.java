/****************************************************
*   Program Title: chatClient                       *
*   Author:  Austin Bailey                          *
*   Class: CSCI3550,  Fall 2021                     *   
*   Assignment #2                                   *   
*   Purpose:   Simple Chat Client and Server        *
****************************************************/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;

public class MovieClient {

    private static final int datagramSize = 5000;
    static private SimpleDateFormat formatter= new SimpleDateFormat("'at' HH:mm:ss z");

    //Will take in necessary input, then open a local UDP port and connects to the server
    //Enters a loop until the user quits, checking for input and incoming traffic.
    //function is not multi-threaded but instead uses try-catch with timeouts on blocking IO
    //operations.
    public static void main(String argv[]) throws Exception {
        
        //handle 0 or 1 input of argv[1]; default is verbose mode
        boolean verbose = true;
        if (argv[2].equals("0")) verbose = false;
        
        int serverPort = Integer.parseInt(argv[1]);
        String serverName = argv[0];

        //Buffered Reader for user input.
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        //NUID is used in port number
        System.out.println("Enter your NUID (or a random number/port):");
        int NUID = Integer.parseInt(in.readLine());
        int localPort = NUID % 65535;

        //UDP socket on your local machine.
        DatagramSocket socketConnection = new DatagramSocket(localPort);

        //Create MovieMessage and send JOIN REQ
        MovieMessage datum = new MovieMessage(1, 0, "");
        DatagramPacket sendPacket = new DatagramPacket(datum.serialize(), datagramSize, InetAddress.getByName(serverName), serverPort);
        socketConnection.send(sendPacket);
        if (verbose) System.out.println("DEBUG MESSAGE: " + "attempted to join...\n");
        
        /*Attempt to connect to server. Will wait up to 5 seconds for a reply. If no reply is found the program exits on exception.
          Otherwise, the program takes the CID assigned and displays the join message. Below is the infinite loop for user input
          while inside the chatroom*/
        try{
            DatagramPacket receivePacket = new DatagramPacket(new byte[datagramSize], datagramSize);
            socketConnection.setSoTimeout(5000);
            socketConnection.receive(receivePacket);
            datum.deserialize(receivePacket.getData());
            if (datum.getSegment() == -1 && datum.getFrameNumber() == -1 && (verbose))
                System.out.println("DEBUG MESSAGE: " + "Connection Confirmed\n");
            else
                System.exit(-1);
        }
        catch (SocketTimeoutException e) {
            System.out.println("\n|***|");
            e.printStackTrace();
            System.out.println("|***|\n");
            System.exit(1);
        }
}
