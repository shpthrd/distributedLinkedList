import java.util.ArrayList;
class nodeExample{
    public static void main(String[] args) throws Exception {
        Node node = new Node();
        //node.NodeRun();
        node.Consumer();
        node.Producer();
        ArrayList<Item> itens = new ArrayList<Item>(node.itemMap.values());
        for (int i = 0; i < itens.size(); i++) {
            System.out.println((itens.get(i).getKey())+" "+(itens.get(i).getInfo())+" "+(itens.get(i).getNextKey()));
        }
        System.out.println("test ok");
    }
}