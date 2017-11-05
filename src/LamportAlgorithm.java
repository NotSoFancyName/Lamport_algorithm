/**
 * Created by Вова on 28.10.2017.
 */

import java.util.concurrent.CyclicBarrier;

import static java.lang.Thread.sleep;

/**
 *
 */

public class LamportAlgorithm {

    private static BakeryLock lock = new BakeryLock();

    public static void main(String args[]){

        MyInteger poorCounter = new MyInteger();
        Thread t1 = new Thread(new IncrementThread(poorCounter,lock));
        Thread t2 = new Thread(new DecrementThread(poorCounter,lock));
        Thread t3 = new Thread(new IncrementThread(poorCounter,lock));
        Thread t4 = new Thread(new DecrementThread(poorCounter,lock));

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        while(true) {
            try {
                sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(" " + poorCounter.getInt());
        }
    }
}


