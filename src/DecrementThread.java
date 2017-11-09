import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Condition;

/**
 * Created by Вова on 29.10.2017.
 */

public class DecrementThread implements Runnable {
    private BakeryLock lock;
    private MyInteger counter;
    private  CyclicBarrier barrier;


    public DecrementThread(MyInteger counter,BakeryLock lock, CyclicBarrier barrier){
        this.lock = lock;
        this.counter = counter;
        this.barrier = barrier;
    }

    @Override
    public void run() {

        lock.register();

        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }


        while(true){
            lock.lock();
            counter.dec();
            lock.unlock();
        }
        //lock.unregister();
    }
}
