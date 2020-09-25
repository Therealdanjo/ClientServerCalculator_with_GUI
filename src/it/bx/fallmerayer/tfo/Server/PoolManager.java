package it.bx.fallmerayer.tfo.Server;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class PoolManager{

    long[] numbers;

    public PoolManager(long[] numbers) {
        this.numbers = numbers;
    }

    //starts the threads that calculate if a number is prime, collects the results from them and gives them back to the main program
    public Boolean[] calc() throws Exception {
        Boolean[] ret = new Boolean[numbers.length];
        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < numbers.length; i++) {
            Callable<Boolean> c = new PnChecker(numbers[i]);
            Future<Boolean> result = executor.submit(c);
            ret[i] = result.get();
        }
        executor.shutdown();
        return ret;
    }

}
