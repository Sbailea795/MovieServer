import java.nio.ByteBuffer;
import java.util.Arrays;

public class MovieMessage{
    private final int MAXSIZE = 15000;
    private int _segment;
    private int _frameNumber;
    private byte[] _frame;
    
/*********************************
 * METHOD: Message constructor   *
 * INPUT PARAMETERS :            *`~
 *  connection id, first string, *
 *  second string, debug mode    *
 * OUTPUT:                       *
 *  None                         *
 *********************************/
    public MovieMessage(int segment, int frameNumber, String frame){
        _segment = segment;
        _frameNumber = frameNumber;
        _frame = frame.getBytes();
    }


/********************************************
  * METHOD: Serialize:                      *
  *  Converts the int and strings into      *
  *  a byte array for transport             *
  * INPUT:                                  *
  *     None                                *
  * OUTPUT:                                 *
  *     the array of bytes, ready for       *
  *     transport                           *
  *******************************************/
    public byte[] serialize(){
        byte[] rval = new byte[MAXSIZE];
        for (int i = 0; i < MAXSIZE; ++i)
            rval[i] = 0x00;

        byte[] segment = ByteBuffer.allocate(6).putInt(_segment).array();
        for (int i = 0; i < 6; ++i)
            rval[i] = segment[i];
        byte[] frameNum = ByteBuffer.allocate(4).putInt(_frameNumber).array();
            for (int i = 6; i < 10; ++i)
                rval[i] = frameNum[i-6];
        for (int i = 0; i < _frame.length; ++i){
            rval[10+i] = _frame[i];
        }
        return rval;
    }


/********************************************
  * METHOD: deserialize:                    *
  *  Converts a bytestream into an int and  *
  *  two strings                            *
  * INPUT:                                  *
  *     the byte array to convert           *
  * OUTPUT:                                 *
  *     None                                *
  *******************************************/
    public void deserialize(byte[] msg){
        byte[] segment = Arrays.copyOfRange(msg, 0, 6);
        _segment = ByteBuffer.wrap(segment).getInt();
        
        byte[] frameNum = Arrays.copyOfRange(msg, 6, 10);
        _frameNumber = ByteBuffer.wrap(frameNum).getInt();

        String frame = "";
        
        for (int i = 0; i < MAXSIZE - 10 && msg[10+i] != 0; ++i){
            frame +=(char) msg[10 + i];
        }
        _frame = frame.getBytes();
    }


/********************************************
  * METHOD: get*:                           *
  *  Returns info on specified data member  *
  * INPUT:                                  *
  *     none                                *
  * OUTPUT:                                 *
  *     Data member value                   *
  *******************************************/
    
    public int getSegment(){
        return _segment;
    }

    public int getFrameNumber(){
        return _frameNumber;
    }

    public String getFrame(){
        return new String(_frame);
    }
}
