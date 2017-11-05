import org.omg.CORBA.INTERNAL;

/**
 * Created by Вова on 29.10.2017.
 */
public class IncrementThread implements Runnable {

    private BakeryLock lock;
    private MyInteger counter;

    public IncrementThread(MyInteger counter,BakeryLock lock){
        this.lock = lock;
        this.counter = counter;
    }

    @Override
    public void run() {

        lock.register();
        while(true){
            lock.lock();
            counter.inc();
            lock.unlock();
        }
        //lock.unregister();
    }


}
