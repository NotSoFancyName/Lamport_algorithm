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
        Thread t1 = new Thread(new IncrementThread(poorCounter,lock,barrier,2));
        Thread t2 = new Thread(new IncrementThread(poorCounter,lock,barrier,-1));
        Thread t3 = new Thread(new IncrementThread(poorCounter,lock,barrier,-1));
        Thread t4 = new Thread(new IncrementThread(poorCounter,lock,barrier,0));

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
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Cunter : " + poorCounter.getInt());
            System.out.println("decremented : " + poorCounter.getDecremented());
            System.out.println("incremented : " + poorCounter.getIncremented() + '\n');

        }
    }
}


