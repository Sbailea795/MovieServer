
/****************************************************
*   Program Title: chatServer                       *
*   Author:  Austin Bailey                          *
*   Class: CSCI3550,  Fall 2021                     *   
*   Assignment #2                                   *   
*   Purpose:   Simple Chat Client and Server        *
****************************************************/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class MovieServer {

    private static final int datagramSize = 20000;
    private static final String fileLocation = "\\MovieSegments\\";
    public static void main(String argv[]) throws Exception {
        PrintStream out = new PrintStream(System.out, true, "UTF-8"); 
        //handle 0 or 1 input of argv[1]; default is verbose mode
        boolean verbose = true;
        if (argv[1].equals("0")) verbose = false;
        
        //UDP socket on your local machine.
        DatagramSocket socketConnection = new DatagramSocket(Integer.parseInt(argv[0]));
        if (verbose) out.println("DEBUG MESSAGE: " + "Starting Server loop.\n");

        /*While loop checks for user input and crafts a reply, formated as necessary. It takes on a blocking UDP socket check
          for 1/10 of a second to check for a new message. If it times out the loop will continue again, if another type of exception is thrown
          it will exit*/
        boolean loop = true;
        MovieMessage datum = new MovieMessage(0, 0, "NULL");
        MovieMessage reply = new MovieMessage(0, 0, "NULL");
        while(loop){
            try {
                //blocking portion of port reading
                byte[] receive = new byte[datagramSize];
                DatagramPacket datagram = new DatagramPacket(receive, receive.length);
                socketConnection.setSoTimeout(100);
                socketConnection.receive(datagram);
                socketConnection.connect(datagram.getAddress(), datagram.getPort());
                datum.deserialize(datagram.getData());
                
                //parse and handle message as necessary. Only gets to thing point if no errors
                //were encountered.
                if (verbose) out.println("Received Request for segment: " + datum.getSegment() +", frame: "+ datum.getFrameNumber() + ">");

                //Each of the 2 control messages and text messages handled here

                //anything less than 0 is functionally a ping, and hte server will just repeat the message
                if (datum.getSegment() < 0){
                    reply = new MovieMessage(datum.getSegment(), datum.getFrameNumber(), "");
                    if (verbose) out.println("Sending: <" + reply.getSegment() +", "+ reply.getFrameNumber()+">");
                    sendMessage(reply, socketConnection);
                }
                //Server will open the segment in question, skip the unnecessary lines, and read the 33 lines that make up the requested frame
                else {
                    File f = new File(fileLocation + String.format("%05d", datum.getSegment()) + ".txt");
                    f.setReadOnly();
                    String frameText = "";
                    FileReader fr = new FileReader(f, StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(fr);
                    for (int i = 0; i < 33 * datum.getFrameNumber(); i++) {
                        reader.readLine();
                    }
                    for (int i = 0; i < 33; i++) {
                        frameText += reader.readLine() + "\n";
                    }
                    if (argv[1].equals("2")) out.println(frameText);
                    reader.close();
                    reply = new MovieMessage(datum.getSegment(), datum.getFrameNumber(), frameText);
                    if (verbose) out.println("Sending: <" + reply.getSegment() +", "+ reply.getFrameNumber()+">");
                    sendMessage(reply, socketConnection);
                    reply.deserialize(reply.serialize());
                    if (argv[1].equals("2")) out.println(reply.getFrame());
                    if (verbose) out.println("End: <" + reply.getSegment() +", "+ reply.getFrameNumber()+">\n");

                }
            }
            //ignore timeouts (they are to be expected)
            catch (SocketTimeoutException e){
                continue;
            }
            //Other exceptions are not accounted for
            catch (Exception e){
                out.println("/**/");
                e.printStackTrace();
                out.println("/**/");
                System.exit(0);
            }
        }
    }

    //an encapsulation of sending a message, to make things more standardized and easier to read.
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