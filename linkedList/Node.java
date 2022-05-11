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
class Node{
    HashMap<Integer, Item> itemMap;
    List<Integer> neighborKey;//talvez desnecessário, porque os itens já saberão || só o master deve manter uma lista de todos os nodes para fazer o toggle de qual recebe o proximo item
    int machineKey;
    ServerSocket ns;
    static Queue<String> command = new LinkedList<String>();
    static Semaphore mutex1 = new Semaphore(0);   // creating a Semaphore To ImplementLogic
    static Semaphore mutex = new Semaphore(1);        // Creating A Mutex
    //node master tem o contador de itens e faz o toggle para qual é o proximo node a receber um item;
    //node master precisa ter uma referencia do first e last (key e machineKey) PARA 
    Node() throws Exception{
        this.itemMap = new HashMap<Integer, Item>();
        this.neighborKey = new LinkedList<Integer>();
        this.machineKey = RandomAux.getPort();//COLOCAR NUMERO ALEATORIO, TER CERTEZA QUE É UMA PORTA VAZIA
        sendMsg("ADN##"+this.machineKey, 8000);//to add this node
        System.out.println("node criado, porta: "+this.machineKey);
    }
    Node(int i){

    }
    void Producer() throws Exception{
        String code = "";
        while(code != "exit"){//MUDAR PARA RECEBER VIA TCP E DEPOIS IMPLEMENTAR UMA QUEUE
            //System.out.println("Before ServerSocket");
            this.ns = new ServerSocket(this.machineKey);
            Socket s = ns.accept();
            DataInputStream din=new DataInputStream(s.getInputStream());  
            code = din.readUTF();
            din.close();  
            s.close();  
            ns.close();
            //System.out.println("Producer: After Sockets Close");
            //System.out.println("Producer: before mutex");
            mutex.acquire();
            command.offer(code);
            mutex.release();  //releasing After Production ;
            mutex1.release();
            System.out.println("Producer: after mutex com o code: "+ code);
            //String[] cmd = code.split("##");
            if(code.startsWith("exit")){
                code = "exit";
                //System.out.println("Producer: entrou no if do exit");
            }
        }
        System.out.println("Producer: fim do while");
    }

    void Consumer() throws Exception{
        Thread t = new Thread(new Runnable() {
            @Override
            public void run(){
                //System.out.println("starting new thread");//VAI DAR MERDA
                String code = "";
                while(code!="exit"){
                    
                    Boolean noCommand = true;
                    while(noCommand){
                        try {
                            //System.out.println("Consumer: before mutex");
                            mutex1.acquire();     /// Again Acquiring So no production while consuming
                            mutex.acquire();
                            if(command.size() < 1){//lista vazia
                                //System.out.println("lista vazia");
                                mutex.release();
                                //System.out.println("Consumer: after mutex");
                                Thread.sleep(500);
                            }
                            else{//lista não vazia
                                //System.out.println("peguei um comando");
                                code = command.poll();
                                mutex.release();
                                //System.out.println("Consumer: after mutex");
                                noCommand = false;
                            }
                            
                        } catch (Exception e) {
                        }
                        
                    }
                    System.out.println(code);
                    String[] cmd = code.split("##");/* ************* ATENÇÃO AQUI, PRECISA ESTAR BEM DE ACORDO COM O CMD*/
                    cmd[0] = cmd[0].toUpperCase();
                    
                    try {
                        switch (cmd[0]){
                            case "ADD": //ADICIONA UM ITEM NOVO NO NODE -> ADD##key##INFO##lastkey##lastmachinekey
                                System.out.println("add");
                                addItem(Integer.parseInt(cmd[1]),cmd[2],Integer.parseInt(cmd[3]),Integer.parseInt(cmd[4]));
                                break;
                            case "RET": //PERCORRE A LISTA LIGADA E DEVOLVE A INFO DE CADA UM -> RET##key do item no hashmap ## machineKey para onde enviar ## retrieve key
                                System.out.println("retrieve");
                                retrieveInfo(Integer.parseInt(cmd[1]),Integer.parseInt(cmd[2]),Integer.parseInt(cmd[3]));
                                break;
                            case "SER": //PROCURA POR UMA INFO ESPECIFICA -> SER##key do item sendo procurado ## para onde responder ## a info procurada
                                search(Integer.parseInt(cmd[1]),Integer.parseInt(cmd[2]), cmd[3]);
                                break;
                            case "LAD": //ATUALIZA UM ITEM COLOCANDO OS DADOS DO PRÓXIMO ITEM QUE ELE APONTA -> LAD## KEY DO ITEM DESTE NODE A MODIFICAR ## KEY DO NEXT ITEM ## MACHINE KEY DO NEXT ITEM
                                System.out.println("LAD");
                                addNext(Integer.parseInt(cmd[1]),Integer.parseInt(cmd[2]), Integer.parseInt(cmd[3]));
                                break;
                            case "EXIT":
                                System.out.println("Consumer: entrou no case EXIT");
                                code = "exit";
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

    void addItem(int key, String info, int lastKey, int lastMachineKey) throws Exception{//################VERIFICAR
        Item item = new Item(info, key, this.machineKey);
        itemMap.put(item.getKey(), item);
        //mandar msg para o last para atualizar e mandar msg para o master para atualizar a referencia do last
        sendMsg("lad##"+lastKey+"##"+item.getKey()+"##"+this.machineKey, lastMachineKey);
        //sendMsg("upl##"+item.getKey()+"##"+this.machineKey, 8000);//update in the master the last key
        System.out.println("item adicionado");

    }
    void retrieveInfo(int key,int userKey,int retrieveKey) throws Exception{
        Item it = itemMap.get(key);
        sendMsg("RES##"+ it.getInfo() + "##" + retrieveKey, userKey);
        if(it.getNextmachineKey() == 0) return;
        sendMsg("RET##" + it.getNextKey() + "##" + userKey + "##" + retrieveKey, it.getNextmachineKey());
    }

    void search(int key, int userKey, String info){//PROCURA EM UM ITEM ESPECIFICO SE CONSTA A INFO E DEVOLVE PARA O NODE USUARIO
        System.out.println("entrou no search");
    }

    void addNext(int key, int nextKey, int nextMachineKey){
        System.out.println("key before LAD: "+itemMap.get(key).getNextKey());
        Item item = itemMap.get(key);
        item.setNextKey(nextKey);
        item.setNextmachineKey(nextMachineKey);
        System.out.println("key after LAD: "+itemMap.get(key).getNextKey());
    }

    void sendMsg(String msg,int machineKey) throws Exception{
        boolean scanning=true;
        int i = 0;
        while(scanning) {
            try {
                //socketChannel.open(hostname, port);
                Socket s = new Socket("localhost",machineKey);
                DataOutputStream dout;
                dout=new DataOutputStream(s.getOutputStream());
                dout.writeUTF(msg);
                dout.flush();
                dout.close();
                s.close();
                System.out.println("success");
                try {
                    Thread.sleep(500);// *********** ANALISAR QUANTO TEMPO DEIXAR AQUI DE DELAY
                } catch (Exception e) {
                    //TODO: handle exception
                }
                scanning=false;
            } catch(Exception e) {
                System.err.println(e);
                System.out.println("Falha na msg para: "+machineKey+" msg: "+msg);
                if(i>=3){
                    scanning = false;
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