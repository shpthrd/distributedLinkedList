import java.util.Collection;
import java.util.ArrayList;
class Main{
    public static void main(String args []) throws Exception{
        MasterNode masterNode = new MasterNode();
        //masterNode.MasterNodeRun();
        masterNode.MasterConsumer();
        masterNode.MasterProducer();
        ArrayList<Item> itens = new ArrayList<Item>(masterNode.itemMap.values());
        for (int i = 0; i < itens.size(); i++) {
            System.out.println((itens.get(i).getKey())+" "+(itens.get(i).getInfo())+" "+(itens.get(i).getNextKey()));
        }
        System.out.println("test ok");
    }
}