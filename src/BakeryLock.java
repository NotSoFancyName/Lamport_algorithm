import com.sun.org.apache.xpath.internal.operations.Bool;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

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
    private HashMap<Long,Integer> localThreadId = new HashMap<>();              // Threads' ids corresponds local thread's
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
    public void register() {
        if(size == maximumSize) return;

        int i = 0;
        while(localThreadId.containsValue(i)){
            i++;
        }

        localThreadId.put(Thread.currentThread().getId(),i);
        ++size;
    }

    @Override
    public void unregister() {
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

        long startTime;

        switch (unit) {
            case MILLISECONDS :startTime = System.currentTimeMillis();
            case NANOSECONDS: startTime = System.nanoTime();
            default: startTime = System.nanoTime();
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

                switch (unit) {
                    case MILLISECONDS : if(System.currentTimeMillis() - startTime > time)return false;
                    case NANOSECONDS: if(System.nanoTime() - startTime > time) return false;
                }

                Thread.yield();
            }

            while( threadTickets.get(i) != 0  &&
                    ( threadTickets.get(curId) > threadTickets.get(i)  ||
                            (Objects.equals(threadTickets.get(curId), threadTickets.get(i)) && curId > i))){

                switch (unit) {
                    case MILLISECONDS : if(System.currentTimeMillis() - startTime > time)return false;
                    case NANOSECONDS: if(System.nanoTime() - startTime > time) return false;
                }

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

    }

    @Override
    public void awaitUninterruptibly() {

    }

    @Override
    public long awaitNanos(long nanosTimeout) throws InterruptedException {
        return 0;
    }

    @Override
    public boolean await(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public boolean awaitUntil(Date deadline) throws InterruptedException {
        return false;
    }

    @Override
    public void signal() {

    }

    @Override
    public void signalAll() {

    }
}

/*

public class BakeryLock implements  FixnumLock {

    private long currentThreadId = -1;                  // current thread that aquiered a lock
    private HashMap<Long,Integer> threadTickets;
    private HashMap<Long,Boolean> enteringThread;
    private Integer maximumTicketValue;

    private final int maximumQuantity = 100;
    private int threadQuantity = 0;

    public BakeryLock(){
        threadTickets = new HashMap<>();
        enteringThread = new HashMap<>();
        maximumTicketValue = 0;
    }

    @Override
    public long getId() {
        return currentThreadId;
    }

    @Override
    public void register() {
        if(threadQuantity != maximumQuantity) {
            threadTickets.put(Thread.currentThread().getId(), 0);
            enteringThread.put(Thread.currentThread().getId(), false);
            threadQuantity++;
        }
    }

    @Override
    public void unregister() {
        if(threadTickets.remove(Thread.currentThread().getId(),0)){
            threadQuantity--;
            enteringThread.remove(Thread.currentThread().getId());
        }
    }

    @Override
    public void lock() {

        long CurrentId = Thread.currentThread().getId();

        if(threadTickets.containsKey(CurrentId)){

            enteringThread.replace(CurrentId, true);
            threadTickets.replace(CurrentId, ++maximumTicketValue);
            enteringThread.replace(CurrentId, false);

            for (Map.Entry<Long, Boolean> entry : enteringThread.entrySet())
            {
                while(entry.getValue()){
                    Thread.yield();
                }

                while ((threadTickets.get(entry.getKey()) != 0) && compareThreads(CurrentId, entry.getKey())){
                    Thread.yield();
                }
            }
        }


    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        threadTickets.replace(Thread.currentThread().getId(), 0);
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    // check threads priority
    private boolean compareThreads(Long id1, Long id2){

        if(threadTickets.get(id1) > threadTickets.get(id2))
            return true;
        else if(threadTickets.get(id1) < threadTickets.get(id2))
            return false;
        else
            return (id1 > id2);
    }
}*/
