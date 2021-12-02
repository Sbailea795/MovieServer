public class Message{
    private int _cid;
    private byte[] _str1;
    private byte[] _str2;
    //private boolean _debug;
    
/*********************************
 * METHOD: Message constructor   *
 * INPUT PARAMETERS :            *`~
 *  connection id, first string, *
 *  second string, debug mode    *
 * OUTPUT:                       *
 *  None                         *
 *********************************/
    public Message(int cid, String str1, String str2){
        _cid = cid;
        _str1 = str1.getBytes();
        _str2 = str2.getBytes();
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
        byte[] rval = new byte[164];
        for (int i = 0; i < 164; ++i)
            rval[i] = 0x00;
        rval[3] =(byte) ((_cid & 0xFF000000) >> 24);
        rval[2] =(byte) ((_cid & 0x00FF0000) >> 16);
        rval[1] =(byte) ((_cid & 0x0000FF00) >> 8);
        rval[0] =(byte) ((_cid & 0x000000FF));
        //int max = 0;
        for (int i = 0; i < _str1.length; ++i){
            rval[4+i] = _str1[i];
        }
        for (int i = 0; i < _str2.length; ++i){
            rval[84+i] = _str2[i];
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
        _cid = 0;
        _cid |= ((int) msg[3]) << 24;
        _cid |= ((int) msg[2]) << 16;
        _cid |= ((int) msg[1]) << 8;
        _cid |= ((int) msg[0]);
        String str1 = "";
        String str2 = "";
        
        for (int i = 0; i < 80 && msg[4+i] != 0; ++i){
            str1 +=(char) msg[4 + i];
        }
        _str1 = str1.getBytes();
        for (int i = 0; i < 80 && msg[84+i] != 0; ++i){
            str2 += (char) msg[84 + i];
        }
        _str2 = str2.getBytes();
    }


/********************************************
  * METHOD: get*:                           *
  *  Returns info on specified data member  *
  * INPUT:                                  *
  *     none                                *
  * OUTPUT:                                 *
  *     Data member value                   *
  *******************************************/
    public int getCid(){
        return _cid;
    }

    public String getStr1(){
        return new String(_str1);
    }
    public String getStr2(){
        return new String(_str2);
    }
}
