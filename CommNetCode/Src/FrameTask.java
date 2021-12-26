import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.TimerTask;


// This is a TimerTask with some slight modifications. The FrameTask as variables that allow it
// to effectively instantiate tasks with info about an individual frame. Its mostly for the peripheral
// timing info.

public class FrameTask extends TimerTask {
    int frameCount;
    double beginTime;
    String frame;

    public FrameTask(int frameCount, double beginTime, String frame) {
        this.frameCount = frameCount;
        this.beginTime = beginTime;
        this.frame = frame;
    }

    @Override
    public void run() {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
            out.print(String.format("Frame: %09d ", frameCount) +
                      String.format("Segment: <%04d, %1d> ", frameCount/8, frameCount % 8) + 
                      String.format("Time Stamp: %06.3f ", ((double)frameCount / 8)) +
                      String.format("Time: %3.3f\n", (double)System.currentTimeMillis()/1000-beginTime) +
                      frame);
    }
}
