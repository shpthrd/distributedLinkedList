class LinkedList{
    Item first;
    Item last;

    LinkedList(){

    }
    LinkedList(Item first){
        this.first = first;
        this.last = first;
        
    }
    public Item getFirst() {
        return first;
    }
    public void setFirst(Item first) {
        this.first = first;
    }
    public Item getLast() {
        return last;
    }
    public void setLast(Item last) {
        this.last = last;
    }
    
    
}