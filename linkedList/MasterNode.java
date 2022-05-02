import java.util.HashMap;
import java.util.Scanner;
import java.util.List;
import java.util.LinkedList;
import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Queue;

class MasterNode extends Node{
    int countItens = 0;
    int firstKey;
    int firstMachineKey;
    int lastKey;
    int lastMachineKey;
    ServerSocket ss;
    MasterNode() throws Exception{
        super(0);
        this.machineKey = 8000;
        //ss = new ServerSocket(this.machineKey);
        this.itemMap = new HashMap<Integer, Item>();
        this.neighborKey = new LinkedList<Integer>();
        this.neighborKey.add(this.machineKey);//ele próprio é o primeiro da lista
        System.out.println("criado o master");
    }
    void MasterProducer() throws Exception{
        System.out.println("Start Producer");
        String code = "";
        while(code != "exit"){
            System.out.println("Producer: Before ServerSocket");
            ss = new ServerSocket(this.machineKey);
            Socket s = ss.accept();
            DataInputStream din=new DataInputStream(s.getInputStream());
            code = din.readUTF();
            din.close(); 
            s.close(); 
            ss.close();
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

    void MasterConsumer() throws Exception{
        System.out.println("Start Consumer");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run(){
                System.out.println("starting new thread");//VAI DAR MERDA
                String code = "";
                while(code != "exit"){
                    System.out.println("Consumer: after while code: "+code);
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
                    System.out.println("Consumer: command " + cmd[0]);
                    try {
                        switch (cmd[0]) {
                            case "ADD": //ADD##INFO
                                System.out.println("add");
                                addItem(cmd[1]);
                                break;
                            
                            case "RET": //RET##key do item no hashmap ## machineKey para onde enviar
                                System.out.println("retrieve");
                                retrieveInfo(Integer.parseInt(cmd[1]),Integer.parseInt(cmd[2]));
                                break;
                            
                            case "SER": //SER##key do item sendo procurado ## para onde responder ## a info procurada
                                search(Integer.parseInt(cmd[1]),Integer.parseInt(cmd[2]), cmd[3]);
                                break;
                            
                            case "ADN": //add node -> ADN##NODEKEY
                                addNode(Integer.parseInt(cmd[1]));
                                break;
                            case "UPL": //update last -> UPL##LASTKEY##LASTMACHINEKEY
                                updateLast(Integer.parseInt(cmd[1]),Integer.parseInt(cmd[2]));
                                break;
                            case "LAD": //ATUALIZA UM ITEM COLOCANDO OS DADOS DO PRÓXIMO ITEM QUE ELE APONTA -> LAD## KEY DO ITEM DESTE NODE A MODIFICAR ## KEY DO NEXT ITEM ## MACHINE KEY DO NEXT ITEM
                                addNext(Integer.parseInt(cmd[1]),Integer.parseInt(cmd[2]), Integer.parseInt(cmd[3]));
                                break;
                            case "EXIT":
                                System.out.println("Consumer: entrou no case EXIT");
                                code = "exit";
                                spawnExit();
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

    void addItem(String info){
        System.out.println("master|inside add");
        if(countItens == 0){
            System.out.println("master|inserindo na lista vazia");
            Item item = new Item(info, this.machineKey);
            itemMap.put(item.getKey(),item);
            this.firstKey = item.getKey();
            this.firstMachineKey = this.machineKey;
            this.lastKey = item.getKey();
            this.lastMachineKey = this.machineKey;
        }
        else{
            if(this.neighborKey.size() < 1){//so tem esse node (master)
                System.out.println("master|master sozinho");
                Item item = new Item(info,this.machineKey);
                this.itemMap.get(this.lastKey).nextKey = item.key;
                this.itemMap.get(this.lastKey).nextmachineKey = item.machineKey;
                itemMap.put(item.getKey(),item);
                this.lastKey = item.getKey();//verificar se isso aqui nao da pau
                this.lastMachineKey = this.machineKey;
            }
            else{
                int chosen = countItens%neighborKey.size();
                if(chosen == 0){//SERÁ INSERIDO NO MASTER PELA RODADA NORMAL
                    System.out.println("master|master escolhido");
                    Item item = new Item(info,this.machineKey);
                    itemMap.put(item.getKey(),item);
                    if(this.lastMachineKey == this.machineKey){//se o last estiver no master não mandar msg via rede //pouco provavel, mas pra garantir
                        System.out.println("master|o ultimo item esta no master e mais um item está sendo inserido no master");
                        this.itemMap.get(this.lastKey).setNextKey(item.getKey());
                        this.itemMap.get(this.lastKey).setNextmachineKey(item.getMachineKey());
                        
                        this.lastKey = item.getKey();//verificar se isso aqui nao da pau
                        this.lastMachineKey = this.machineKey;
                    }
                    else{
                        try {
                            System.out.println("master|o ultimo item nao esta no master e mais um item esta sendo inserido");
                            /*Socket s = new Socket("localhost",neighborKey.get(this.lastMachineKey));
                            DataOutputStream dout=new DataOutputStream(s.getOutputStream());
                            dout=new DataOutputStream(s.getOutputStream());
                            dout.writeUTF("lad##"+this.lastKey+"##"+item.getKey()+"##"+this.machineKey);//last add pattern LAD##KAY DO ITEM MODIFICADO##KEY DO NEXT ITEM##MACHINE KEY DO NEXT ITEM 
                            dout.flush();
                            dout.close();
                            s.close();*/
                            sendMsg("lad##"+this.lastKey+"##"+item.getKey()+"##"+this.machineKey, this.lastMachineKey);
                        } catch (Exception e) {
                            //TODO: handle exception
                        }
                    }

                }
                else{//O ITEM SERÁ ADICIONADO EM OUTRO NODE
                    try{
                        System.out.println("master|o item será inserido em outro node");
                        sendMsg("add##"+ info + "##" + this.lastKey + "##" + this.lastMachineKey, neighborKey.get(chosen));
                    }catch (Exception e) {
                        //TODO: handle exception
                    }
                }
            }
        }

        this.countItens++;
        System.out.println("end of add");
    }

    void addNode(int nodeMachineKey){
        neighborKey.add(nodeMachineKey);
        System.out.println("node added");
    }

    void updateLast(int lastKey, int lastMachineKey){
        this.lastKey = lastKey;
        this.lastMachineKey = lastMachineKey;
    }
    void spawnExit() throws Exception{
        if(neighborKey.size() <=1) return;
        for (int i = 1; i < neighborKey.size(); i++) {
            try {
                sendMsg("exit##0", neighborKey.get(i));
            } catch (Exception e) {
                //TODO: handle exception
            }
            
        }
    }
}
