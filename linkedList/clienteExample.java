class clienteExample{
    public static void main(String[] args) throws Exception{
        ClientNode clientNode = new ClientNode();
        clientNode.Consumer();
        clientNode.Producer();
        System.out.println("end of clientNode");
    }
}