import java.nio.charset.Charset;
import java.util.Random;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;

class randomStrSocketClient{
    public static void main(String[] args) throws Exception {
        Random r = new Random();
        String str = "",str2 = "";
        for (int i = 0; i < 10*1024; i++) {
            str = str + (char) (65+r.nextInt(90-65));

        }
        System.out.println("Client:String generated: " + str);
        Socket s=new Socket("localhost",8000); 
        DataOutputStream dout=new DataOutputStream(s.getOutputStream());
        DataInputStream din=new DataInputStream(s.getInputStream());
        dout.writeUTF(str);  
        dout.flush();  
        str2=din.readUTF();  
        System.out.println("Client: Received from Server: "+str2);  
        dout.close();  
        s.close();
    }
}