import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


/****************************************************
*   Program Title: SMTP_Emailer           *
*   Author:  Austin Bailey                          *
*   Class: CSCI3550,  Fall 2021                     *   
*   Assignment #1                                   *   
*   Purpose:   Simple Chat Client and Server        *
****************************************************/
public class SMTP_Emailer {
    //CRLF symbol
    private static final String CRLF = "\r\n";

    //Im not going to describe main formally, all it does is prompt for input and feeds it into send()
    //it is not designed to be refactored quickly nor does it have logical segmentation etc. It stands
    //as a proof of concept.
    public static void main(String argv[]) throws Exception {
        //handle 0 or 1 input of argv[1]; default is verbose mode
        boolean verbose = true;
        if (argv[1].equals("0")) verbose = false;
        
        String line = "";
        String datum = "";
        String To;
        String From;
        String Subject;
        String Body;

        //Buffered Reader from user input
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        //Initial socket on port 25 to connect to server; handled by following vars to create data streams
        Socket clientSocket = new Socket(argv[0], 25);
        //DataoutputStream to send messages to server
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        //Response messages read from bufferedReader
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        //by default the connection has a reply in buffer, print if necessary
        datum = inFromServer.readLine();
        if (verbose) printServerReply(datum);

        //send HELO
        send(outToServer, inFromServer, "HELO odin.ist.unomaha.edu", verbose, 250);

        //Handle and send MAIL FROM:
        System.out.print("Enter Senders email address: ");
        From = inFromUser.readLine();
        send(outToServer, inFromServer, "MAIL FROM: <" + From + ">", verbose, 250);

        //Handle RCPT TO:
        System.out.print("Enter Receiver's email address: ");
        To = inFromUser.readLine();
        send(outToServer, inFromServer, "RCPT TO: <" + To + ">", verbose, 250);

        //takes subject line, fed into handling of DATA cmd below
        System.out.print("Enter the subject of the message:");
        Subject = inFromUser.readLine();

        //takes the message body via while loop. breaks on '.'
        System.out.print("Enter the body of the message:");
        datum ="";
        while ( ! (line = inFromUser.readLine()).equals("." ) )
            datum += line + CRLF;
        
        //send DATA command
        send(outToServer, inFromServer, "DATA", verbose, 354);
        //follow DATA with body of msg, then QUIT. the message is automatically given CRLF.CRLF
        Body = "To: " + To + CRLF + "From: " + From + CRLF + "Subject: " + Subject + CRLF + datum + ".";
        send(outToServer, inFromServer, Body, verbose, 250);
        send(outToServer, inFromServer, "QUIT", verbose, 221);

        System.out.println("Email Concluded; Program terminating.");

        clientSocket.close();

    }
    /****************************************************
    *   FUNCTION  send()                                *
    *       sends a SMTP command and validates server   *
    *       reply code                                  *
    *  INPUT PARAMETERS :                               *
    *    target : channel to send command on            *
    *    replyBuffer : channel to listen for reply on   *
    *    data : msg to send to server, appened with CRLF*
    *    verbose : bool for printing response msg       *
    *    rc : reply code expected; validated against    *
    *       servers actual rc response                  * 
    *  OUTPUT :                                         *    
    *    returns void; throws error if message/response *
    *       break protocol                              *       
    ****************************************************/
    //Function send data over a channel and parses the response. if the expected reply code (rc) doesnt match then
    //It wil throw an error
    static public void send(DataOutputStream target, BufferedReader replyBuffer, String data, boolean verbose, int rc) throws Exception
    {
        if (verbose) System.out.println("SENDING: " + data);
        target.writeBytes(data + CRLF);
        String reply = replyBuffer.readLine();
        if (verbose) printServerReply(reply);

        if ( rc != Integer.parseInt(reply.substring(0, 3)) )
            throw new IOException("Server returned unexpected response: " + reply);
    }

    static public void printServerReply(String reply)
    {
        System.out.println("FROM SERVER: " + reply + '\n');
    }
}