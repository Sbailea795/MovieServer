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

public class chatClient {

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
        
        /*While loop checks for user input sends it to the server, formated as necessary. It then takes on a blocking UDP socket check
          for 1/10 of a second to check for a new message. If it times out the loop will continue again, if another exception is thrown
          it will exit*/
        boolean loop = true;
        while(loop){
            try {
                //conditional user input reading (non-blocking) 
                if(in.ready()){
                    System.out.println("\t\b\b\bSent by You " + formatter.format(System.currentTimeMillis()) + ".\n");
                    String input = in.readLine();
                    if (verbose) System.out.println("DEBUG MESSAGE: parsing '" + input + "'\n   And sending it to the server\n");
                    if (input.equals("QUIT")){
                        datum = new MovieMessage(-1, 0, "");
                        loop = false;
                    }
                    else
                    System.out.println("\t\b\b\b" + input + " is an invalid command.\n");

                    sendPacket = new DatagramPacket(datum.serialize(), datagramSize, InetAddress.getByName(serverName), serverPort);
                    socketConnection.send(sendPacket);
                }
                //blocking portion of port reading     
                byte[] receive = new byte[datagramSize];
                DatagramPacket datagram = new DatagramPacket(receive, receive.length);
                socketConnection.setSoTimeout(100);
                socketConnection.receive(datagram);
                datum.deserialize(datagram.getData());
                //parse and handle message as necessary. Only gets to thing point if no errors
                //were encountered.
                printMessage(datum);
            }
            //ignore timeouts (they are to be expected)
            catch (SocketTimeoutException e){
                continue;
            }
            //Other exceptions are not accounted for
            catch (Exception e){
                System.out.println("/**/");
                e.printStackTrace();
                System.out.println("/**/");
                System.exit(0);
            }
        }
        if (verbose) System.out.println("DEBUG MESSAGE: " + "quit sent");
        if (verbose) System.out.println("DEBUG MESSAGE: " + "Program terminating");
        
        //close & disconnect resources upon exit
        in.close();
        socketConnection.disconnect();
        socketConnection.close();

    }


    /****************************************************
    *  FUNCTION printMessage:                           *
    *    printJoinMessage will print normal messages of *
    *    another user in the chat.                      *
    *  INPUT PARAMETERS :                               *           
    *    MovieMessage - The message to be printed            *    
    *  OUTPUT :                                         *    
    *    Gives a boolean value to indicate the funtion  *
    *    completed without error.                       *    
    ****************************************************/
    public static boolean printMessage(MovieMessage datum){
        System.out.println(datum.getFrame());
        return true;
    }
}