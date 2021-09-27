import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SMTPtest {
    private static final String CRLF = "\r\n";
    public static void main(String argv[]) throws Exception {
        boolean verbose = true;
        if (argv[1].equals("0")) verbose = false;
        System.out.println(verbose);
        String line = "";
        String datum = "";
        String To;
        String From;
        String Subject;
        String Body;

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        Socket clientSocket = new Socket(argv[0], 25);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        datum = inFromServer.readLine();
        if (verbose) printServerReply(datum);
        send(outToServer, inFromServer, "HELO odin.ist.unomaha.edu", verbose, 250);

        System.out.print("Enter Senders email address: ");
        From = inFromUser.readLine();
        send(outToServer, inFromServer, "MAIL FROM: <" + From + ">", verbose, 250);

        System.out.print("Enter Receiver's email address: ");
        To = inFromUser.readLine();
        send(outToServer, inFromServer, "RCPT TO: <" + To + ">", verbose, 250);

        System.out.print("Enter the subject of the message:");
        Subject = inFromUser.readLine();

        System.out.print("Enter the body of the message:");
        datum ="";
        while ( ! (line = inFromUser.readLine()).equals("." ) )
            datum += line + CRLF;
        
        send(outToServer, inFromServer, "DATA", verbose, 354);
        Body = "To: " + To + CRLF + "From: " + From + CRLF + "Subject: " + Subject + CRLF + datum + "." + CRLF;
        
        send(outToServer, inFromServer, Body, verbose, 250);
        send(outToServer, inFromServer, "QUIT", verbose, 221);

        System.out.println("Email Concluded; Program terminating.");

        clientSocket.close();

    }
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