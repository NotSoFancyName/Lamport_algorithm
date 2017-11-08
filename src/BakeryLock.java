import com.sun.org.apache.xpath.internal.operations.Bool;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Created by Вова on 28.10.2017.
 */


interface FixnumLock extends Lock {

    int getId();
    void register();
    void unregister();

    //reset(); ?
}


public class BakeryLock implements FixnumLock{

    private final int maximumSize = 100;                                        // threads' quantity limit
    private HashMap<Long,Integer> localThreadId = new HashMap<>();              //
    private List<Long> threadTickets = new ArrayList<>(maximumSize);            // Threads' tickets
    private List<Boolean> enteringThread = new ArrayList<>(maximumSize);        // Threads entering critical zone
    private long size = 0;                                                      // current threads size


    BakeryLock(){
        for(int i = 0; i < maximumSize; i++){           // initializing array lists
            threadTickets.add(0L);
            enteringThread.add(false);
        }
    }

    @Override
    public int getId() {
        return localThreadId.get(Thread.currentThread().getId());
    }

    @Override
    synchronized public void register() {
        if(size == maximumSize) return;

        int i = 0;
        while(localThreadId.containsValue(i)){
            i++;
        }

        localThreadId.put(Thread.currentThread().getId(),i);
        ++size;
    }

    @Override
    synchronized public void unregister() {
        localThreadId.remove(Thread.currentThread().getId());
        size--;
    }

    @Override
    public void lock() {

        int curId = getId();
        enteringThread.set(curId,true);

        long max = 0;
        for (long ticket : threadTickets)
        {
            max = Math.max(max, ticket);
        }

        if(max == Long.MAX_VALUE) {
            max = 0;
        }


        threadTickets.set(curId,max + 1);
        enteringThread.set(curId,false);

        for(int i = 0; i < size; i++){

            while(enteringThread.get(i)){
                Thread.yield();
            }

            while( threadTickets.get(i) != 0  &&
                    ( threadTickets.get(curId) > threadTickets.get(i)  ||
                    (Objects.equals(threadTickets.get(curId), threadTickets.get(i)) && curId > i))){
                Thread.yield();
            }
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }

        int curId = getId();
        enteringThread.set(curId,true);

        long max = 0;
        for (long ticket : threadTickets)
        {
            max = Math.max(max, ticket);
        }

        if(max == Long.MAX_VALUE) {
            max = 0;
        }

        threadTickets.set(curId,max + 1);
        enteringThread.set(curId,false);

        for(int i = 0; i < size; i++){

            while(enteringThread.get(i)){

                if(Thread.interrupted()) throw new InterruptedException();
                Thread.yield();
            }

            while( threadTickets.get(i) != 0  &&
                    ( threadTickets.get(curId) > threadTickets.get(i)  ||
                            (Objects.equals(threadTickets.get(curId), threadTickets.get(i)) && curId > i))){
                if(Thread.interrupted()) throw new InterruptedException();
                Thread.yield();
            }
        }
    }

    @Override
    public boolean tryLock() {
        int curId = getId();
        enteringThread.set(curId,true);

        long max = 0;
        for (long ticket : threadTickets)
        {
            max = Math.max(max, ticket);
        }

        if(max == Long.MAX_VALUE) {
            max = 0;
        }


        threadTickets.set(curId,max + 1);
        enteringThread.set(curId,false);

        for(int i = 0; i < size; i++){

            if(enteringThread.get(i)){
                return false;
            }

            if( threadTickets.get(i) != 0  &&
                    ( threadTickets.get(curId) > threadTickets.get(i)  ||
                            (Objects.equals(threadTickets.get(curId), threadTickets.get(i)) && curId > i))){
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {

        long startTime = System.currentTimeMillis();
        long waitingTime = TimeUnit.MILLISECONDS.convert(time, unit);

        int curId = getId();
        enteringThread.set(curId,true);

        long max = 0;
        for (long ticket : threadTickets)
        {
            max = Math.max(max, ticket);
        }

        if(max == Long.MAX_VALUE) {
            max = 0;
        }


        threadTickets.set(curId,max + 1);
        enteringThread.set(curId,false);

        for(int i = 0; i < size; i++){

            while(enteringThread.get(i)){

                if(System.currentTimeMillis() - startTime > waitingTime)return false;
                if(Thread.interrupted()) return false;

                Thread.yield();
            }

            while( threadTickets.get(i) != 0  &&
                    ( threadTickets.get(curId) > threadTickets.get(i)  ||
                            (Objects.equals(threadTickets.get(curId), threadTickets.get(i)) && curId > i))){

                if(System.currentTimeMillis() - startTime > waitingTime)return false;

                if(Thread.interrupted()) return false;
                Thread.yield();
            }
        }

        return true;
    }

    @Override
    public void unlock() {

        threadTickets.set(getId(), (long) 0);
    }

    @Override
    public Condition newCondition() {
        return new BackeryLockCondition();
    }
}


class BackeryLockCondition implements Condition{

    @Override
    public void await() throws InterruptedException {
        synchronized (this) {
            this.wait();
        }
    }

    @Override
    public void awaitUninterruptibly() {
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long awaitNanos(long nanosTimeout) throws InterruptedException {
        synchronized (this) {
            this.wait(TimeUnit.MICROSECONDS.convert(nanosTimeout, NANOSECONDS));
        }
        return nanosTimeout;
    }

    @Override
    public boolean await(long time, TimeUnit unit) throws InterruptedException {
        synchronized (this) {
            this.wait(TimeUnit.MICROSECONDS.convert(time, unit));
        }
        return false;
    }

    @Override
    public boolean awaitUntil(Date deadline) throws InterruptedException {
        return false;
    }

    @Override
    public void signal() {
        synchronized (this) {
            this.notify();
        }
    }

    @Override
    public void signalAll() {
        synchronized (this) {
            this.notifyAll();
        }
    }
}
