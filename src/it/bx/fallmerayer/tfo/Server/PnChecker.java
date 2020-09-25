package it.bx.fallmerayer.tfo.Server;

import java.util.concurrent.Callable;

public class PnChecker implements Callable<Boolean> {

    private final long toCheck;

    public PnChecker(long toCheck) {
        this.toCheck = toCheck;
    }

    //checks if a number is prime
    public boolean isPrime(){
        if (toCheck <= 1){
            return false;
        }
        for (int i = 2; i < toCheck; i++) {
            if (toCheck % i == 0) {
                return false;
            }
        }

        return true;
    }

    //starts prime numbers check
    @Override
    public Boolean call() {
        return isPrime();
    }
}
