/**
 * Created by Вова on 28.10.2017.
 */

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Condition;

import static java.lang.Thread.sleep;

/**
 *
 */

public class LamportAlgorithm {

    private static BakeryLock lock = new BakeryLock();
    private static CyclicBarrier barrier = new CyclicBarrier(5);


    public static void main(String args[]){

        MyInteger poorCounter = new MyInteger();
        Thread t1 = new Thread(new IncrementThread(poorCounter,lock,barrier));
        Thread t2 = new Thread(new DecrementThread(poorCounter,lock,barrier));
        Thread t3 = new Thread(new IncrementThread(poorCounter,lock,barrier));
        Thread t4 = new Thread(new DecrementThread(poorCounter,lock,barrier));

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }




        while(true) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.print(" " + poorCounter.getInt());
        }
    }
}


