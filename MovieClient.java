/****************************************************
*   Program Title: chatClient                       *
*   Author:  Austin Bailey                          *
*   Class: CSCI3550,  Fall 2021                     *   
*   Assignment #2                                   *   
*   Purpose:   Simple Chat Client and Server        *
****************************************************/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Timer;


public class MovieClient {

    private static final int datagramSize = 20000;
    private final static int BufferSize = 8;
    private static Frame[] frameBuffer = new Frame[BufferSize];
    private static Frame[] tmpBuffer = new Frame[BufferSize];
    private static double begin = System.currentTimeMillis()/1000;
    private static PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8); 

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
        out.println("Enter your NUID (or a random number/port):");
        int NUID = Integer.parseInt(in.readLine());
        int localPort = NUID % 65535;

        //UDP socket on your local machine.
        DatagramSocket socketConnection = new DatagramSocket(localPort);

        //Create MovieMessage and send 'JOIN REQ'
        MovieMessage datum = new MovieMessage(-1, 0, "");
        DatagramPacket sendPacket = new DatagramPacket(datum.serialize(), datagramSize, InetAddress.getByName(serverName), serverPort);
        socketConnection.send(sendPacket);
        if (verbose) out.println("DEBUG MESSAGE: " + "attempted to join...\n");
        
        /*Attempt to connect to server. Will wait up to 5 seconds for a reply. If no reply is found the program exits on exception.
          Otherwise, the program takes the CID assigned and displays the join message. Below is the infinite loop for user input
          while inside the chatroom*/
        DatagramPacket receivePacket = new DatagramPacket(new byte[datagramSize], datagramSize);
        try{
            socketConnection.setSoTimeout(5000);
            socketConnection.receive(receivePacket);
            datum.deserialize(receivePacket.getData());
            if (datum.getSegment() == -1 && datum.getFrameNumber() == 0){
                if (verbose) out.println("DEBUG MESSAGE: " + "Connection Confirmed\n");
            }
            else
                System.exit(-1);
        }
        catch (SocketTimeoutException e) {
            out.println("\n|***|");
            e.printStackTrace();
            out.println("|***|\n");
            System.exit(1);
        }
        out.println("Movie will begin shortly.");
        
        frameBuffer = refreshBuffer(0, socketConnection, serverName, serverPort);
        Thread.sleep(1000);
        ExecutorService threadpool = Executors.newCachedThreadPool();
        begin = System.currentTimeMillis()/1000;
        Timer frameHandler = new Timer();
        
        //only iterating through this once, needs to go throught it 7304 times
        for (int segment = 1; segment < 7304; segment++) {
            Thread.sleep(60);
            final int SEGMENT = segment;
            Future<?> RefreshBuffer = threadpool.submit(() -> {
                try {
                    tmpBuffer = refreshBuffer(SEGMENT, socketConnection, serverName, serverPort);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
            //set frame to play after a delay and put thread to sleep. This makes it so the frames are gapped by a proper timing.
            for (int bufferedFrame = 0; bufferedFrame < BufferSize; bufferedFrame++) {
                FrameTask frameTask = new FrameTask(segment * 8 + bufferedFrame, begin, frameBuffer[bufferedFrame].getFrame() + "\n");
                
                if ( (System.currentTimeMillis()/1000 - begin ) - (segment+(double)bufferedFrame/8) > 0){
                    frameHandler.schedule(frameTask, 100);
                    Thread.sleep(90);
                }
                else{
                    frameHandler.schedule(frameTask, 100);
                    Thread.sleep(120);
                }

            }
            //wait for buffer to refresh and make sure movie isnt playing too fast
            while(!RefreshBuffer.isDone()); //out.println("Waiting on Segment "+ segment +".");
            while( (System.currentTimeMillis()/1000 - begin ) - (segment+1) < 0 ); //out.println((System.currentTimeMillis()/1000 - begin ) - (segment+1));
            //after all async tasksa are completed, write new data into the frame buffer.
            frameBuffer = tmpBuffer;
        }
    }

    //this function automates requests for a segment of data and writes it to a buffer.
    private static Frame[] refreshBuffer(int segment, DatagramSocket socketConnection, String serverName, int serverPort){
        Frame[] buffer = new Frame[BufferSize];
        for (int frames = 0; frames < BufferSize; frames++) {
            try{
            MovieMessage datum = new MovieMessage(segment, frames, "");
            DatagramPacket sendPacket = new DatagramPacket(datum.serialize(), datagramSize, InetAddress.getByName(serverName), serverPort);
           
            //do not set below 200 plz
            socketConnection.setSoTimeout(300);

            socketConnection.send(sendPacket);
            DatagramPacket receivePacket = new DatagramPacket(new byte[datagramSize], datagramSize);
            socketConnection.receive(receivePacket);
            datum.deserialize(receivePacket.getData());
            //out.println("Received: <" + datum.getSegment() +", "+ datum.getFrameNumber()+">");
            buffer[frames] = new Frame(datum.getFrame());
            } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            }
        }
        return buffer;
    }
}
