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
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.Timer;


public class MovieClient {

    private static final int datagramSize = 15000;
    private final static int BufferSize = 8;
    private static Frame[] frameBufferA = new Frame[BufferSize];
    private static Frame[] frameBufferB = new Frame[BufferSize];
    private static double begin = System.currentTimeMillis()/1000;

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

        //Create MovieMessage and send 'JOIN REQ'
        MovieMessage datum = new MovieMessage(-1, 0, "");
        DatagramPacket sendPacket = new DatagramPacket(datum.serialize(), datagramSize, InetAddress.getByName(serverName), serverPort);
        socketConnection.send(sendPacket);
        if (verbose) System.out.println("DEBUG MESSAGE: " + "attempted to join...\n");
        
        /*Attempt to connect to server. Will wait up to 5 seconds for a reply. If no reply is found the program exits on exception.
          Otherwise, the program takes the CID assigned and displays the join message. Below is the infinite loop for user input
          while inside the chatroom*/
        DatagramPacket receivePacket = new DatagramPacket(new byte[datagramSize], datagramSize);
        try{
            socketConnection.setSoTimeout(5000);
            socketConnection.receive(receivePacket);
            datum.deserialize(receivePacket.getData());
            if (datum.getSegment() == -1 && datum.getFrameNumber() == 0 && (verbose))
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
        System.out.println("Movie will begin shortly.");
        
        frameBufferB = refreshBuffer(0, socketConnection, serverName, serverPort);

        for (int i = 0; i < frameBufferB.length; i++)
            System.out.println(frameBufferB[0].getFrame());

        ExecutorService threadpool = Executors.newCachedThreadPool();
        begin = System.currentTimeMillis()/1000;
        Timer frameHandler = new Timer();
        
        
        //only iterating through this once, needs to go throught it 7304 times
        for (int frame = BufferSize; frame < 7304; frame+=BufferSize*2) {
            System.out.println("**********Segemnt: " + frame + "************");

            int segment = frame/BufferSize;
            Future<?> RefreshA = threadpool.submit(() -> {
                try {
                    frameBufferA = refreshBuffer(segment, socketConnection, serverName, serverPort);
                    for (int i = 0; i < frameBufferA.length; i++)
                        System.out.println(frameBufferA[0].getFrame());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });

            for (int bufferedFrame = 0; bufferedFrame < BufferSize; bufferedFrame++) {
                final int buffer = bufferedFrame;
                final int frameCount = frame;
                TimerTask frameTask = new TimerTask() {
                    public void run() {
                        System.out.print(String.format("Frame: %09d ", frameCount + buffer) + 
                                        String.format("Time Stamp: %09d", (frameCount + buffer) / 8) +
                                        String.format("Time: %06f", System.currentTimeMillis()/1000-begin));
                        System.out.print(frameBufferB[buffer].getFrame());
                    }
                };
                Thread.sleep(50);
                if (begin - System.currentTimeMillis()/1000 * 8 - (frame +bufferedFrame) > 1)
                    frameHandler.schedule(frameTask, 1075);
                else
                    frameHandler.schedule(frameTask, 1125);
            }
        

            while(!RefreshA.isDone());// System.out.println("Waiting on Segment "+ segment +".");
            System.exit(0);
            Future<?> RefreshB = threadpool.submit(() -> {
                try {
                    frameBufferB = refreshBuffer(segment+1, socketConnection, serverName, serverPort);
                    
                    for (int i = 0; i < frameBufferB.length; i++)
                        System.out.println(frameBufferB[0].getFrame());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
                     

            for (int bufferedFrame = 0; bufferedFrame < BufferSize; bufferedFrame++) {
                final int buffer = bufferedFrame;
                final int frameCount = frame + BufferSize;
                TimerTask frameTask = new TimerTask() {
                    public void run() {
                        System.out.print(String.format("Frame: %9d ", frameCount + buffer) + 
                                        String.format("Time Stamp: %09d ", (frameCount + buffer) / 8) +
                                        String.format("Time: %06f ", System.currentTimeMillis()/1000-begin));
                        System.out.print(frameBufferA[buffer].getFrame()+"\nNumber:"+frameCount+buffer);
                    }
                };
                if (begin - System.currentTimeMillis()/1000 * 8 - (frame +bufferedFrame) > 1)
                    frameHandler.schedule(frameTask, 1075);
                else
                    frameHandler.schedule(frameTask, 1125);
                Thread.sleep(50);
            }
        
             while(!RefreshB.isDone()) //System.out.println("Waiting on Segment "+ (segment+1) +".");  
             while((begin-System.currentTimeMillis()/1000) - ((frame+BufferSize*2)/8) < 0);

        }
    }
    private static Frame[] refreshBuffer(int segment, DatagramSocket socketConnection, String serverName, int serverPort) throws Exception{
        Frame[] buffer = new Frame[BufferSize];
        for (int frames = 0; frames < BufferSize; frames++) {
            MovieMessage datum = new MovieMessage(segment, frames, "");
            DatagramPacket sendPacket = new DatagramPacket(datum.serialize(), datagramSize, InetAddress.getByName(serverName), serverPort);
           
            //do not set below 200 plz
            socketConnection.setSoTimeout(200);

            socketConnection.send(sendPacket);
            DatagramPacket receivePacket = new DatagramPacket(new byte[datagramSize], datagramSize);
            socketConnection.receive(receivePacket);
            datum.deserialize(receivePacket.getData());
            //System.out.println("Received: <" + datum.getSegment() +", "+ datum.getFrameNumber()+">");
            buffer[frames] = new Frame(datum.getFrame());
            Thread.sleep(50);
        }
        return buffer;
    }
}
