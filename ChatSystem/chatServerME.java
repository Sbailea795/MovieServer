/****************************************************
*   Program Title: chatServer                       *
*   Author:  Austin Bailey                          *
*   Class: CSCI3550,  Fall 2021                     *   
*   Assignment #2                                   *   
*   Purpose:   Simple Chat Client and Server        *
****************************************************/

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Hashtable;

public class chatServerME {

    private static final int datagramSize = 164;
    public static Hashtable<Integer, String> dictionary = new Hashtable<Integer,String>();

    //Will take in necessary input, then open a local UDP port and connects to the server
    //Enters a loop until the user quits, checking for input and incoming traffic.
    //function is not multi-threaded but instead uses try-catch with timeouts on blocking IO
    //operations.
    public static void main(String argv[]) throws Exception {
        
        //handle 0 or 1 input of argv[1]; default is verbose mode
        boolean verbose = true;
        if (argv[1].equals("0")) verbose = false;
        
        //UDP socket on your local machine.
        int localPort = Integer.parseInt(argv[1]);
        DatagramSocket socketConnection = new DatagramSocket(localPort);
        
        /*Create Message and send JOIN REQ
        Message datum = new Message(0, "JOIN", userName);
        DatagramPacket sendPacket = new DatagramPacket(datum.serialize(), datagramSize, InetAddress.getByName(serverName), serverPort);
        socketConnection.send(sendPacket);*/

        if (verbose) System.out.println("DEBUG MESSAGE: " + "Starting Server loop.\n");

        /*While loop checks for user input sends it to the server, formated as necessary. It then takes on a blocking UDP socket check
          for 1/10 of a second to check for a new message. If it times out the loop will continue again, if another exception is thrown
          it will exit*/
        boolean loop = true;
        Message datum = new Message(0, "NULL", "NULL");

        while(loop){
            try {
                System.out.print(".");
                //blocking portion of port reading
                DatagramPacket datagram = new DatagramPacket(new byte[datagramSize], datagramSize);
                socketConnection.setSoTimeout(10);
                System.out.print(',');
                socketConnection.receive(datagram);
                datum.deserialize(datagram.getData());
                System.out.println("\nHandling");
                //parse and handle message as necessary. Only gets to thing point if no errors
                //were encountered.
                if (datum.getStr1().equals("QUIT")){
                    System.out.println("User " + datum.getStr2() + "sent QUIT REQ.");
                    int cid = -datum.getCid();
                    String host = datum.getStr2();
                    datum = new Message(cid, "QUIT", host);
                    repeatControlMessages(datum, socketConnection, localPort);
                    dictionary.remove(datum.getCid());
                }
                else if  (datum.getStr1().equals("JOIN")){
                    int index = 1;
                    String host = datum.getStr2();
                    while(dictionary.containsKey(index)){
                        index++;
                    }
                    dictionary.put(index, datum.getStr2());
                    System.out.println("User " + datum.getStr2() + "sent JOIN REQ.");
                    datum = new Message(index, "JOIN", host);
                    repeatControlMessages(datum, socketConnection, localPort);
                }
                else {
                    repeatTextMessages(datum, socketConnection, localPort);
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


        if (verbose) System.out.println("DEBUG MESSAGE: " + "Program terminating");
        
        //close & disconnect resources upon exit
        socketConnection.disconnect();
        socketConnection.close();

    }

    public static boolean repeatTextMessages(Message datum, DatagramSocket socket, int port){
        dictionary.forEach((cid, hostName) -> {
            if(datum.getCid() != cid)
                sendMessage(datum, socket, hostName, port); 
        });
        return true;
    }

    public static boolean repeatControlMessages(Message datum, DatagramSocket socket, int port){
        dictionary.forEach((cid, hostName) -> {
            sendMessage(datum, socket, hostName, port); 
        });
        return true;
    }

    public static boolean sendMessage(Message datum, DatagramSocket socket, String host, int port){
        try {
            DatagramPacket packet = new DatagramPacket(datum.serialize(), datagramSize, InetAddress.getByName(host), port);
            socket.send(packet);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}