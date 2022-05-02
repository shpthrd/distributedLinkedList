import java.nio.charset.Charset;
import java.util.Random;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;

class randomStrSocketServer{
    public static void main(String[] args) throws Exception{
        String str = "";
        ServerSocket ss=new ServerSocket(8000);  
        Socket s=ss.accept(); 
        DataInputStream din=new DataInputStream(s.getInputStream());  
        DataOutputStream dout=new DataOutputStream(s.getOutputStream());
        str = din.readUTF();
        System.out.println("Server: Received from Client: " + str);
        dout.writeUTF(str+"OK");  
        dout.flush();
        din.close();
        s.close();  
        ss.close();
    }
}