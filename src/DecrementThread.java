/**
 * Created by Вова on 29.10.2017.
 */
public class DecrementThread implements Runnable {
    private BakeryLock lock;
    private MyInteger counter;

    public DecrementThread(MyInteger counter,BakeryLock lock){
        this.lock = lock;
        this.counter = counter;
    }

    @Override
    public void run() {
        lock.register();
        while(true){
            lock.lock();
            counter.dec();
            lock.unlock();
        }
        //lock.unregister();
    }
}
