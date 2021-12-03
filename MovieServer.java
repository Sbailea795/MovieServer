
/****************************************************
*   Program Title: chatServer                       *
*   Author:  Austin Bailey                          *
*   Class: CSCI3550,  Fall 2021                     *   
*   Assignment #2                                   *   
*   Purpose:   Simple Chat Client and Server        *
****************************************************/

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
public class MovieServer {

    private static final int datagramSize = 5000;
    private static final String fileLocation = "A:\\SWANH160x33\\";
    public static void main(String argv[]) throws Exception {
        
        //handle 0 or 1 input of argv[1]; default is verbose mode
        boolean verbose = true;
        if (argv[1].equals("0")) verbose = false;
        
        //UDP socket on your local machine.
        DatagramSocket socketConnection = new DatagramSocket(Integer.parseInt(argv[0]));
        if (verbose) System.out.println("DEBUG MESSAGE: " + "Starting Server loop.\n");

        /*While loop checks for user input and crafts a reply/forward, formated as necessary. It takes on a blocking UDP socket check
          for 1/10 of a second to check for a new message. If it times out the loop will continue again, if another exception is thrown
          it will exit*/
        boolean loop = true;
        MovieMessage datum = new MovieMessage(0, 0, "NULL");
        MovieMessage reply = new MovieMessage(0, 0, "NULL");
        while(loop){
            try {
                //blocking portion of port reading
                byte[] receive = new byte[datagramSize];
                DatagramPacket datagram = new DatagramPacket(receive, receive.length);
                socketConnection.setSoTimeout(25);
                socketConnection.receive(datagram);
                socketConnection.connect(datagram.getAddress(), datagram.getPort());
                datum.deserialize(datagram.getData());
                
                //parse and handle message as necessary. Only gets to thing point if no errors
                //were encountered.
                if (verbose) System.out.println("Received Request for segment: " + datum.getSegment() +", frame: "+ datum.getFrameNumber() + ">");

                //Each of the 2 control messages and text messages handled here
                if (datum.getSegment() < 0){
                    reply = new MovieMessage(datum.getSegment(), datum.getFrameNumber(), "");
                    if (verbose) System.out.println("Sending: <" + reply.getSegment() +", "+ reply.getFrameNumber()+">");
                    sendMessage(reply, socketConnection);
                }
                else {
                    File f = new File(fileLocation + String.format("%05d", datum.getSegment()) + ".txt");
                    f.setReadOnly();
                    String frameText = "";
                    Scanner reader = new Scanner(f);  
                    for (int i = 0; i < 33 * datum.getFrameNumber(); i++) {
                            reader.nextLine();
                        }
                    for (int i = 0; i < 33; i++) {
                        frameText += reader.nextLine() + "\n";
                    }
                    reader.close();
                    reply = new MovieMessage(datum.getSegment(), datum.getFrameNumber(), frameText);
                    if (verbose) System.out.println("Sending: <" + reply.getSegment() +", "+ reply.getFrameNumber()+">");
                    sendMessage(reply, socketConnection);

                }
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
    }

    public static boolean sendMessage(MovieMessage datum, DatagramSocket socket/*, SocketAddress addr*/){
        try {
            DatagramPacket packet = new DatagramPacket(datum.serialize(), datagramSize/*, addr*/);
            socket.send(packet);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}