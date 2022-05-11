import java.util.HashMap;
import java.util.Scanner;
import java.util.List;
import java.util.LinkedList;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Queue;
import java.util.concurrent.Semaphore;

class ClientNode{
    int machineKey;
    ServerSocket cs;
    static Queue<String> command = new LinkedList<String>();
    static Semaphore mutex1 = new Semaphore(0);   // creating a Semaphore To ImplementLogic
    static Semaphore mutex = new Semaphore(1);        // Creating A Mutex

    ClientNode() throws Exception {
        this.machineKey = RandomAux.getPort();
        System.out.println("client criado");
    }

    void Producer() throws Exception{
        mutex.acquire();//APENAS PARA TESTAR COM A FUNÇÃO AUXILIAR PARA GERAR A ADICAO DE ALGUNS ITENS E DEPOIS PROCURA
        command.offer("SPA##0");
        mutex.release();  //releasing After Production ;
        mutex1.release();
        String code = "";
        while(code != "exit"){//MUDAR PARA RECEBER VIA TCP E DEPOIS IMPLEMENTAR UMA QUEUE
            System.out.println("Before ServerSocket");
            this.cs = new ServerSocket(this.machineKey);
            Socket s = cs.accept();
            DataInputStream din=new DataInputStream(s.getInputStream());  
            code = din.readUTF();
            din.close();  
            s.close();  
            cs.close();
            System.out.println("Producer: After Sockets Close");
            System.out.println("Producer: before mutex");
            mutex.acquire();
            command.offer(code);
            mutex.release();  //releasing After Production ;
            mutex1.release();
            System.out.println("Producer: after mutex com o code: "+ code);
            //String[] cmd = code.split("##");
            if(code.startsWith("exit")){
                code = "exit";
                System.out.println("Producer: entrou no if do exit");
            }
        }
        System.out.println("Producer: fim do while");
    }

    void Consumer() throws Exception{
        Thread t = new Thread(new Runnable() {
            @Override
            public void run(){
                System.out.println("starting new thread");//VAI DAR MERDA
                String code = "";
                while(code!="exit"){
                    
                    Boolean noCommand = true;
                    while(noCommand){
                        try {
                            System.out.println("Consumer: before mutex");
                            mutex1.acquire();     /// Again Acquiring So no production while consuming
                            mutex.acquire();
                            if(command.size() < 1){//lista vazia
                                System.out.println("lista vazia");
                                mutex.release();
                                System.out.println("Consumer: after mutex");
                                Thread.sleep(500);
                            }
                            else{//lista não vazia
                                System.out.println("peguei um comando");
                                code = command.poll();
                                mutex.release();
                                System.out.println("Consumer: after mutex");
                                noCommand = false;
                            }
                            
                        } catch (Exception e) {
                        }
                        
                    }
                    String[] cmd = code.split("##");/* ************* ATENÇÃO AQUI, PRECISA ESTAR BEM DE ACORDO COM O CMD*/
                    cmd[0] = cmd[0].toUpperCase();
                    for(int i =0; i<cmd.length;i++){
                        System.out.println(cmd[i]);
                    }
                    try {
                        switch (cmd[0]){
                            case "RES": //RESPOSTA DE UM NODE DE UM RETRIEVE -> RES##INFO##retrieveKey
                                System.out.println("add");
                                retrieveResponse(cmd[1],Integer.parseInt(cmd[2]));
                                break;
                            case "SPA":
                                auxSpawn();
                                break;
                            default:
                                break;
                        }
                    } catch (Exception e) {
                        //TODO: handle exception
                    }
                }
                System.out.println("Consumer: fim do while");
            }
        });
        t.start();

    }

    void retrieveResponse(String info, int retrieveKey){
        System.out.println("retrieved info: "+info+" key: "+retrieveKey);
    }
    void auxSpawn(){
        try {
            System.out.println("b4 for");
            for (int i = 0; i < 10; i++) {
                sendMsg("add##infoexample "+i, 8000);
                System.out.println("msg sent "+i);
            }
            Thread.sleep(3000);
            int retrieveKey = RandomAux.getPort();
            System.out.println("retrieve key gerada: "+retrieveKey);
            sendMsg("RTI##"+this.machineKey+"##"+retrieveKey, 8000);
            Thread.sleep(10000);
            System.out.println("out of for");
            sendMsg("exit##0", 8000); 
            System.out.println("end of everything: ");
        } catch (Exception e) {
        //TODO: handle exception
        System.out.println("error");
        }
    }

    void sendMsg(String msg, int port){// throws Exception{
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