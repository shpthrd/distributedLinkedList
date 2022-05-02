import java.net.*;  
import java.io.*;  
class TestClient{  
    public static void main(String args[])throws Exception{  
        try {
            System.out.println("b4 for");
            for (int i = 0; i < 21; i++) {
                sendMsg("add##infoexample "+i, 8000);
                System.out.println("msg sent "+i);
            }
            System.out.println("out of for");
            sendMsg("exit##0", 8000); 
            System.out.println("end of everything: ");
        } catch (Exception e) {
        //TODO: handle exception
        System.out.println("error");
        }
    }
    static void sendMsg(String msg, int port){// throws Exception{
        boolean scanning=true;
        int i = 0;
        while(scanning) {
            try {
                //socketChannel.open(hostname, port);
                Socket s = new Socket("localhost",port);
                DataOutputStream dout;
                dout=new DataOutputStream(s.getOutputStream());
                dout.writeUTF(msg);
                dout.flush();
                dout.close();
                s.close();
                System.out.println("success");
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    //TODO: handle exception
                }
                scanning=false;
            } catch(Exception e) {
                System.err.println(e);
                System.out.println("Connect failed, waiting and trying again");
                if(i>=10){
                    scanning = true;
                    System.out.println("terminating");
                }
                else{
                    try {
                        i++;
                        Thread.sleep(2000);//2 seconds
                    } catch(InterruptedException ie){
                        ie.printStackTrace();
                    }
                }
                
            } 
        }
    }
}  