class Item{
    int nextKey;
    int nextmachineKey;//o key vai apontar no hashmap para uma porta, já que o ip é localhost
    //before em um sistema onde se apaga/transfere itens é necessário o before para avisar o anterior para onde apontar
    int key;
    int machineKey;
    String info;
    Item(){

    }
    Item(String info,int machineKey){
        setKey(RandomAux.getKey());
        setMachineKey(machineKey);
        setInfo(info);
    }




    //GETTERS E SETTERS
    public int getNextKey() {
        return nextKey;
    }
    public void setNextKey(int nextKey) {
        this.nextKey = nextKey;
    }
    public int getNextmachineKey() {
        return nextmachineKey;
    }
    public void setNextmachineKey(int nextmachineKey) {
        this.nextmachineKey = nextmachineKey;
    }
    public int getKey() {
        return key;
    }
    public void setKey(int key) {
        this.key = key;
    }
    public int getMachineKey() {
        return machineKey;
    }
    public void setMachineKey(int machineKey) {
        this.machineKey = machineKey;
    }
    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;
    }
    
}