
// this is just a wrapper for a String.
// But putting a name to it helps with comprehension
public class Frame {
    private String frame;

    public Frame(){}

    public Frame(String frameLines){
        this.frame = frameLines;
    }

    public void setFrame(String frameString){
        this.frame = frameString;
    }
    public String getFrame(){
        return frame;
    }
}
