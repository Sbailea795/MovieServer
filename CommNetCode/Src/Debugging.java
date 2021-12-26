import java.util.Timer;
import java.util.TimerTask;

public class Debugging {
    public static void main(String[] args) {


        Timer frameHandler = new Timer();
        double begin = System.currentTimeMillis()/1000;

        TimerTask frameTask = new TimerTask() {
            public void run() {
                System.out.print(String.format("Frame: %09d ", 1) + 
                                String.format("Time Stamp: %09d ", (1) / 8) +
                                String.format("Time: %06f \n", System.currentTimeMillis()/1000-begin));
                System.out.println("FRAME " + 1);
            }
        };
        for (int frame = 0; frame < 8; frame++) {
            frameHandler.schedule(frameTask, 125);
        }
    }
}
