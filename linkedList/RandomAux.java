import java.util.Random;

class RandomAux{
    static Random random = new Random();

    RandomAux(){

    }

    static int getPort(){
        return random.nextInt(5000)+3000;
    }
    static int getKey(){
        return random.nextInt(100000);
    }
}