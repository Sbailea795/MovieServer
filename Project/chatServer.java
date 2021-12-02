
/****************************************************
*   Program Title: chatServer                       *
*   Author:  Austin Bailey                          *
*   Class: CSCI3550,  Fall 2021                     *   
*   Assignment #2                                   *   
*   Purpose:   Simple Chat Client and Server        *
****************************************************/

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Hashtable;

public class chatServer {

    private static final int datagramSize = 5000;
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
                socketConnection.setSoTimeout(100);
                socketConnection.receive(datagram);
                datum.deserialize(datagram.getData());
                
                //parse and handle message as necessary. Only gets to thing point if no errors
                //were encountered.
                if (verbose) System.out.println("Received: <" + datum.getCid() +", "+datum.getStr1()+", "+datum.getStr2()+">");

                //Each of the 2 control messages and text messages handled here
                if (datum.getStr1().equals("QUIT")){
                    int cid = -datum.getCid();
                    reply = new MovieMessage(cid, "QUIT", IPmapping.get(-datum.getCid()).toString());
                    repeatControlMessages(reply, socketConnection);
                    IPmapping.remove(datum.getCid());
                    if (verbose) System.out.println("Sending: <" + reply.getCid() +", "+reply.getStr1()+", "+reply.getStr2()+">");
                }
                else if  (datum.getStr1().equals("JOIN")){
                    int index = 0; 
                    while(IPmapping.containsKey(++index)){};
                    IPmapping.put(index, datagram.getSocketAddress());
                    reply = new MovieMessage(index, "JOIN", IPmapping.get(index).toString());
                    repeatControlMessages(reply, socketConnection);
                    if (verbose) System.out.println("Sending: <" + reply.getCid() +", "+reply.getStr1()+", "+reply.getStr2()+">");

                }
                else {
                    repeatTextMessages(datum, socketConnection);
                    if (verbose) System.out.println("Sending: <" + reply.getCid() +", "+reply.getStr1()+", "+reply.getStr2()+">");

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

    /***********************************************
    * METHOD: repeatTextMessages/ControlMessages:  *
    *  Uses the global CID-to-Socket hash table    *
    *  to repeat texts to all but the sender and   *
    *  control replies to all users.               *
    *  Wrapper for send message                    * 
    * INPUT:                                       *
    *     the message and a socket to send it via  *
    * OUTPUT:                                      *
    *     returns true if successful               *
    ***********************************************/
    public static boolean repeatTextMessages(MovieMessage datum, DatagramSocket socket){
        IPmapping.forEach((cid, hostName) -> {
            if(datum.getCid() != cid)
                sendMessage(datum, socket, IPmapping.get(cid)); 
        });
        return true;
    }

    //See above method block for RCM method explaination
    public static boolean repeatControlMessages(MovieMessage datum, DatagramSocket socket){
        IPmapping.forEach((cid, hostName) -> {
            sendMessage(datum, socket, IPmapping.get(cid)); 
        });
        return true;
    }

    /***********************************************
    * METHOD: sendMessage:                         *
    *  sends a message to the specified socket     *
    *  via the specified local socket              *
    * INPUT:                                       *
    *     the message and a socket to send it via, *
    *     as well as an address to hit             *
    * OUTPUT:                                      *
    *     returns true if successful               *
    ***********************************************/
    public static boolean sendMessage(MovieMessage datum, DatagramSocket socket, SocketAddress addr){
        try {
            DatagramPacket packet = new DatagramPacket(datum.serialize(), datagramSize, addr);
            socket.send(packet);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}