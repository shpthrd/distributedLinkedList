import java.nio.charset.Charset;
import java.util.Random;

import java.util.Random;
class randomStr{
    public static void main(String[] args) {
        Random r = new Random();
        String str = "";
        for (int i = 0; i < 10*1024; i++) {
            str = str + (char) (65+r.nextInt(90-65));

        }
        System.out.println(str);
        
    }
}